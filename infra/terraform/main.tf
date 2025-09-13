terraform {
  required_version = ">= 1.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "~> 2.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }

  backend "azurerm" {
    resource_group_name  = "ecommerce-tfstate-rg"
    storage_account_name = "ecommercetfstate"
    container_name       = "tfstate"
    key                  = "terraform.tfstate"
  }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = true
      recover_soft_deleted_key_vaults = true
    }
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

provider "azuread" {}

# Data sources
data "azurerm_client_config" "current" {}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = var.resource_group_name
  location = var.location

  tags = var.common_tags
}

# Virtual Network
resource "azurerm_virtual_network" "main" {
  name                = "${var.project_name}-vnet"
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  tags = var.common_tags
}

# Subnets
resource "azurerm_subnet" "aks" {
  name                 = "aks-subnet"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.1.0/24"]
}

resource "azurerm_subnet" "db" {
  name                 = "db-subnet"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.2.0/24"]
  
  delegation {
    name = "fs"
    service_delegation {
      name = "Microsoft.DBforPostgreSQL/flexibleServers"
      actions = [
        "Microsoft.Network/virtualNetworks/subnets/join/action",
      ]
    }
  }
}

resource "azurerm_subnet" "cache" {
  name                 = "cache-subnet"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.3.0/24"]
}

# Network Security Groups
resource "azurerm_network_security_group" "db" {
  name                = "${var.project_name}-db-nsg"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  security_rule {
    name                       = "AllowPostgreSQL"
    priority                   = 1001
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "5432"
    source_address_prefix      = "10.0.0.0/16"
    destination_address_prefix = "*"
  }

  tags = var.common_tags
}

resource "azurerm_subnet_network_security_group_association" "db" {
  subnet_id                 = azurerm_subnet.db.id
  network_security_group_id = azurerm_network_security_group.db.id
}

# Azure Kubernetes Service
resource "azurerm_kubernetes_cluster" "main" {
  name                = "${var.project_name}-aks"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "${var.project_name}-aks"
  kubernetes_version  = var.kubernetes_version

  default_node_pool {
    name                = "system"
    node_count          = var.aks_system_node_count
    vm_size             = var.aks_system_node_size
    vnet_subnet_id      = azurerm_subnet.aks.id
    type                = "VirtualMachineScaleSets"
    os_disk_size_gb     = 100
    max_pods            = 110
    enable_auto_scaling = true
    min_count          = 1
    max_count          = 5

    upgrade_settings {
      max_surge = "33%"
    }
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    network_policy    = "azure"
    load_balancer_sku = "standard"
    service_cidr      = "10.1.0.0/16"
    dns_service_ip    = "10.1.0.10"
  }

  auto_scaler_profile {
    balance_similar_node_groups      = false
    expander                        = "random"
    max_graceful_termination_sec    = "600"
    max_node_provisioning_time      = "15m"
    max_unready_nodes              = 3
    max_unready_percentage         = 45
    new_pod_scale_up_delay         = "10s"
    scale_down_delay_after_add     = "10m"
    scale_down_delay_after_delete  = "10s"
    scale_down_delay_after_failure = "3m"
    scan_interval                  = "10s"
    scale_down_unneeded           = "10m"
    scale_down_unready            = "20m"
    scale_down_utilization_threshold = 0.5
  }

  azure_policy_enabled = true
  
  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  }

  tags = var.common_tags
}

# Additional Node Pool for Applications
resource "azurerm_kubernetes_cluster_node_pool" "apps" {
  name                  = "apps"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.main.id
  vm_size              = var.aks_app_node_size
  node_count           = var.aks_app_node_count
  vnet_subnet_id       = azurerm_subnet.aks.id
  os_disk_size_gb      = 100
  max_pods             = 110
  enable_auto_scaling  = true
  min_count           = 2
  max_count           = 10

  node_taints = ["workload=applications:NoSchedule"]
  
  upgrade_settings {
    max_surge = "33%"
  }

  tags = var.common_tags
}

# Azure Container Registry
resource "azurerm_container_registry" "main" {
  name                = "${replace(var.project_name, "-", "")}acr"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Standard"
  admin_enabled       = false

  identity {
    type = "SystemAssigned"
  }

  tags = var.common_tags
}

# Grant AKS access to ACR
resource "azurerm_role_assignment" "aks_acr" {
  principal_id                     = azurerm_kubernetes_cluster.main.kubelet_identity[0].object_id
  role_definition_name             = "AcrPull"
  scope                           = azurerm_container_registry.main.id
  skip_service_principal_aad_check = true
}

# PostgreSQL Flexible Server
resource "azurerm_postgresql_flexible_server" "main" {
  name                   = "${var.project_name}-postgres"
  resource_group_name    = azurerm_resource_group.main.name
  location              = azurerm_resource_group.main.location
  version               = "15"
  delegated_subnet_id   = azurerm_subnet.db.id
  private_dns_zone_id   = azurerm_private_dns_zone.postgres.id
  administrator_login   = var.postgres_admin_login
  administrator_password = var.postgres_admin_password
  zone                  = "1"
  storage_mb            = 32768
  sku_name             = var.postgres_sku

  backup_retention_days        = 7
  geo_redundant_backup_enabled = false

  high_availability {
    mode                      = "ZoneRedundant"
    standby_availability_zone = "2"
  }

  maintenance_window {
    day_of_week  = 0
    start_hour   = 8
    start_minute = 0
  }

  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]

  tags = var.common_tags
}

# PostgreSQL Databases
resource "azurerm_postgresql_flexible_server_database" "databases" {
  for_each  = toset(var.postgres_databases)
  name      = each.value
  server_id = azurerm_postgresql_flexible_server.main.id
  collation = "en_US.utf8"
  charset   = "utf8"
}

# Private DNS Zone for PostgreSQL
resource "azurerm_private_dns_zone" "postgres" {
  name                = "${var.project_name}-postgres.private.postgres.database.azure.com"
  resource_group_name = azurerm_resource_group.main.name

  tags = var.common_tags
}

resource "azurerm_private_dns_zone_virtual_network_link" "postgres" {
  name                  = "${var.project_name}-postgres-vnet-link"
  private_dns_zone_name = azurerm_private_dns_zone.postgres.name
  virtual_network_id    = azurerm_virtual_network.main.id
  resource_group_name   = azurerm_resource_group.main.name

  tags = var.common_tags
}

# Azure Cache for Redis
resource "azurerm_redis_cache" "main" {
  name                = "${var.project_name}-redis"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  capacity            = var.redis_capacity
  family              = var.redis_family
  sku_name           = var.redis_sku
  enable_non_ssl_port = false
  minimum_tls_version = "1.2"
  subnet_id          = azurerm_subnet.cache.id

  redis_configuration {
    enable_authentication = true
  }

  tags = var.common_tags
}

# Event Hubs Namespace (for Kafka-compatible streaming)
resource "azurerm_eventhub_namespace" "main" {
  name                = "${var.project_name}-eventhubs"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "Standard"
  capacity            = 2
  kafka_enabled       = true

  tags = var.common_tags
}

# Event Hubs
resource "azurerm_eventhub" "topics" {
  for_each            = toset(var.kafka_topics)
  name                = each.value
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = azurerm_resource_group.main.name
  partition_count     = 3
  message_retention   = 7
}

# Service Bus Namespace (for RabbitMQ replacement)
resource "azurerm_servicebus_namespace" "main" {
  name                = "${var.project_name}-servicebus"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "Standard"

  tags = var.common_tags
}

# Service Bus Queues
resource "azurerm_servicebus_queue" "queues" {
  for_each     = toset(var.servicebus_queues)
  name         = each.value
  namespace_id = azurerm_servicebus_namespace.main.id

  enable_partitioning = true
  max_delivery_count  = 3
  default_message_ttl = "P14D"
}

# Key Vault
resource "azurerm_key_vault" "main" {
  name                       = "${var.project_name}-kv"
  location                   = azurerm_resource_group.main.location
  resource_group_name        = azurerm_resource_group.main.name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = false

  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = data.azurerm_client_config.current.object_id

    certificate_permissions = [
      "Create",
      "Delete",
      "DeleteIssuers",
      "Get",
      "GetIssuers",
      "Import",
      "List",
      "ListIssuers",
      "ManageContacts",
      "ManageIssuers",
      "SetIssuers",
      "Update",
    ]

    key_permissions = [
      "Backup",
      "Create",
      "Decrypt",
      "Delete",
      "Encrypt",
      "Get",
      "Import",
      "List",
      "Purge",
      "Recover",
      "Restore",
      "Sign",
      "UnwrapKey",
      "Update",
      "Verify",
      "WrapKey",
    ]

    secret_permissions = [
      "Backup",
      "Delete",
      "Get",
      "List",
      "Purge",
      "Recover",
      "Restore",
      "Set",
    ]
  }

  # Grant AKS access to Key Vault
  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = azurerm_kubernetes_cluster.main.identity[0].principal_id

    secret_permissions = [
      "Get",
      "List",
    ]
  }

  tags = var.common_tags
}

# Store secrets in Key Vault
resource "azurerm_key_vault_secret" "postgres_connection_string" {
  name         = "postgres-connection-string"
  value        = "postgresql://${var.postgres_admin_login}:${var.postgres_admin_password}@${azurerm_postgresql_flexible_server.main.fqdn}:5432"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_key_vault.main]
}

resource "azurerm_key_vault_secret" "redis_connection_string" {
  name         = "redis-connection-string"
  value        = azurerm_redis_cache.main.primary_connection_string
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_key_vault.main]
}

resource "azurerm_key_vault_secret" "eventhub_connection_string" {
  name         = "eventhub-connection-string"
  value        = azurerm_eventhub_namespace.main.default_primary_connection_string
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_key_vault.main]
}

# Log Analytics Workspace
resource "azurerm_log_analytics_workspace" "main" {
  name                = "${var.project_name}-logs"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "PerGB2018"
  retention_in_days   = 30

  tags = var.common_tags
}

# Application Insights
resource "azurerm_application_insights" "main" {
  name                = "${var.project_name}-insights"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  workspace_id        = azurerm_log_analytics_workspace.main.id
  application_type    = "web"

  tags = var.common_tags
}

# Storage Account for monitoring data
resource "azurerm_storage_account" "monitoring" {
  name                     = "${replace(var.project_name, "-", "")}monitoring"
  resource_group_name      = azurerm_resource_group.main.name
  location                 = azurerm_resource_group.main.location
  account_tier             = "Standard"
  account_replication_type = "LRS"

  tags = var.common_tags
}

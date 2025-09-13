# infra/terraform/outputs.tf
output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "aks_cluster_name" {
  description = "Name of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.name
}

output "aks_cluster_fqdn" {
  description = "FQDN of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.fqdn
}

output "aks_cluster_id" {
  description = "ID of the AKS cluster"
  value       = azurerm_kubernetes_cluster.main.id
}

output "acr_login_server" {
  description = "Login server of the ACR"
  value       = azurerm_container_registry.main.login_server
}

output "acr_name" {
  description = "Name of the ACR"
  value       = azurerm_container_registry.main.name
}

output "postgres_fqdn" {
  description = "FQDN of the PostgreSQL server"
  value       = azurerm_postgresql_flexible_server.main.fqdn
  sensitive   = true
}

output "redis_hostname" {
  description = "Hostname of the Redis cache"
  value       = azurerm_redis_cache.main.hostname
  sensitive   = true
}

output "redis_port" {
  description = "Port of the Redis cache"
  value       = azurerm_redis_cache.main.ssl_port
}

output "eventhub_namespace_name" {
  description = "Name of the Event Hub namespace"
  value       = azurerm_eventhub_namespace.main.name
}

output "servicebus_namespace_name" {
  description = "Name of the Service Bus namespace"
  value       = azurerm_servicebus_namespace.main.name
}

output "key_vault_uri" {
  description = "URI of the Key Vault"
  value       = azurerm_key_vault.main.vault_uri
}

output "log_analytics_workspace_id" {
  description = "ID of the Log Analytics workspace"
  value       = azurerm_log_analytics_workspace.main.id
}

output "application_insights_connection_string" {
  description = "Connection string for Application Insights"
  value       = azurerm_application_insights.main.connection_string
  sensitive   = true
}

output "kube_config" {
  description = "Kubernetes config"
  value       = azurerm_kubernetes_cluster.main.kube_config_raw
  sensitive   = true
}
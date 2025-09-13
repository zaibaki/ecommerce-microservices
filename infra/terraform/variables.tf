# infra/terraform/variables.tf
variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "ecommerce-platform"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "East US 2"
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
  default     = "ecommerce-platform-rg"
}

variable "kubernetes_version" {
  description = "Kubernetes version"
  type        = string
  default     = "1.28.3"
}

variable "aks_system_node_count" {
  description = "Number of system nodes"
  type        = number
  default     = 3
}

variable "aks_system_node_size" {
  description = "Size of system nodes"
  type        = string
  default     = "Standard_D2s_v3"
}

variable "aks_app_node_count" {
  description = "Number of application nodes"
  type        = number
  default     = 3
}

variable "aks_app_node_size" {
  description = "Size of application nodes"
  type        = string
  default     = "Standard_D4s_v3"
}

variable "postgres_admin_login" {
  description = "PostgreSQL admin username"
  type        = string
  default     = "ecommerce_admin"
  sensitive   = true
}

variable "postgres_admin_password" {
  description = "PostgreSQL admin password"
  type        = string
  sensitive   = true
}

variable "postgres_sku" {
  description = "PostgreSQL SKU"
  type        = string
  default     = "GP_Standard_D2s_v3"
}

variable "postgres_databases" {
  description = "List of PostgreSQL databases to create"
  type        = list(string)
  default = [
    "ecommerce_users",
    "ecommerce_inventory",
    "ecommerce_payments",
    "ecommerce_analytics"
  ]
}

variable "redis_capacity" {
  description = "Redis cache capacity"
  type        = number
  default     = 1
}

variable "redis_family" {
  description = "Redis cache family"
  type        = string
  default     = "C"
}

variable "redis_sku" {
  description = "Redis cache SKU"
  type        = string
  default     = "Standard"
}

variable "kafka_topics" {
  description = "List of Kafka topics to create"
  type        = list(string)
  default = [
    "order-events",
    "payment-events",
    "inventory-events",
    "user-events",
    "analytics-events"
  ]
}

variable "servicebus_queues" {
  description = "List of Service Bus queues to create"
  type        = list(string)
  default = [
    "order-notification-queue",
    "payment-processing-queue",
    "inventory-sync-queue",
    "fraud-detection-queue"
  ]
}

variable "common_tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default = {
    Project     = "E-commerce Platform"
    Environment = "Production"
    ManagedBy   = "Terraform"
  }
}

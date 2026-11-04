variable "aws_region" {
  description = "AWS region where everything is deployed."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Prefix for resource names."
  type        = string
  default     = "franchise-api"
}

variable "dynamodb_table_name" {
  description = "Single table that stores franchises, branches and products."
  type        = string
  default     = "franchises"
}

variable "container_port" {
  description = "Port the Spring Boot application listens on."
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "Fargate task CPU units (256 = 0.25 vCPU)."
  type        = number
  default     = 256
}

variable "task_memory" {
  description = "Fargate task memory in MiB."
  type        = number
  default     = 512
}

variable "desired_count" {
  description = "Number of running tasks. Set to 0 to stop paying for compute without destroying anything."
  type        = number
  default     = 1
}

variable "allowed_ingress_cidr" {
  description = "CIDR allowed to call the API. Restrict it to your own IP (x.x.x.x/32) if possible."
  type        = string
  default     = "0.0.0.0/0"
}

variable "github_repository" {
  description = "GitHub repository (owner/name) whose main branch may deploy through OIDC."
  type        = string
  default     = "EduardoZabala/franchise-api"
}

variable "create_github_oidc_provider" {
  description = "Create the GitHub OIDC provider. Set to false if the AWS account already has one."
  type        = bool
  default     = true
}

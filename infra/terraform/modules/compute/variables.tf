variable "name" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "instance_type" {
  type = string
}

variable "subnet_id" {
  type = string
}

variable "security_group_ids" {
  type = list(string)
}

variable "domain_name" {
  type = string
}

variable "images_bucket_arn" {
  type = string
}

variable "images_bucket_name" {
  type = string
}

variable "deploy_bucket_arn" {
  type = string
}

variable "deploy_bucket_name" {
  type = string
}

variable "ecr_repository_url" {
  type = string
}

variable "ssm_prefix" {
  type = string
}

variable "db_host" {
  type = string
}

variable "db_name" {
  type = string
}

variable "db_username" {
  type = string
}

variable "admin_email" {
  type = string
}

variable "deploy_script_etag" {
  description = "Stand von deploy.sh im Bucket (nur für die Startreihenfolge)"
  type        = string
}

variable "compose_version" {
  description = "Docker-Compose-Plugin (GitHub-Release)"
  type        = string
  default     = "v2.29.7"
}

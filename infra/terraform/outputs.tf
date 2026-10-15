output "site_url" {
  description = "Öffentliche Adresse der Demo"
  value       = "https://${module.compute.site_host}"
}

output "public_ip" {
  value = module.compute.public_ip
}

output "aws_region" {
  value = var.aws_region
}

output "instance_id" {
  description = "Zugriff ohne SSH: aws ssm start-session --target <id>"
  value       = module.compute.instance_id
}

output "ecr_repository_url" {
  value = module.registry.repository_url
}

output "ssm_prefix" {
  value = local.ssm_prefix
}

output "images_bucket" {
  value = module.storage.images_bucket_name
}

output "db_endpoint" {
  value = module.database.address
}

output "admin_login" {
  description = "Admin-Login; Passwort mit dem Befehl im Wert auslesen"
  value       = "${var.admin_email} – aws ssm get-parameter --with-decryption --name ${local.ssm_prefix}/admin_initial_password --query Parameter.Value --output text"
}

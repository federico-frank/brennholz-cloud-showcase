# Variante C (Release-Plan): eine EC2 mit Docker Compose (App, RabbitMQ, Prometheus, Grafana, Caddy),
# RDS PostgreSQL im privaten Subnetz, Bilder in S3. Kein NAT-Gateway, kein Load Balancer – ~50 USD/Monat
# statt ~145 USD mit Fargate + ALB + NAT. Begründung: docs/01-produkt/release-plan-bewerbung.md

locals {
  name       = "${var.project_name}-${var.environment}"
  ssm_prefix = "/${var.project_name}/${var.environment}"
}

module "network" {
  source = "./modules/network"
  name   = local.name
}

module "storage" {
  source      = "./modules/storage"
  name        = local.name
  deploy_dir  = "${path.root}/../deploy"
  grafana_dir = "${path.root}/../../grafana-provisioning"
}

module "registry" {
  source = "./modules/registry"
  name   = local.name
}

module "secrets" {
  source     = "./modules/secrets"
  ssm_prefix = local.ssm_prefix
}

module "database" {
  source             = "./modules/database"
  name               = local.name
  instance_class     = var.db_instance_class
  subnet_ids         = module.network.private_subnet_ids
  security_group_ids = [module.network.database_security_group_id]
  password           = module.secrets.db_password
}

module "compute" {
  source             = "./modules/compute"
  name               = local.name
  aws_region         = var.aws_region
  instance_type      = var.instance_type
  subnet_id          = module.network.public_subnet_ids[0]
  security_group_ids = [module.network.web_security_group_id]
  domain_name        = var.domain_name

  images_bucket_arn  = module.storage.images_bucket_arn
  images_bucket_name = module.storage.images_bucket_name
  deploy_bucket_arn  = module.storage.deploy_bucket_arn
  deploy_bucket_name = module.storage.deploy_bucket_name
  ecr_repository_url = module.registry.repository_url
  ssm_prefix         = local.ssm_prefix

  db_host     = module.database.address
  db_name     = module.database.db_name
  db_username = module.database.username
  admin_email = var.admin_email

  # Implizite Abhängigkeit: Die Instanz startet erst, wenn deploy.sh im Bucket liegt
  # (ein modulweites depends_on würde jeden Plan mit Schein-Änderungen füllen)
  deploy_script_etag = module.storage.deploy_script_etag
}

module "budget" {
  source       = "./modules/budget"
  name         = local.name
  limit_usd    = var.budget_limit_usd
  alert_emails = var.budget_alert_emails
}

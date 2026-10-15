# Alle Zugangsdaten werden zufällig erzeugt und als SecureString im SSM Parameter Store abgelegt
# (Standard-Parameter: kostenlos). Die EC2 liest sie beim Deployment über ihre IAM-Rolle.
# Hinweis: Die Werte stehen auch im Terraform-State – der liegt nur lokal und ist per .gitignore ausgeschlossen.

locals {
  secrets = {
    db_password            = 32
    rabbitmq_password      = 32
    grafana_admin_password = 24
    admin_initial_password = 20
    encryption_key         = 32 # AES-256 braucht genau 32 Bytes
  }
}

resource "random_password" "this" {
  for_each = local.secrets
  length   = each.value
  special  = false # sicher in JDBC-URLs, .env-Dateien und RDS (dort sind / @ " verboten)
}

resource "aws_ssm_parameter" "secret" {
  for_each = local.secrets
  name     = "${var.ssm_prefix}/${each.key}"
  type     = "SecureString"
  value    = random_password.this[each.key].result
}

# Welches App-Image läuft; setzt infra/scripts/release.sh bei jedem Release
resource "aws_ssm_parameter" "app_image_tag" {
  name  = "${var.ssm_prefix}/app_image_tag"
  type  = "String"
  value = "none"

  lifecycle {
    ignore_changes = [value]
  }
}

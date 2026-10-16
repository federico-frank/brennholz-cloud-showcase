variable "aws_region" {
  description = "AWS-Region"
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Präfix für alle Ressourcennamen"
  type        = string
  default     = "brennholz"
}

variable "environment" {
  description = "Umgebung (Teil der Namen und des SSM-Pfads)"
  type        = string
  default     = "demo"
}

variable "instance_type" {
  description = "EC2-Typ für App, RabbitMQ, Prometheus, Grafana und Caddy (ARM/Graviton)"
  type        = string
  default     = "t4g.medium"

  validation {
    condition     = startswith(var.instance_type, "t4g.")
    error_message = "Nur t4g-Typen: Image und Compose-Plugin sind für arm64 gebaut."
  }
}

variable "db_instance_class" {
  description = "RDS-Klasse"
  type        = string
  default     = "db.t4g.micro"
}

variable "domain_name" {
  description = "Eigene Domain (A-Record auf die Elastic IP). Leer = <ip>.sslip.io"
  type        = string
  default     = ""
}

variable "dns_zone" {
  description = "Domain, für die eine Route-53-Zone angelegt wird (leer = keine). Unabhängig von domain_name, damit DNS vor dem Umschalten der App steht"
  type        = string
  default     = ""
}

variable "admin_email" {
  description = "Login des Plattform-Admins; das Start-Passwort wird zufällig erzeugt (SSM)"
  type        = string
  default     = "admin@brennholz.de"
}

variable "budget_limit_usd" {
  description = "Monatsbudget; Alarm bei 80 % Ist und 100 % Prognose"
  type        = number
  default     = 60
}

variable "budget_alert_emails" {
  description = "Empfänger der Budget-Alarme"
  type        = list(string)
}

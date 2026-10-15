variable "name" {
  type = string
}

variable "deploy_dir" {
  description = "Ordner mit docker-compose.yml, Caddyfile, prometheus.yml, deploy.sh"
  type        = string
}

variable "grafana_dir" {
  description = "Grafana-Provisionierung (Datenquelle, Dashboards) – dieselbe wie lokal"
  type        = string
}

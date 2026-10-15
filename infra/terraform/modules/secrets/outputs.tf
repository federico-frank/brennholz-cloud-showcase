output "db_password" {
  value     = random_password.this["db_password"].result
  sensitive = true
}

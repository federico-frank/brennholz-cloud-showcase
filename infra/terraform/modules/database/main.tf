resource "aws_db_subnet_group" "main" {
  name       = "${var.name}-db"
  subnet_ids = var.subnet_ids
}

# PostgreSQL 16 wie lokal (postgis/postgis:16); die App legt PostGIS beim Start an (schema.sql, Master-User darf das)
resource "aws_db_instance" "main" {
  identifier     = "${var.name}-db"
  engine         = "postgres"
  engine_version = "16" # Major-Version; AWS wählt die aktuelle Minor-Version
  instance_class = var.instance_class

  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true # AES-256 über AWS KMS

  db_name  = "brennholz"
  username = "brennholz"
  password = var.password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = var.security_group_ids
  publicly_accessible    = false
  multi_az               = false

  backup_retention_period    = 1
  auto_minor_version_upgrade = true
  apply_immediately          = true

  # Demo: schneller Abbau ohne Abschluss-Snapshot
  skip_final_snapshot = true
  deletion_protection = false
}

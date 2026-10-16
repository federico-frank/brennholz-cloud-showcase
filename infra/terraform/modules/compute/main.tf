# Eine EC2 (Graviton) mit Docker Compose: App, RabbitMQ, Prometheus, Grafana, Caddy (TLS + noindex).
# Kein SSH-Port: Zugriff über SSM Session Manager. Zugangsdaten über die IAM-Rolle, nie auf der Platte im Image.

data "aws_ssm_parameter" "al2023_arm64" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"
}

resource "aws_eip" "app" {
  domain = "vpc"
  tags   = { Name = "${var.name}-eip" }
}

locals {
  # Ohne eigene Domain: <ip>.sslip.io zeigt auf die Elastic IP – Let's Encrypt funktioniert damit
  sslip_host = "${replace(aws_eip.app.public_ip, ".", "-")}.sslip.io"
  site_host  = var.domain_name != "" ? var.domain_name : local.sslip_host
  # Leiten dauerhaft auf site_host um: www und – mit eigener Domain – die alte sslip-Adresse (alte Links bleiben gültig)
  redirect_hosts = var.domain_name != "" ? "www.${var.domain_name}, ${local.sslip_host}" : "www.${local.sslip_host}"
}

# --- IAM: genau die Rechte, die die Instanz braucht ---

data "aws_iam_policy_document" "assume_ec2" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "app" {
  name               = "${var.name}-ec2"
  assume_role_policy = data.aws_iam_policy_document.assume_ec2.json
}

resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ecr_read" {
  role       = aws_iam_role.app.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "app" {
  statement {
    sid       = "LogosLesenSchreibenLoeschen"
    actions   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
    resources = ["${var.images_bucket_arn}/*"]
  }

  statement {
    sid       = "LogosBucket"
    actions   = ["s3:ListBucket"]
    resources = [var.images_bucket_arn]
  }

  statement {
    sid       = "DeployKonfigurationLesen"
    actions   = ["s3:GetObject", "s3:ListBucket"]
    resources = [var.deploy_bucket_arn, "${var.deploy_bucket_arn}/*"]
  }

  statement {
    sid       = "SecretsLesen"
    actions   = ["ssm:GetParameter", "ssm:GetParameters"]
    resources = ["arn:aws:ssm:${var.aws_region}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_prefix}/*"]
  }
}

resource "aws_iam_role_policy" "app" {
  name   = "${var.name}-app"
  role   = aws_iam_role.app.id
  policy = data.aws_iam_policy_document.app.json
}

resource "aws_iam_instance_profile" "app" {
  name = "${var.name}-ec2"
  role = aws_iam_role.app.name
}

# --- Instanz ---

resource "aws_instance" "app" {
  ami                    = data.aws_ssm_parameter.al2023_arm64.value
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = var.security_group_ids
  iam_instance_profile   = aws_iam_instance_profile.app.name

  # t4g ist standardmäßig "unlimited" – kann bei Dauerlast Zusatzkosten erzeugen
  credit_specification {
    cpu_credits = "standard"
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required" # nur IMDSv2
    http_put_response_hop_limit = 2          # Container erreichen die Instanz-Rolle (ein Hop durch Docker)
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = 20
    encrypted   = true
  }

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    region          = var.aws_region
    deploy_bucket   = var.deploy_bucket_name
    images_bucket   = var.images_bucket_name
    ecr_url         = var.ecr_repository_url
    ssm_prefix      = var.ssm_prefix
    db_host         = var.db_host
    db_name         = var.db_name
    db_username     = var.db_username
    admin_email     = var.admin_email
    compose_version = var.compose_version
  })

  tags = {
    Name         = "${var.name}-app"
    DeployScript = var.deploy_script_etag
  }

  lifecycle {
    # Neue AMI-Versionen ersetzen die Instanz nicht ungefragt; user_data läuft nur beim Erststart –
    # spätere Konfiguration kommt über S3 (deploy-Bucket) und SSM, nicht über einen Neuaufbau
    ignore_changes = [ami, user_data]
  }
}

# Hostname im Parameter Store statt im Startskript: Ein Domain-Wechsel ist so nur ein Redeployment,
# kein Neuaufbau der Instanz (user_data_replace_on_change)
resource "aws_ssm_parameter" "site_host" {
  name  = "${var.ssm_prefix}/site_host"
  type  = "String"
  value = local.site_host
}

resource "aws_ssm_parameter" "redirect_hosts" {
  name  = "${var.ssm_prefix}/redirect_hosts"
  type  = "String"
  value = local.redirect_hosts
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}

# Fällt die Hardware aus, startet AWS die Instanz automatisch neu (gleiche IP, gleiche Platte)
resource "aws_cloudwatch_metric_alarm" "recover" {
  alarm_name          = "${var.name}-auto-recover"
  namespace           = "AWS/EC2"
  metric_name         = "StatusCheckFailed_System"
  statistic           = "Maximum"
  period              = 60
  evaluation_periods  = 2
  comparison_operator = "GreaterThanThreshold"
  threshold           = 0
  dimensions          = { InstanceId = aws_instance.app.id }
  alarm_actions       = ["arn:aws:automate:${var.aws_region}:ec2:recover"]
}

# Zwei private Buckets:
#  - images: Logos der Betriebe (ADR-0003/0004), nur die App liest und schreibt, ausgeliefert über /bilder/{key}
#  - deploy: Compose-Datei, Caddyfile, Prometheus- und Grafana-Konfiguration für die EC2

resource "random_id" "suffix" {
  byte_length = 3 # Bucket-Namen sind weltweit eindeutig
}

locals {
  buckets = {
    images = "${var.name}-images-${random_id.suffix.hex}"
    deploy = "${var.name}-deploy-${random_id.suffix.hex}"
  }
}

resource "aws_s3_bucket" "this" {
  for_each = local.buckets
  bucket   = each.value

  # Demo: terraform destroy darf volle Buckets löschen
  force_destroy = true
}

resource "aws_s3_bucket_ownership_controls" "this" {
  for_each = aws_s3_bucket.this
  bucket   = each.value.id

  rule {
    object_ownership = "BucketOwnerEnforced" # keine ACLs
  }
}

resource "aws_s3_bucket_public_access_block" "this" {
  for_each                = aws_s3_bucket.this
  bucket                  = each.value.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Serverseitige AES-256-Verschlüsselung (SSE-S3); die App setzt sie zusätzlich je Objekt
resource "aws_s3_bucket_server_side_encryption_configuration" "this" {
  for_each = aws_s3_bucket.this
  bucket   = each.value.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "images" {
  bucket = aws_s3_bucket.this["images"].id

  rule {
    id     = "abgebrochene-uploads-aufraeumen"
    status = "Enabled"
    filter {}
    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}

# Nur verschlüsselte Verbindungen
data "aws_iam_policy_document" "tls_only" {
  for_each = aws_s3_bucket.this

  statement {
    sid       = "DenyInsecureTransport"
    effect    = "Deny"
    actions   = ["s3:*"]
    resources = [each.value.arn, "${each.value.arn}/*"]

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "tls_only" {
  for_each = aws_s3_bucket.this
  bucket   = each.value.id
  policy   = data.aws_iam_policy_document.tls_only[each.key].json

  depends_on = [aws_s3_bucket_public_access_block.this]
}

# --- Deploy-Konfiguration hochladen; Änderungen rollt infra/scripts/release.sh aus ---

resource "aws_s3_object" "deploy" {
  for_each = fileset(var.deploy_dir, "**")

  bucket = aws_s3_bucket.this["deploy"].id
  key    = each.value
  source = "${var.deploy_dir}/${each.value}"
  etag   = filemd5("${var.deploy_dir}/${each.value}")
}

resource "aws_s3_object" "grafana" {
  for_each = fileset(var.grafana_dir, "**")

  bucket = aws_s3_bucket.this["deploy"].id
  key    = "grafana/${each.value}"
  source = "${var.grafana_dir}/${each.value}"
  etag   = filemd5("${var.grafana_dir}/${each.value}")
}

#!/usr/bin/env bash
# Release vom eigenen Rechner: App-Image bauen (arm64), nach ECR pushen, auf der EC2 ausrollen.
# Voraussetzung: terraform apply in infra/terraform ist gelaufen, AWS-CLI ist angemeldet.
set -euo pipefail
cd "$(dirname "$0")/../.."

tf() { terraform -chdir=infra/terraform output -raw "$1"; }
REGION=$(tf aws_region)
ECR=$(tf ecr_repository_url)
INSTANCE=$(tf instance_id)
PREFIX=$(tf ssm_prefix)

# Tags sind unveränderlich: Commit-Hash, bei lokalen Änderungen mit Zeitstempel
TAG=$(git rev-parse --short HEAD)
git diff --quiet HEAD || TAG="$TAG-dirty-$(date +%Y%m%d%H%M)"

echo "==> Image $TAG bauen und pushen"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "${ECR%%/*}"
docker build --platform linux/arm64 -t "$ECR:$TAG" .
docker push "$ECR:$TAG"
aws ssm put-parameter --region "$REGION" --name "$PREFIX/app_image_tag" --value "$TAG" --type String --overwrite >/dev/null

echo "==> Auf $INSTANCE ausrollen"
CMD=$(aws ssm send-command --region "$REGION" --instance-ids "$INSTANCE" \
  --document-name AWS-RunShellScript --comment "release $TAG" \
  --parameters 'commands=["/opt/brennholz/deploy.sh"]' \
  --query Command.CommandId --output text)
aws ssm wait command-executed --region "$REGION" --command-id "$CMD" --instance-id "$INSTANCE" || true
aws ssm get-command-invocation --region "$REGION" --command-id "$CMD" --instance-id "$INSTANCE" \
  --query '[Status, StandardOutputContent, StandardErrorContent]' --output text
echo "==> $(tf site_url)"

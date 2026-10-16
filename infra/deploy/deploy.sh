#!/bin/bash
# Läuft auf der EC2 (Erststart über user_data, danach per SSM aus infra/scripts/release.sh):
# Konfiguration aus S3 holen, Secrets aus SSM in .env schreiben, Images ziehen, Stack starten.
set -euo pipefail
cd /opt/brennholz
source ./instance.env

aws s3 sync "s3://$DEPLOY_BUCKET/" . --delete --region "$REGION" \
  --exclude "instance.env" --exclude ".env" --exclude "deploy.sh"

param() {
  aws ssm get-parameter --region "$REGION" --with-decryption --name "$SSM_PREFIX/$1" \
    --query Parameter.Value --output text
}

TAG=$(param app_image_tag)
SITE_HOST=$(param site_host)
REDIRECT_HOSTS=$(param redirect_hosts)
if [ "$TAG" = "none" ]; then
  echo "Noch kein App-Image veröffentlicht – infra/scripts/release.sh ausführen."
  exit 0
fi

umask 077
cat > .env <<EOF
APP_IMAGE=$ECR_URL:$TAG
SITE_HOST=$SITE_HOST
REDIRECT_HOSTS=$REDIRECT_HOSTS
DB_HOST=$DB_HOST
DB_NAME=$DB_NAME
DB_USER=$DB_USER
DB_PASS=$(param db_password)
RABBITMQ_PASS=$(param rabbitmq_password)
AWS_REGION=$REGION
S3_BUCKET_NAME=$IMAGES_BUCKET
ENCRYPTION_KEY=$(param encryption_key)
ADMIN_EMAIL=$ADMIN_EMAIL
ADMIN_INITIAL_PASSWORD=$(param admin_initial_password)
GRAFANA_ADMIN_PASSWORD=$(param grafana_admin_password)
EOF

aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "${ECR_URL%%/*}"
docker compose pull --quiet
docker compose up -d --remove-orphans
# Einzeldatei-Mounts (Caddyfile, prometheus.yml) zeigen nach dem Sync sonst auf die alte Datei
docker compose restart caddy prometheus
docker image prune -f

# Sich selbst zuletzt aktualisieren (mv ersetzt die Datei, die laufende Shell liest die alte weiter)
aws s3 cp "s3://$DEPLOY_BUCKET/deploy.sh" deploy.sh.new --region "$REGION" --quiet
chmod +x deploy.sh.new && mv deploy.sh.new deploy.sh
echo "Deployment $TAG gestartet: https://$SITE_HOST"

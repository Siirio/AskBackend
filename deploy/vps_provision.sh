#!/bin/bash
set -e

VPS_PASSWORD="${ASK_VPS_PASSWORD:?ASK_VPS_PASSWORD must be set}"

echo "$VPS_PASSWORD" | sudo -S apt-get update -y
sudo apt-get install -y openjdk-21-jdk postgresql nginx ufw
java -version
nginx -v
psql --version
sudo mkdir -p /srv/ask.kz/app
sudo mkdir -p /etc/nginx/ssl
echo "PROVISION_BASE_DONE"

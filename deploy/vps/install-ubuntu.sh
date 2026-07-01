#!/bin/sh
set -eu

sudo apt-get update
sudo apt-get install -y ca-certificates curl git ufw docker.io docker-compose-v2
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
docker --version
docker compose version

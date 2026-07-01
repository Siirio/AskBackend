#!/bin/bash
set -e

VPS_HOST="${ASK_VPS_HOST:?ASK_VPS_HOST must be set}"
DB_USER="${ASK_DB_USERNAME:?ASK_DB_USERNAME must be set}"
DB_PASSWORD="${ASK_DB_PASSWORD:?ASK_DB_PASSWORD must be set}"
JWT_SECRET="${AUTH_JWT_SECRET:?AUTH_JWT_SECRET must be set}"

sudo openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout /etc/nginx/ssl/ask.key -out /etc/nginx/ssl/ask.crt -days 365 \
  -subj "/CN=$VPS_HOST"

sudo tee /etc/systemd/system/ask-backend.service > /dev/null <<EOF
[Unit]
Description=Ask Backend (Spring Boot)
After=network.target postgresql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/srv/ask.kz/app
ExecStart=/usr/bin/java -jar /srv/ask.kz/app/ask-backend.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=5

Environment=ASK_DB_URL=jdbc:postgresql://localhost:5432/ask
Environment=ASK_DB_USERNAME=$DB_USER
Environment=ASK_DB_PASSWORD=$DB_PASSWORD
Environment=ASK_SERVER_PORT=8080
Environment=AUTH_JWT_SECRET=$JWT_SECRET
Environment=AUTH_VERIFICATION_EMAIL_ENABLED=\${AUTH_VERIFICATION_EMAIL_ENABLED:-false}
Environment=AUTH_VERIFICATION_SMS_ENABLED=\${AUTH_VERIFICATION_SMS_ENABLED:-false}
Environment=ASK_MAIL_HOST=\${ASK_MAIL_HOST:-smtp.resend.com}
Environment=ASK_MAIL_PORT=\${ASK_MAIL_PORT:-587}
Environment=ASK_MAIL_USERNAME=\${ASK_MAIL_USERNAME:-resend}
Environment=ASK_MAIL_PASSWORD=\${ASK_MAIL_PASSWORD:-}
Environment=AUTH_EMAIL_FROM=\${AUTH_EMAIL_FROM:-noreply@ask.kz}

[Install]
WantedBy=multi-user.target
EOF

sudo tee /etc/nginx/sites-available/ask-backend > /dev/null <<NGINX
server {
    listen 8443 ssl;
    listen [::]:8443 ssl;
    server_name $VPS_HOST;

    ssl_certificate     /etc/nginx/ssl/ask.crt;
    ssl_certificate_key /etc/nginx/ssl/ask.key;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
NGINX

sudo ln -sf /etc/nginx/sites-available/ask-backend /etc/nginx/sites-enabled/ask-backend
sudo nginx -t

sudo ufw allow 8443/tcp || true

sudo systemctl daemon-reload
sudo systemctl enable ask-backend
sudo systemctl restart ask-backend
sudo systemctl restart nginx

echo "DEPLOY_DONE"

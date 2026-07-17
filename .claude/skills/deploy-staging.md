---
name: deploy-staging
description: Deploy AskBackend + AskFrontend dev branches to staging. Use when the user says "deploy to staging", "deploy to VPS", "update staging", "push to staging", "deploy dev", "staging deploy", or wants to ship the latest dev code to the staging environment. Commits uncommitted changes, pushes to dev, builds JAR locally, SFTP-uploads to VPS, rebuilds Docker container, and triggers Vercel frontend deploy.
---

# Deploy Staging

Complete pipeline from local changes to live staging. Commits + pushes both repos to `dev`, then deploys backend via SFTP + Docker and frontend via Vercel auto-deploy on push.

## CRITICAL: Server identity

| Server | IP | User | Password | Used for |
|--------|-----|------|----------|----------|
| **ASK VPS** | **212.19.134.80** | choki_ai_deployer | 1234567890 | ASK prod + stage |
| Shared dev | 194.238.42.177 | ubuntu | in AskBackend/.env | timetrack, sales-bot, drama — NOT ASK |

**194.238.42.177 is NOT the ASK server.** Verify with: `dig +short api-stage.ask.com.kz` → must be 212.19.134.80.

## Architecture
- **Backend staging**: Docker container `app-stage` on 212.19.134.80, part of compose project at `/opt/ask/AskBackend/deploy/vps/`
- **Frontend staging**: Vercel (stage.ask.com.kz), auto-deploys on push to `dev` branch
- **Caddy routes**: api.ask.com.kz → app-prod:8080, api-stage.ask.com.kz → app-stage:8080
- **DB**: db-stage (PostgreSQL, database ask_stage) — separate from db-prod

## Pipeline (execute in order)

### Step 0: Pre-flight — no Co-Authored-By

Check last 10 commits on BOTH repos for `Co-Authored-By` trailers. If found → STOP, tell user to rebase them out.

```bash
git -C AskBackend log --oneline -10 --format="%H %s%(trailers:key=Co-Authored-By,valueonly)"
git -C AskFrontend log --oneline -10 --format="%H %s%(trailers:key=Co-Authored-By,valueonly)"
```

### Step 1: Commit and push both repos

```bash
# AskBackend
cd AskBackend
git add -A
git reset HEAD .env   # NEVER commit .env — it has secrets
git commit -m "<descriptive message>"
git push origin dev

# AskFrontend
cd AskFrontend
git add -A
git commit -m "<descriptive message>"
git push origin dev
```

**Vercel auto-deploys** on push to `dev`. The frontend is live at stage.ask.com.kz within ~2 minutes.

**Never** include `Co-Authored-By` in commit messages. **Never** commit `.env` files.

### Step 2: Build backend JAR locally

**Never run Maven on the VPS** — it will exhaust CPU/memory.

```bash
cd AskBackend && mvn package -DskipTests -q
```

JAR lands at `AskBackend/target/ask-backend-*.jar` (~73MB).

### Step 3: Create deploy package

Create a tarball with the pre-built JAR and a slim Dockerfile (no Maven stage):

```bash
JAR=$(ls AskBackend/target/ask-backend-*.jar | head -1)
TARBALL=/tmp/ask-stage-deploy.tar.gz

cp "$JAR" /tmp/ask-stage-app.jar

cat > /tmp/ask-stage-Dockerfile << 'EOF'
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY ask-stage-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

cd /tmp && tar czf "$TARBALL" ask-stage-app.jar ask-stage-Dockerfile
```

### Step 4: SFTP upload to VPS

Use `paramiko` directly from Python (the `vps_upload.py` script uses env vars pointing to the wrong server). 66MB tarball takes ~2-3 minutes.

```python
import paramiko, os

local = os.path.expandvars(r"${LOCALAPPDATA}\Temp\ask-stage-deploy.tar.gz")
# On msys2/git-bash: cygpath -w /tmp/ask-stage-deploy.tar.gz

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect("212.19.134.80", username="choki_ai_deployer", password="1234567890", timeout=60)

transport = client.get_transport()
sftp = paramiko.SFTPClient.from_transport(transport)
sftp.put(local, "/opt/ask/ask-stage-deploy.tar.gz")
sftp.close()
client.close()
```

### Step 5: Extract, set OAuth env vars, rebuild on VPS

SSH to 212.19.134.80 (bash via `vps_ssh.py` or paramiko):

```bash
# Extract tarball
cd /opt/ask
tar xzf ask-stage-deploy.tar.gz
cp ask-stage-Dockerfile AskBackend/Dockerfile.stage-light
cp ask-stage-app.jar AskBackend/app-stage.jar
rm ask-stage-deploy.tar.gz ask-stage-Dockerfile ask-stage-app.jar

# Set Google OAuth env vars for staging in deploy/vps/.env
# OAUTH2_STAGE_GOOGLE_CLIENT_ID=...
# OAUTH2_STAGE_GOOGLE_CLIENT_SECRET=...
# OAUTH2_FRONTEND_REDIRECT_URI=https://stage.ask.com.kz/oauth/callback

# Rebuild and restart app-stage
cd /opt/ask/AskBackend
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml up -d --build app-stage
```

The `Dockerfile.stage-light` is NOT committed — it's only used for staging deploy to avoid the heavy multi-stage Maven build on the VPS. The committed `Dockerfile` in the repo still uses Maven (suitable for CI).

### Step 6: Health check

```bash
# Container status
docker ps --format "table {{.Names}}\t{{.Status}}" | grep app-stage

# Direct backend check (inside VPS)
curl -s http://localhost:8080/api/v1/search | head -c 200

# Through Caddy (public URL)
curl -s https://api-stage.ask.com.kz/api/v1/search | head -c 200
```

### Step 7: Vercel frontend

Frontend is deployed via Vercel auto-deploy on push to `dev` (done in Step 1). Verify:

```bash
curl -s https://stage.ask.com.kz | head -c 200
```

If auto-deploy didn't trigger, use `vercel deploy` in AskFrontend or trigger manually via Vercel dashboard.

## Credentials source

Primary credentials for the ASK VPS:
- Host: 212.19.134.80
- User: choki_ai_deployer
- Password: 1234567890

The `.env` file at `AskBackend/.env` has `ASK_VPS_HOST=194.238.42.177` — that's the SHARED DEV server, NOT the ASK staging server. Do NOT use `vps_upload.py` or `vps_ssh.py` scripts that read those env vars for ASK deploys — use paramiko directly with the correct credentials.

## Required VPS env vars

In `/opt/ask/AskBackend/deploy/vps/.env` on the VPS (must exist before docker compose):

```
ASK_STAGE_DB_PASSWORD=<...>
AUTH_STAGE_JWT_SECRET=<...>
AUTH_STAGE_STAFF_TEMP_PASSWORD_KEY=<...>
MEILI_STAGE_MASTER_KEY=<...>
OAUTH2_STAGE_GOOGLE_CLIENT_ID=<...>
OAUTH2_STAGE_GOOGLE_CLIENT_SECRET=<...>
DEEPSEEK_API_KEY=<...>
```

## Failure recovery

- **Wrong server (194.238.42.177)**: This is the shared dev server. Stop immediately. Use 212.19.134.80.
- **VPS overload during build**: Never run Maven on the VPS. Pre-build JAR locally.
- **SSH timeout during build**: Use nohup + log polling: `nohup docker compose ... up -d --build app-stage > /opt/ask/build.log 2>&1 &`
- **Disk full**: `docker system prune -f` on VPS.
- **DB connection refused**: Check db-stage healthcheck passed before app-stage starts.
- **SFTP upload hangs**: VPS disk might be full. SSH in and `df -h` first.
- **Caddy not routing**: Check `docker logs ask-vps-caddy-1` for TLS errors.
- **Vercel not deploying**: Check GitHub → Vercel integration, or use `vercel deploy` manually.

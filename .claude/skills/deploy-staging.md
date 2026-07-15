---
name: deploy-staging
description: Deploy AskBackend + AskFrontend dev branches to staging. Use when the user says "deploy to staging", "deploy to VPS", "update staging", "push to staging", "deploy dev", "staging deploy", or wants to ship the latest dev code to the staging environment. Covers pre-flight checks (no Co-Authored-By), local build, upload, and Docker restart.
---

# Deploy Staging

Deploys current `dev` branches of AskBackend and AskFrontend to staging.

## CRITICAL: Server identity

| Server | IP | User | Password | Used for |
|--------|-----|------|----------|----------|
| **ASK VPS** | **212.19.134.80** | choki_ai_deployer | 1234567890 | ASK prod + stage |
| Shared dev | 194.238.42.177 | ubuntu | in AskBackend/.env | timetrack, sales-bot, drama — NOT ASK |

**194.238.42.177 is NOT the ASK server.** Do not connect there for ASK deploys. Verify with: `dig +short api-stage.ask.com.kz` → must be 212.19.134.80.

## Architecture
- **Backend staging**: Docker container `app-stage` on 212.19.134.80, part of compose project "vps" at `/opt/ask/AskBackend/deploy/vps/`
- **Frontend staging**: Vercel (stage.ask.com.kz), NOT on the VPS
- **Caddy routes**: api.ask.com.kz → app-prod:8080, api-stage.ask.com.kz → app-stage:8080
- **DB**: db-stage (PostgreSQL, database ask_stage) — separate from db-prod

## Pipeline (execute in order)

### Step 0: Pre-flight — no Co-Authored-By

Check last 10 commits on BOTH repos for `Co-Authored-By` trailers. If found → STOP, tell user to rebase them out.

```bash
git -C AskBackend log --oneline -10 --format="%H %s%(trailers:key=Co-Authored-By,valueonly)"
git -C AskFrontend log --oneline -10 --format="%H %s%(trailers:key=Co-Authored-By,valueonly)"
```

### Step 1: Build backend JAR locally

**Never run Maven on the VPS** — it will exhaust CPU/memory on the already-loaded server.

```bash
cd AskBackend && mvn package -DskipTests -q
```

JAR lands at `AskBackend/target/ask-backend-*.jar` (~70MB).

### Step 2: Create deploy package

Create a lightweight tarball with the pre-built JAR and a slim Dockerfile (no Maven stage):

```bash
cd AskBackend
cp target/ask-backend-*.jar /tmp/ask-stage-app.jar
```

Write `/tmp/ask-stage-Dockerfile`:
```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY ask-stage-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
cd /tmp && tar czf ask-stage-deploy.tar.gz ask-stage-app.jar ask-stage-Dockerfile
```

### Step 3: Upload to VPS

SFTP upload to **212.19.134.80** as **choki_ai_deployer** (password: 1234567890):

Upload `ask-stage-deploy.tar.gz` to `/opt/ask/`.

### Step 4: Extract and rebuild on VPS

SSH to 212.19.134.80:
```bash
cd /opt/ask
tar xzf ask-stage-deploy.tar.gz
cp ask-stage-Dockerfile AskBackend/Dockerfile.stage-light
cp ask-stage-app.jar AskBackend/app-stage.jar
rm ask-stage-deploy.tar.gz ask-stage-Dockerfile ask-stage-app.jar
```

Temporarily update compose to use the light Dockerfile, then:
```bash
cd /opt/ask/AskBackend
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml up -d --build app-stage
```

Or if the existing compose already works (after code update):
```bash
cd /opt/ask/AskBackend
docker compose --env-file deploy/vps/.env -f deploy/vps/compose.yml up -d --build app-stage
```

Use `nohup ... > /opt/ask/build.log 2>&1 &` for long builds and poll the log.

### Step 5: Health check

```bash
# Check container is up
docker ps --format "table {{.Names}}\t{{.Status}}" | grep app-stage

# Check backend health
curl -s http://localhost:8080/api/v1/search

# Check through Caddy
curl -s https://api-stage.ask.com.kz/api/v1/search
```

### Step 6: Frontend (Vercel)

Frontend staging is on Vercel at stage.ask.com.kz. Deploy via Vercel CLI or push to the branch linked to Vercel staging.

## Credentials source

Primary credentials for the ASK VPS:
- Host: 212.19.134.80
- User: choki_ai_deployer
- Password: 1234567890

The `.env` file at `AskBackend/.env` has `ASK_VPS_HOST=194.238.42.177` — that's the SHARED DEV server, NOT the ASK staging server. Do NOT use it for ASK deploys.

## Failure recovery

- **Wrong server (194.238.42.177)**: This is the shared dev server. Stop immediately. Use 212.19.134.80.
- **VPS overload during build**: Never run Maven on the VPS. Pre-build JAR locally.
- **SSH timeout during build**: Use nohup + log polling.
- **Disk full**: `docker system prune -f` on VPS.
- **DB connection refused**: Check db-stage healthcheck passed before app-stage starts.

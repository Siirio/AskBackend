# Ask VPS deployment

Target VPS:

- Ubuntu 24.04
- Public IP: `212.19.134.80`
- Frontend: Vercel
- Backend and PostgreSQL: VPS

DNS split:

| Host | Target |
| --- | --- |
| `ask.com.kz` | Vercel production frontend |
| `stage.ask.com.kz` | Vercel stage frontend |
| `api.ask.com.kz` | `212.19.134.80` |
| `api-stage.ask.com.kz` | `212.19.134.80` |

Do not point `ask.com.kz` or `stage.ask.com.kz` to the VPS when frontend is served by Vercel.

## First server setup

Run on the VPS:

```sh
cd /opt/ask/AskBackend/deploy/vps
chmod +x install-ubuntu.sh
./install-ubuntu.sh
```

Log out and log in again after the Docker group is added.

## Environment

Create the env file on the VPS:

```sh
cd /opt/ask/AskBackend/deploy/vps
cp env.example .env
nano .env
```

Fill every empty secret before starting services.

Build only from a clean checkout of the intended commit. Set `BUILD_STAGE_SHA` to the exact
backend `dev` commit deployed to staging and `BUILD_PROD_SHA` to the exact backend `master`
commit deployed to production. Compose tags each backend image independently and the
application exposes the selected value through `/actuator/info`.

The server-local `.env` is never committed. Back it up before every deployment, keep mode
`600`, and validate required values without printing secrets.

## Start or update

Build the Java 21 JAR locally and upload it as `/opt/ask/AskBackend/ask-stage-app.jar` or
`/opt/ask/AskBackend/ask-prod-app.jar`. The committed `Dockerfile.runtime` packages that
artifact without compiling source code on the VPS.

Run on the VPS from `AskBackend/deploy/vps`:

```sh
docker compose --env-file .env -f compose.yml up -d --build
```

For a normal release, target only the intended application:

```sh
docker compose --env-file .env -f compose.yml up -d --build app-stage
docker compose --env-file .env -f compose.yml up -d --build app-prod
```

Staging runs with mock six-digit verification (`test-mode=true`,
`staging-bypass=true`). Production fixes both flags to `false` and requires real mail
configuration. Deployment must not edit PostgreSQL data, schema, volumes, or Flyway history.

Check services:

```sh
docker compose --env-file .env -f compose.yml ps
docker compose --env-file .env -f compose.yml logs -f app-prod
docker compose --env-file .env -f compose.yml logs -f app-stage
```

## Public URLs

- Production API: `https://api.ask.com.kz`
- Stage API: `https://api-stage.ask.com.kz`

Caddy obtains and renews HTTPS certificates automatically. Ports `80` and `443` must be open on the VPS, and DNS for both API hostnames must already point to `212.19.134.80`.

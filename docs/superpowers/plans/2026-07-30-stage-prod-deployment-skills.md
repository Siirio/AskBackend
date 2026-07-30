# Stage and Production Deployment Skills Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create verified stage and production deployment skills and promote the current ASK backend and frontend changes through staging and production.

**Architecture:** Two repository-local Codex skills own separate `dev` and `master` release workflows while reusing the existing chunked VPS upload tools and shared Compose stack. The Compose file provides environment-specific verification behavior, and every production action is gated by a successful staging deployment.

**Tech Stack:** Codex skills, Git, GitHub, Maven, Node.js, Vercel CLI, Python/Paramiko, Docker Compose, Spring Boot Actuator, Caddy

## Global Constraints

- Preserve every local and server `.env`; never commit it.
- Stage deploys only `dev`; production deploys only `master`.
- Stage accepts any syntactically valid six-digit code; production uses real verification.
- Production database access remains read-only during deployment.
- Do not proceed to production unless all staging checks pass.

---

### Task 1: Create the deployment skills

**Files:**
- Create: `.agents/skills/deploy-stage/SKILL.md`
- Create: `.agents/skills/deploy-stage/agents/openai.yaml`
- Create: `.agents/skills/deploy-prod/SKILL.md`
- Create: `.agents/skills/deploy-prod/agents/openai.yaml`
- Delete: `.claude/skills/deploy-staging.md`

**Interfaces:**
- Consumes: `tools/deploy/vps_run.py`, `tools/deploy/vps_upload.py`, the two Git repositories, and the shared VPS Compose stack.
- Produces: complete `deploy-stage` and `deploy-prod` workflows with branch, environment, verification, and rollback gates.

- [ ] **Step 1: Establish the failing baseline**

Run:

```powershell
Test-Path .agents/skills/deploy-stage/SKILL.md
Test-Path .agents/skills/deploy-prod/SKILL.md
```

Expected: both commands return `False`.

- [ ] **Step 2: Initialize both skills**

Run the system `skill-creator/scripts/init_skill.py` for `deploy-stage` and `deploy-prod`, targeting `.agents/skills`.

- [ ] **Step 3: Write the complete workflows**

Include the requested VPS connection fields, exact branch rules, safe environment handling, local build, resumable upload, Compose deployment, health/OAuth/SHA/Vercel checks, and explicit stop conditions.

- [ ] **Step 4: Validate both skills**

Run:

```powershell
python C:/Users/user/.codex/skills/.system/skill-creator/scripts/quick_validate.py .agents/skills/deploy-stage
python C:/Users/user/.codex/skills/.system/skill-creator/scripts/quick_validate.py .agents/skills/deploy-prod
rg -n "TODO|flyway_schema_history|docker system prune" .agents/skills
```

Expected: both validators pass and the forbidden-pattern scan has no matches.

### Task 2: Configure environment-specific verification

**Files:**
- Modify: `deploy/vps/compose.yml`
- Modify: `deploy/vps/env.example`
- Modify: `deploy/vps/README.md`
- Modify: `AI_Knowledge/features/identity/README.md`
- Modify: `AI_Knowledge/Changelog.md`

**Interfaces:**
- Consumes: Spring configuration keys `AUTH_VERIFICATION_TEST_MODE` and `AUTH_VERIFICATION_STAGING_BYPASS`.
- Produces: stage runtime values `true/true` and production runtime values `false/false`.

- [ ] **Step 1: Record the failing configuration check**

Run:

```powershell
rg -n -A 35 "app-stage:" deploy/vps/compose.yml
```

Expected: the stage service currently sets both verification flags to `false`.

- [ ] **Step 2: Update the Compose environment**

Set both stage flags to `true` and leave both production flags `false`.

- [ ] **Step 3: Update the environment contract and documentation**

Remove obsolete verification variables from `env.example` and document that behavior is fixed per service in Compose.

- [ ] **Step 4: Validate the Compose source**

Run a YAML parse and assert the four exact flag values.

### Task 3: Verify and publish `dev`

**Files:**
- Commit all intended backend changes except `.env` and `uploads/business-media/`.
- Commit all intended frontend changes.

**Interfaces:**
- Consumes: current dirty `dev` worktrees.
- Produces: tested backend and frontend `origin/dev` commits.

- [ ] **Step 1: Review both diffs and exclusions**

Run `git status`, `git diff --check`, and inspect staged file lists in both repositories.

- [ ] **Step 2: Run backend verification**

Run the backend test suite and package the Java 21 JAR.

- [ ] **Step 3: Run frontend verification**

Run the frontend test suite and production build. Any pre-existing failure must be identified; a regression blocks deployment.

- [ ] **Step 4: Commit and push both `dev` branches**

Use short human commit messages. Verify `HEAD` equals `origin/dev` after each push.

### Task 4: Deploy and verify staging

**Files:**
- Server-local: `/opt/ask/AskBackend/deploy/vps/.env`
- Server-local: `/opt/ask/AskBackend/ask-stage-app.jar`
- Server-local: `/opt/ask/AskBackend/deploy/vps/compose.yml`

**Interfaces:**
- Consumes: backend and frontend `origin/dev` SHAs.
- Produces: healthy `app-stage` and verified Vercel staging deployment.

- [ ] **Step 1: Establish VPS access and back up the server environment**

Connect using the skill credentials, verify server identity, and create a timestamped mode-`600` `.env` backup without printing values.

- [ ] **Step 2: Validate stage environment keys**

Check every required stage key for presence and non-empty value. Upload only owner-provided missing values and never fabricate secrets.

- [ ] **Step 3: Upload the JAR and changed deployment configuration**

Use `vps_upload.py`, normalize line endings on text files, and preserve the previous JAR as a rollback artifact.

- [ ] **Step 4: Deploy only `app-stage`**

Validate Compose, rebuild `app-stage`, and wait for a healthy container.

- [ ] **Step 5: Verify stage**

Check health, backend SHA, HTTPS OAuth redirect, `true/true` verification flags, logs, staging frontend HTTP 200, and Vercel commit association.

### Task 5: Promote and deploy production

**Files:**
- Merge backend `dev` into `master`.
- Merge frontend `dev` into `master`.
- Server-local: `/opt/ask/AskBackend/ask-prod-app.jar`

**Interfaces:**
- Consumes: successful Task 4 evidence.
- Produces: matching `origin/master`, healthy `app-prod`, and verified production frontend.

- [ ] **Step 1: Merge and push both master branches**

Fetch, require that local `dev` equals `origin/dev`, update `master`, merge `dev`, and push.

- [ ] **Step 2: Validate the production environment**

Back up the server `.env`; verify all production secret keys and real mail settings without printing values.

- [ ] **Step 3: Upload and deploy only `app-prod`**

Build from backend `master`, upload the JAR, validate Compose, rebuild `app-prod`, and wait for health.

- [ ] **Step 4: Verify production**

Check health, backend SHA, HTTPS OAuth redirect, `false/false` verification flags, logs, production frontend HTTP 200, and Vercel commit association.

- [ ] **Step 5: Produce the release report**

Report backend and frontend SHAs for both branches, VPS runtime SHAs, Vercel deployment results, verification modes, tests, and any remaining operational issue.

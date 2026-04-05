# Workflow Online API

A **single-endpoint** ingress service built with **Spring Boot 4.0.3** and **JDK 21**. It exposes **`POST /api/workflow`**, persists rows in the shared **`WORKFLOW_RECORD`** table, and runs the same async enrichment / outbound pipeline as **[Workflow Operation API](https://github.com/yiurenma/workflow)**. Operation owns definitions and admin APIs; Online owns ingress only.

## Quick start

**Requirements:** JDK 21, Maven 3.9+

```bash
mvn clean install
mvn spring-boot:run
```

Runs at **`http://localhost:8080`**. Default `application.yml` uses the **same Neon PostgreSQL** URL, user, and password as [`yiurenma/workflow`](https://github.com/yiurenma/workflow/blob/main/src/main/resources/application.yml). Override with `SPRING_DATASOURCE_*` or a local `application-local.yml` when needed.

**Same machine as [workflow-operation-api](https://github.com/yiurenma/workflow-operation-api)?** Both default to port **8080**. Run one service on another port (see the Operation README example, e.g. Online on **8081**) and point **workflow-ui** at the matching `VITE_*_API_BASE` values.

### Confirm startup

1. Wait for **`Started WorkflowOnlineApplication`** and Tomcat on port **8080**.
2. Health (requires DB reachable):

```bash
curl -s http://localhost:8080/actuator/health
```

Expect `"status":"UP"`. First connection to Neon may take **~30–60s**.

## Key endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/workflow` | JSON or XML body; query params `confirmationNumber`, `applicationName`; optional `channelKind`; required header **`X-Request-Correlation-Id`**. |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/v3/api-docs` | OpenAPI 3.0 JSON |
| GET | `/redoc.html` | ReDoc |
| GET | `/actuator/health` | Health |
| GET | `/actuator/info` | Info |
| GET | `/actuator/metrics` | Metrics |

No other REST controllers are packaged in this artifact.

## Architecture

### Online execution flow

1. **Ingress** — Validates correlation id (duplicate detection), resolves **`WORKFLOW_ENTITY_SETTING`** by `applicationName`, encrypts the initial runtime payload, inserts **`WORKFLOW_RECORD`**.
2. **Pipeline** — Async dispatch walks **`WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING`**, then **`WORKFLOW_RULE_AND_TYPE_MAPPING`**, **`WORKFLOW_RULE`**, **`WORKFLOW_TYPE`**, then consumer / branch / HTTP steps and optional downstream calls.

```text
Client → POST /api/workflow → WorkflowOnlineController → dispatch services → PostgreSQL (shared with Operation)
```

### Shared database

- **Same instance as Operation** — keep **`spring.jpa.hibernate.ddl-auto`** at **`none`** (Online does not own schema migrations). Operation may use **`update`**; schema is the source of truth.
- Entities mirror Operation: `WorkflowEntitySetting`, `WorkflowRecord`, `WorkflowType`, `WorkflowEntityAndLinkingIdMapping`, `WorkflowRule`, `WorkflowRuleAndType`, etc.

### Encrypted runtime JSON

`WorkflowRuntimePayload` keeps historical **`@JsonProperty`** names inside ciphertext for compatibility with existing rows and JsonPath in the database. Do not rename those annotation values without a coordinated migration.

## Configuration

| Variable / key | Purpose |
|----------------|---------|
| `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD` | Same as Operation (`jdbc:p6spy:postgresql:…` if using P6Spy) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Default **`none`** |
| `TRUST_SAPI_URI`, `TRUST_SAPI_URI_AP`, … | Trust-token issuer bases (optional `DSP_SAPI_URI*` aliases still accepted) |
| `WORKFLOW_SAPI_URI*`, `WORKFLOW_RECORD_URI` | Optional downstream / read-only Feign mode |
| `WORKFLOW_INTERNAL_HOST_MARKER` | Host marker for internal URL template swap |
| `async.enrichInformation` / `async.dispatchChannels` | Async stages |
| `jks.*` | Keystore for field encryption (`SecureData`) |

Logbook, tracing, and OpenAPI exclusions follow the same patterns as the Operation API (`/actuator/*`, `/swagger-ui/*`, `/v3/api-docs/*`, `/redoc.html` excluded from request logs).

## Build, tests, and coverage

```bash
mvn clean verify
```

JaCoCo enforces **≥ 98%** instruction coverage on the **checked slice** (entry controller, trust-token helper, and shared utilities under test), with exclusions aligned to generated JPA / Feign plumbing — same idea as **`yiurenma/workflow`**. Use **`-Djacoco.skip=true`** only when you intentionally skip the gate.

## Deploy to Render (GitHub Actions + Deploy Hook)

Use this when the service is a **Render Web Service** built from this repo (e.g. **Docker** with root `Dockerfile`).

1. In **Render**: open the service → **Settings** → **Build & Deploy** → **Deploy Hook** → create a hook and copy the URL.
2. In **GitHub**: repo → **Settings** → **Secrets and variables** → **Actions** → New secret **`RENDER_DEPLOY_HOOK_URL`** (paste the hook URL).
3. Workflow **`.github/workflows/render-deploy.yml`** runs **`mvn test`**, then **`POST`**s the hook so Render pulls latest commit and rebuilds.

**Avoid double deploys:** if Render is set to **auto-deploy on push** *and* you use this workflow, each push may trigger **two** builds. Either disable auto-deploy on Render and rely on the hook only, or remove this workflow.

Set **Environment** variables for DB and Spring on the Render service (same names as locally: `SPRING_DATASOURCE_*`, etc.).

Local smoke build (same image Render uses): `docker build -t workflow-online-api:local .`

## Integration tests (PostgreSQL)

`*IT` classes run under the Maven **`integration`** profile (skipped by default).

**Prerequisites**

- Set **`IT_WORKFLOW_APPLICATION_NAME`** to an `application_name` that exists in **`WORKFLOW_ENTITY_SETTING`** (seed via Operation API).
- Optional: **`IT_WORKFLOW_CONFIRMATION_NUMBER`** (default `itest-confirmation`).

**Run**

```bash
export IT_WORKFLOW_APPLICATION_NAME='YOUR_EXISTING_APP'
mvn -Pintegration verify
```

Each test sends a new UUID in **`X-Request-Correlation-Id`**. Tests are **`@Transactional`** with rollback to avoid polluting the shared database.

## Maven coordinates

- `groupId`: **`${resolved.groupId}`** — default **`com.snails`**; override with env **`MAVEN_GROUP_ID`** (same pattern as Operation API).
- `artifactId`: **`workflow-online-api`**

## Reference repository

For entity shapes, schema expectations, and documentation tone, use **[yiurenma/workflow](https://github.com/yiurenma/workflow)**.

Optional **local copy inside this repo** (ignored by Git — see `.gitignore`):

```bash
git clone --depth 1 https://github.com/yiurenma/workflow.git reference-workflow
```

Open `reference-workflow/` beside this project in your IDE for side-by-side comparison.

See **`AGENTS.md`** for automation / IDE notes.

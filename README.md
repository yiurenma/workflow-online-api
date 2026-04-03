# Workflow Online API

A **single-endpoint** execution service built with **Spring Boot 4.0.3** and **JDK 21**. It exposes `POST /api/workflow`, persists rows in the shared **`WORKFLOW_RECORD`** table, and runs the async enrichment / outbound pipeline defined in the same PostgreSQL schema as **[Workflow Operation API](https://github.com/yiurenma/workflow)** (Operation owns definitions and admin APIs; Online runs ingress only).

## Quick start

**Requirements:** JDK 21, Maven 3.9+. Datasource defaults in `application.yml` match [yiurenma/workflow](https://github.com/yiurenma/workflow) (same Neon URL, username, password), so **`mvn spring-boot:run`** typically needs no extra env vars when the database is reachable. Override with `SPRING_DATASOURCE_*` or `application-local.yml` for other environments.

```bash
mvn -Djacoco.skip=true spring-boot:run
```

Service listens on **`http://localhost:8080`**. (`mvn test` / `mvn verify` only run automated tests; they **do not** prove the full web app + database stack starts — use the steps below.)

### How to confirm the application started

1. In the console, wait for: **`Started WorkflowOnlineApplication`** (and **`Tomcat started on port 8080`**).
2. Check health (needs DB reachable with the default datasource):

```bash
curl -s http://localhost:8080/actuator/health
```

You should see JSON containing **`"status":"UP"`**. If the database is unreachable, startup may fail during Hikari/JPA init or health may not be UP — fix network or `SPRING_DATASOURCE_*` first.

First boot may take **~30–60s** while the pool connects to Neon.

Optional: copy `application-local.yml.example` to `application-local.yml` only when you need a different database or rotated credentials.

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

1. **Ingress** — `POST /api/workflow` checks correlation id (duplicate detection), resolves **`WORKFLOW_ENTITY_SETTING`** by `applicationName`, encrypts the initial runtime payload, inserts **`WORKFLOW_RECORD`**.
2. **Pipeline** — Async dispatch reads **`WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING`** → **`WORKFLOW_RULE_AND_TYPE_MAPPING`** → **`WORKFLOW_RULE`** / **`WORKFLOW_TYPE`**, then consumers, branches, HTTP steps, and optional downstream calls (Feign / `RestTemplate`).

```text
Client → POST /api/workflow → WorkflowOnlineController → dispatch services → PostgreSQL (shared with Operation)
```

### Shared database

- **Same instance as Operation:** defaults in `application.yml` match [`workflow`’s `application.yml`](https://github.com/yiurenma/workflow/blob/main/src/main/resources/application.yml) (URL, `neondb_owner`, password). Use env vars to point elsewhere or after credential rotation.
- Hikari **`connection-timeout`** / **`initialization-fail-timeout`** default to **120s** so cold Neon / network latency is less likely to kill startup; tune with `SPRING_DATASOURCE_HIKARI_*_MS` if needed.
- Keep **`spring.jpa.hibernate.ddl-auto`** at **`none`** (Online does not own schema migrations).
- JPA entities align with Operation (`WorkflowEntitySetting`, `WorkflowRecord`, `WorkflowType`, `WorkflowEntityAndLinkingIdMapping`, rule bindings, etc.).

### Encrypted runtime JSON

`WorkflowRuntimePayload` uses Jackson **`@JsonProperty`** so ciphertext and JsonPath templates in the database keep **stable wire names** from earlier schema versions. Do not change those annotation values without a coordinated data migration.

## Configuration

| Variable / key | Purpose |
|----------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL, often `jdbc:p6spy:postgresql:...` (match Operation) |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | Credentials |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Default `none` |
| `DSP_SAPI_URI` (+ regional overrides) | Token service bases for IB2B trust token |
| `WORKFLOW_SAPI_URI`, `WORKFLOW_SAPI_URI_AP`, … | Optional downstream SAPI URI map |
| `WORKFLOW_RECORD_URI` | Feign record API when `read-only` datasource mode is enabled |
| `WORKFLOW_INTERNAL_HOST_MARKER` | Host substring used to switch to internal HTTP URL templates |
| `async.enrichInformation` / `async.dispatchChannels` | Async stage toggles |

See `src/main/resources/application.yml` for Actuator exposure, Logbook, tracing, and OpenAPI exclusions.

## Build, tests, and coverage

```bash
mvn clean verify
```

JaCoCo applies a modest instruction gate on this slice; use **`-Djacoco.skip=true`** for a reporting-only build if needed (same escape hatch as many Boot codebases).

## Integration tests (PostgreSQL)

`*IT` classes run only with the Maven **`integration`** profile (skipped by default).

**Prerequisites**

- Set **`IT_WORKFLOW_APPLICATION_NAME`** to an `application_name` that already exists in **`WORKFLOW_ENTITY_SETTING`** (seed via Operation). Datasource defaults match `application.yml` / workflow repo; set `SPRING_DATASOURCE_*` only if you use another database.
- Optional: **`IT_WORKFLOW_CONFIRMATION_NUMBER`** (default `itest-confirmation`).

**Run**

```bash
export IT_WORKFLOW_APPLICATION_NAME='YOUR_EXISTING_APP'

mvn -Pintegration verify
```

Each test sends a **new UUID** in **`X-Request-Correlation-Id`**. Tests are **`@Transactional`** so the default rollback avoids leaving rows in the shared database.

## Maven coordinates

- `groupId`: `${resolved.groupId}` — default **`com.snails`**; override with **`MAVEN_GROUP_ID`** (same pattern as yiurenma/workflow).
- `artifactId`: **`workflow-online-api`**

## Reference repository

For Operation API behaviour, entity shapes, and documentation patterns, clone **[yiurenma/workflow](https://github.com/yiurenma/workflow)** (a shallow clone is enough):

```bash
git clone --depth 1 https://github.com/yiurenma/workflow reference-workflow
```

See also **`AGENTS.md`** for Cursor / automation notes.

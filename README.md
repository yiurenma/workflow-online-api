# Workflow Online API

A **single-endpoint** execution service built with **Spring Boot 4.0.3** and **JDK 21**. It exposes `POST /api/workflow`, persists rows in the shared **`WORKFLOW_RECORD`** table, and runs the async dispatch pipeline defined in the same PostgreSQL schema as **[Workflow Operation API](https://github.com/yiurenma/workflow)** (Operation owns definitions and admin APIs; Online runs ingress only).

## Quick Start

**Requirements:** JDK 21, Maven 3.9+

```bash
mvn clean install
mvn spring-boot:run
```

Runs at `http://localhost:8080`.

Datasource defaults in `application.yml` match [yiurenma/workflow](https://github.com/yiurenma/workflow) (same Neon URL, username, password), so `mvn spring-boot:run` typically needs no extra env vars when the database is reachable.

## Key Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| **Online API** | | |
| POST | `/api/workflow` | Accept a workflow dispatch request and run the configured async pipeline |
| **Docs & Tools** | | |
| GET | `/redoc.html` | Interactive API documentation (ReDoc) |
| GET | `/swagger-ui.html` | Swagger UI endpoint |
| GET | `/v3/api-docs` | OpenAPI 3.0 JSON spec |
| GET | `/actuator/health` | Actuator health endpoint |
| GET | `/actuator/info` | Actuator info |
| GET | `/actuator/metrics` | Actuator metrics |

## Architecture

### Online API (Request Execution)

A single **online API** serves as the entry point for all incoming requests:

1. **Request Ingestion** — `POST /api/workflow` validates correlation ID (duplicate detection), resolves `WORKFLOW_ENTITY_SETTING` by `applicationName`
2. **Record Persistence** — Creates a `WORKFLOW_RECORD` with the initial runtime context
3. **Async Dispatch** — Reads `WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING` → `WORKFLOW_RULE_AND_TYPE_MAPPING` → `WORKFLOW_RULE` / `WORKFLOW_TYPE`, then executes matched rules and creates child dispatch records

### Flow

```
Client → POST /api/workflow → WorkflowOnlineController → Dispatch Services → PostgreSQL (shared with Operation)
```

### Shared Database

Both projects use the **same PostgreSQL database** (Neon hosted):

- **URL:** `jdbc:p6spy:postgresql://ep-frosty-bush-a5f8yx09-pooler.us-east-2.aws.neon.tech/neondb`
- **Username:** `neondb_owner`
- **Password:** `npg_iW3juwZGAI8K`

Shared tables:
- `WORKFLOW_ENTITY_SETTING` — Application configuration
- `WORKFLOW_ENTITY_AND_LINKING_ID_MAPPING` — Ordered workflow step links
- `WORKFLOW_RULE` — JSONPath rule definitions
- `WORKFLOW_TYPE` — Action/step type definitions
- `WORKFLOW_RULE_AND_TYPE_MAPPING` — Rule-to-type bindings
- `WORKFLOW_RECORD` — Execution records (written by Online API)

### Error Handling

Unified error codes following the same pattern as the Operation API:

| Code | Description |
|------|-------------|
| WF-400-000 | Bad request payload or parameters |
| WF-400-001 | Validation failed |
| WF-400-101 | No entity setting found for this application name |
| WF-400-102 | Unexpected payload or format in inbound data |
| WF-409-101 | Duplicate workflow request detected for this correlation ID |
| WF-409-102 | Duplicate retry request detected for this origin record |
| WF-500-000 | Internal server error |

## Build, Tests, and Coverage

```bash
# Unit tests only (with JaCoCo coverage check at 98%)
mvn clean verify

# Unit tests + integration tests
mvn clean verify -Pintegration
```

### Test Coverage

JaCoCo instruction coverage gate: **98%** (matching the Operation API standard).

Current coverage: **99.16%** (956 instructions, 8 missed).

### Integration Tests

Integration tests (`*IT.java`) run with an H2 in-memory database and verify the full Spring Boot context, MockMvc request handling, entity persistence, duplicate detection, and async dispatch pipeline.

```bash
mvn verify -Pintegration
```

## Maven Coordinates

- `groupId`: `com.snails` (override with `MAVEN_GROUP_ID` env var)
- `artifactId`: `workflow-online-api`

## Reference Repository

For Operation API (workflow management, entity setting CRUD, admin APIs):

```bash
git clone https://github.com/yiurenma/workflow
```

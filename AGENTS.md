# AGENTS.md

## Project overview

**Workflow Online API** — Spring Boot **4.0.3** / **JDK 21**. Single REST ingress (`POST /api/workflow`) against the **same PostgreSQL schema** as **[yiurenma/workflow](https://github.com/yiurenma/workflow)** (Operation API). See `README.md` for flow, endpoints, and integration-test env vars.

## Prerequisites

- **JDK 21**
- **Maven 3.9+**

## Commands

| Task | Command |
|------|---------|
| Build + unit tests + JaCoCo gate | `mvn clean verify` |
| Unit tests only | `mvn test` |
| Run locally | `mvn spring-boot:run` |
| Skip JaCoCo check | `mvn verify -Djacoco.skip=true` |
| Integration tests (needs DB + `IT_WORKFLOW_APPLICATION_NAME`) | `mvn -Pintegration verify` |

## Gotchas

- **PostgreSQL**: Default datasource matches Operation’s Neon URL in `application.yml`. If unreachable, override `SPRING_DATASOURCE_*` or use `application-local.yml` (see `application-local.yml.example`).
- **DDL**: Online uses **`spring.jpa.hibernate.ddl-auto: none`** so it does not mutate the shared schema.
- **JaCoCo**: `verify` applies an instruction coverage gate (see `pom.xml`); exclusions mirror the Operation API pattern for JPA/Feign-heavy packages.
- **Integration profile**: Failsafe runs `*IT` only when `-Pintegration` is set and **`IT_WORKFLOW_APPLICATION_NAME`** points at real `WORKFLOW_ENTITY_SETTING` data.
- **Swagger**: `http://localhost:8080/swagger-ui.html`, OpenAPI at `/v3/api-docs`, ReDoc at `/redoc.html`.

---

## Test Plan

> **Scope:** online-api (port 8081) — workflow execution pipeline.
> **Last verified:** 2026-04-06 — all E2E cases below passed.
> **Depends on:** operation-api running on port 8080, Neon DB reachable, keystore `jks/keystoredev.jks` valid.

### Unit tests (automated)

```bash
mvn test
```

### Manual E2E checklist (against live services)

Prerequisites:
- operation-api on 8080, online-api on 8081
- Target app exists in `WORKFLOW_ENTITY_SETTING` with at least one workflow step (non-empty, non-blank rule key)
- Required headers: `Content-Type: application/json`, `X-Request-Correlation-Id: <uuid>`

#### TC-12 Async Execution (ON-04)

App must have `asyncMode=true`. Set via:
```
PATCH http://localhost:8080/api/workflow/entity-setting?applicationName=<name>
{"asyncMode": true}
```

```
POST http://localhost:8081/api/workflow?applicationName=<name>&confirmationNumber=<conf>
X-Request-Correlation-Id: <unique-id>
{"messageInformation":[{"customerId":"C001"}]}
```

| Expected | Check |
|----------|-------|
| 200 returned immediately (fire-and-forget) | Response time < 500ms |
| Record created asynchronously | `GET /api/workflow/records?applicationName=<name>` — new row within ~5s |

#### TC-13 / TC-14 Sync Execution (ON-05)

App must have `asyncMode=false` (default).

| Expected | Check |
|----------|-------|
| 200 returned after pipeline completes | Record exists immediately after response |
| `overallStatus=GI_SUCCESS` | GET record by correlationId confirms |

#### TC-16 Duplicate Detection (ON-02)

Re-send same `X-Request-Correlation-Id` for same `applicationName`.

| Expected | Check |
|----------|-------|
| 400 `M0002` | `"Duplicate records has been found per request correlation ID <id>"` |

#### TC-N Negative Cases

| Scenario | Expected |
|----------|----------|
| Non-existent `applicationName` | 400 `M0001` |
| Missing `X-Request-Correlation-Id` header | 400 `440000` |
| Missing `confirmationNumber` param | 400 `440000` |

### Key types and their execution behavior

| Type | Behavior |
|------|----------|
| `CONSUMER` | HTTP call; result merged into runtime payload (enrichment phase). Failure → `GI_FAIL`. |
| `CONSUMERWITHOUTERROR` | Same as CONSUMER but exceptions are swallowed; execution continues. |
| `FUNCTION` / `FUNCTION_V2` / `FUNCTION_V3` | Java reflection call; enrichment phase. |
| `IFELSE` | Conditional branch on JSONPath rule match; enrichment phase. |
| `DISPATCH` / `MESSAGE` | Outbound HTTP call; dispatch phase (after enrichment). Creates child `WorkflowRecord`. |

### Known gotchas for agents

- **JCEKS keystore required** — `SecureData` uses `KeyStore.getInstance("JCEKS")` for AES-CBC encryption of `workflowTransactionDetails`. The bundled `jks/keystoredev.jks` must be generated with `-storetype JCEKS -keyalg AES`. A JKS-format keystore will cause `Invalid keystore format` on startup.
  ```bash
  keytool -genseckey -keystore src/main/resources/jks/keystoredev.jks \
    -storetype JCEKS -storepass changeit -keyalg AES -keysize 128 \
    -alias mykey -keypass changeit
  ```
- **Empty rule key → `GI_FAIL`** — `ruleAndTypesFullyMatch` evaluates each rule's key as a JSONPath expression. An empty key `""` causes `InvalidPathException` (not caught), which propagates to the `GI_FAIL` catch block. Workflows saved with `ruleList: []` will always fail enrichment.
- **`enabled` flag is not enforced** — The online-api does not check `WorkflowEntitySetting.enabled`. A disabled app still executes. Enforcement must happen upstream.
- **`asyncMode` controls dispatch path** — `WorkflowOnlineController` branches on `settings.get(0).isAsyncMode()`:  `true` → `@Async dispatchFromPersistedRecord`, `false` → `dispatchFromPersistedRecordSync`. Both paths call `runDispatchPipeline`.
- **Duplicate detection scope** — `findIdsByRequestCorrelationIdAndApplicationName` scopes the check to `(requestCorrelationId, applicationName)`. The same correlation ID can be reused for different applications.

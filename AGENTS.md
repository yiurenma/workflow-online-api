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

package com.workflow.common.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenUiConfiguration {
    @Bean
    public OpenAPI workflowOnlineOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Workflow Online API")
                                .description("""
                                        Single public HTTP entry: **POST /api/workflow** — validates request metadata,
                                        persists an execution record, and runs the configured async dispatch pipeline
                                        against the shared Workflow PostgreSQL schema.
                                        
                                        Correlation id: header **X-Request-Correlation-Id** (must be unique per application
                                        for duplicate detection).
                                        """)
                                .version("1.0.0")
                );
    }
}

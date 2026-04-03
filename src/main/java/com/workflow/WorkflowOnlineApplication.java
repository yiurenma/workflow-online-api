package com.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableEnversRepositories(basePackages = "com.workflow.dao.repository")
@EnableAsync
@EnableCaching
public class WorkflowOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowOnlineApplication.class, args);
    }
}

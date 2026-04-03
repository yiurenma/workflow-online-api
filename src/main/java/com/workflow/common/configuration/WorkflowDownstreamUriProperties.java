package com.workflow.common.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Data
@Configuration
@ConfigurationProperties(prefix = "workflow.sapi.root")
public class WorkflowDownstreamUriProperties {
    private HashMap<String, String> uriMap;
}

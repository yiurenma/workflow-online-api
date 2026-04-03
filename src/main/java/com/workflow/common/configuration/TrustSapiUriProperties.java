package com.workflow.common.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Data
@Configuration
@ConfigurationProperties(prefix = "trust.sapi.root")
public class TrustSapiUriProperties {

    private HashMap<String, String> uriMap;
}

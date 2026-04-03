package com.workflow.common.configuration.logging;

import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Keeps Tomcat from discarding request facades too early for some async paths.
 */
@Configuration(proxyBeanMethods = false)
public class TomcatConnectorTuningConfiguration {

    @Bean
    TomcatConnectorCustomizer disableFacadeDiscard() {
        return (connector) -> connector.setDiscardFacades(false);
    }
}

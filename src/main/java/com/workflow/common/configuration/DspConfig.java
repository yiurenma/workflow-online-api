package com.workflow.common.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;



@Data
@Configuration
@ConfigurationProperties(prefix = "dsp.sapi.root")
public class DspConfig {

    HashMap<String,String> uriMap;
}

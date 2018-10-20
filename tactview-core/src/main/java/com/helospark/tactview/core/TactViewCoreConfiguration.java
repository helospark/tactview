package com.helospark.tactview.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
public class TactViewCoreConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}

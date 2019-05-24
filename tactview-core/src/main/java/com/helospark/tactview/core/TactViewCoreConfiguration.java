package com.helospark.tactview.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
public class TactViewCoreConfiguration {

    @Bean
    public ObjectMapper simpleObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ObjectMapper prettyPrintingObjectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.enable(SerializationFeature.INDENT_OUTPUT);
        return result;
    }

    @Bean
    public ObjectMapper getterIgnoringObjectMapper() {
        ObjectMapper regularObjectMapper = new ObjectMapper();
        regularObjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        regularObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return regularObjectMapper;
    }

}

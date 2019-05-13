package com.helospark.tactview.core.it.util;

import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Import;
import com.helospark.lightdi.annotation.PropertySource;
import com.helospark.tactview.core.TactViewCoreConfiguration;

@Configuration
@PropertySource(value = "classpath:application-test.properties", order = -10)
@Import(TactViewCoreConfiguration.class)
public class TestContextConfiguration {

}

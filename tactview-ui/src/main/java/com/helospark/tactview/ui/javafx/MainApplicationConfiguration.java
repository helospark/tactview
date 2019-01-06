package com.helospark.tactview.ui.javafx;

import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Import;
import com.helospark.lightdi.annotation.PropertySource;
import com.helospark.tactview.core.TactViewCoreConfiguration;

@Configuration
@Import(TactViewCoreConfiguration.class)
@ComponentScan
@PropertySource("classpath:uisettings.properties")
public class MainApplicationConfiguration {

}

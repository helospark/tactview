package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;

@Configuration
public class ClassToDetailFileHolderConfiguration {

    @Bean
    public ClassToDetailFileHolder classToDetailFileHolder() {
        return new ClassToDetailFileHolder(List.of(
                "localization/clip-description.json",
                "localization/effect-description.json"));
    }

}

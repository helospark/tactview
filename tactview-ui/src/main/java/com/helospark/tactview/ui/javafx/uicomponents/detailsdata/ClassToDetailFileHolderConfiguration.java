package com.helospark.tactview.ui.javafx.uicomponents.detailsdata;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.localizeddetail.LocalizedDetailFileHolder;

@Configuration
public class ClassToDetailFileHolderConfiguration {

    @Bean
    public LocalizedDetailFileHolder localizedDetailFileHolder() {
        return new LocalizedDetailFileHolder(List.of(
                "localization/clip-description.json",
                "localization/effect-description.json"));
    }

}

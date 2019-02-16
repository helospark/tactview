package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.render.RenderDialogOpener;

@Configuration
public class DefaultProjectMenuItemConfiguration {
    public static final String PROJECT_ROOT = "_Project";

    @Bean
    @Order(2000)
    public MenuContribution renderContributionMenuItem(RenderDialogOpener renderService) {
        return new DefaultMenuContribution(List.of(PROJECT_ROOT, "_Render"), event -> renderService.render());
    }

}

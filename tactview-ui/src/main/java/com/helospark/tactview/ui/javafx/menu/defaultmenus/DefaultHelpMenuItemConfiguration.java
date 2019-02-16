package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.help.AboutDialogOpener;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;

@Configuration
public class DefaultHelpMenuItemConfiguration {
    public static final String HELP_ROOT = "_Help";

    @Bean
    @Order(5000)
    public MenuContribution aboutContributionMenuItem(AboutDialogOpener aboutDialogOpener) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, "About..."), e -> {
            aboutDialogOpener.openDialog();
        });
    }

}

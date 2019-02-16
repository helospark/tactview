package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;
import com.helospark.tactview.ui.javafx.save.UiSaveHandler;

import javafx.application.Platform;

@Configuration
public class DefaultFileMenuItemConfiguration {
    public static final String FILE_ROOT = "_File";

    @Bean
    @Order(0)
    public MenuContribution loadContributionMenuItem(UiLoadHandler loadHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, "_Load"), event -> loadHandler.load());
    }

    @Bean
    @Order(10)
    public MenuContribution loadAutosavedContributionMenuItem(UiLoadHandler loadHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, "Load _audosaved"), event -> loadHandler.loadAutosaved());
    }

    @Bean
    @Order(20)
    public MenuContribution saveContributionMenuItem(UiSaveHandler saveHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, "_Save"), event -> saveHandler.save());
    }

    @Bean
    @Order(30)
    public MenuContribution saveAsContributionMenuItem(UiSaveHandler saveHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, "Save _As"), event -> saveHandler.saveAs());
    }

    @Bean
    @Order(200)
    public MenuContribution exitContributionMenuItem(ExitWithSaveService exitWithSaveService) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, "E_xit"), event -> {
            exitWithSaveService.optionallySaveAndThenRun(() -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }

}

package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.clip.ImportClipFileChooser;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.imagesequence.ImageSequenceChooserDialogOpener;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;
import com.helospark.tactview.ui.javafx.save.UiSaveHandler;

import javafx.application.Platform;

@Configuration
public class DefaultFileMenuItemConfiguration {
    private static final String IMPORT_SUBMENU_ITEM = "Import";
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
    @Order(40)
    public MenuContribution importVideoMenuItem(ImportClipFileChooser importFileChooser) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, IMPORT_SUBMENU_ITEM, "Audio or videoclip"), event -> importFileChooser.importClip());
    }

    @Bean
    @Order(41)
    public MenuContribution importImageSequenceMenuItem(ImageSequenceChooserDialogOpener imageSequenceImporter) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, IMPORT_SUBMENU_ITEM, "Image sequence"), event -> imageSequenceImporter.importImageSequence());
    }

    @Bean
    @Order(190)
    public MenuContribution restartContributionMenuItem(ExitWithSaveService exitWithSaveService) {
        int restartStatusCode = 3;
        return new DefaultMenuContribution(List.of(FILE_ROOT, "R_estart"), event -> {
            exitWithSaveService.optionallySaveAndThenRun(() -> {
                Platform.exit();
                System.exit(restartStatusCode);
            });
        });
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

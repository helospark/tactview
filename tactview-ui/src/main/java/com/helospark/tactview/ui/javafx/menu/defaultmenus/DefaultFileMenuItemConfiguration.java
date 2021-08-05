package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.ProjectInitializer;
import com.helospark.tactview.ui.javafx.hotkey.HotKeyRepository;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.DynamicallyGeneratedParentMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SeparatorMenuContribution;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.clip.ImportClipFileChooser;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.imagesequence.ImageSequenceChooserDialogOpener;
import com.helospark.tactview.ui.javafx.save.ExitWithSaveService;
import com.helospark.tactview.ui.javafx.save.RecentlyAccessedRepository;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;
import com.helospark.tactview.ui.javafx.save.UiSaveHandler;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

@Configuration
public class DefaultFileMenuItemConfiguration {
    private static final String TEMPLATE = "Template";
    private static final String LOAD_RECENT_MENU_ITEM = "Load _recent";
    private static final String IMPORT_SUBMENU_ITEM = "Import";
    public static final String FILE_ROOT = "_File";

    private HotKeyRepository hotKeyRepository;

    public DefaultFileMenuItemConfiguration(HotKeyRepository hotKeyRepository) {
        this.hotKeyRepository = hotKeyRepository;
    }

    @Bean
    @Order(-10)
    public SelectableMenuContribution newContributionMenuItem(ProjectInitializer projectInitializer, ExitWithSaveService exitWithSaveService) {
        KeyCodeCombination combination = hotKeyRepository.registerOrGetHotKey("newProject", new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN), "New project").getCombination();
        return new DefaultMenuContribution(List.of(FILE_ROOT, "_New"), event -> {
            exitWithSaveService.optionallySaveAndThenRun(() -> {
                projectInitializer.clearAndInitialize();
            });

        }, combination);
    }

    @Bean
    @Order(0)
    public SelectableMenuContribution loadContributionMenuItem(UiLoadHandler loadHandler) {
        KeyCodeCombination combination = hotKeyRepository.registerOrGetHotKey("loadProject", new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN), "Load project").getCombination();
        return new DefaultMenuContribution(List.of(FILE_ROOT, "_Load"), event -> loadHandler.load(), combination);
    }

    @Bean
    @Order(10)
    public SelectableMenuContribution loadAutosavedContributionMenuItem(UiLoadHandler loadHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, "Load _autosaved"), event -> loadHandler.loadAutosaved());
    }

    @Bean
    @Order(12)
    public DynamicallyGeneratedParentMenuContribution recentFiles(RecentlyAccessedRepository recentlySavedRepository, UiLoadHandler loadHandler) {
        return new DynamicallyGeneratedParentMenuContribution() {

            @Override
            public List<String> getPath() {
                return List.of(FILE_ROOT, LOAD_RECENT_MENU_ITEM);
            }

            @Override
            public List<MenuContribution> getChildren() {
                return recentlySavedRepository.getRecentlySavedElements()
                        .stream()
                        .map(a -> new DefaultMenuContribution(List.of(a.getAbsolutePath()), event -> {
                            loadHandler.loadFile(a);
                        }))
                        .collect(Collectors.toList());
            }
        };
    }

    @Bean
    @Order(15)
    public SeparatorMenuContribution afterLoadeSeparatorMenuItem(UiLoadHandler loadHandler) {
        return new SeparatorMenuContribution(List.of(FILE_ROOT));
    }

    @Bean
    @Order(20)
    public SelectableMenuContribution saveContributionMenuItem(UiSaveHandler saveHandler) {
        KeyCodeCombination combination = hotKeyRepository.registerOrGetHotKey("saveProject", new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN), "Save project").getCombination();
        return new DefaultMenuContribution(List.of(FILE_ROOT, "_Save"), event -> saveHandler.save(), combination);
    }

    @Bean
    @Order(30)
    public SelectableMenuContribution saveAsContributionMenuItem(UiSaveHandler saveHandler) {
        KeyCodeCombination combination = hotKeyRepository
                .registerOrGetHotKey("saveProjectAs", new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN), "Save project as").getCombination();

        return new DefaultMenuContribution(List.of(FILE_ROOT, "Save _As"), event -> saveHandler.saveAs(), combination);
    }

    @Bean
    @Order(32)
    public SelectableMenuContribution saveProjectAsTemplate(UiSaveHandler saveHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, TEMPLATE, "Save _As template"), event -> saveHandler.saveAsTemplate());
    }

    @Bean
    @Order(33)
    public SelectableMenuContribution loadTemplateAsProjectMenuItem(UiLoadHandler loadHandler) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, TEMPLATE, "_Load template as project"), event -> loadHandler.loadTemplate());
    }

    @Bean
    @Order(35)
    public SeparatorMenuContribution afterSaveSeparatorMenuItem(UiLoadHandler loadHandler) {
        return new SeparatorMenuContribution(List.of(FILE_ROOT));
    }

    @Bean
    @Order(40)
    public SelectableMenuContribution importVideoMenuItem(ImportClipFileChooser importFileChooser) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, IMPORT_SUBMENU_ITEM, "Audio or videoclip"), event -> importFileChooser.importClip());
    }

    @Bean
    @Order(41)
    public SelectableMenuContribution importImageSequenceMenuItem(ImageSequenceChooserDialogOpener imageSequenceImporter) {
        return new DefaultMenuContribution(List.of(FILE_ROOT, IMPORT_SUBMENU_ITEM, "Image sequence"), event -> imageSequenceImporter.importImageSequence());
    }

    @Bean
    @Order(45)
    public SeparatorMenuContribution afterImportSeparatorMenuItem(UiLoadHandler loadHandler) {
        return new SeparatorMenuContribution(List.of(FILE_ROOT));
    }

    @Bean
    @Order(190)
    public SelectableMenuContribution restartContributionMenuItem(ExitWithSaveService exitWithSaveService) {
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
    public SelectableMenuContribution exitContributionMenuItem(ExitWithSaveService exitWithSaveService) {
        KeyCodeCombination combination = hotKeyRepository.registerOrGetHotKey("quit", new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN), "Exit").getCombination();
        return new DefaultMenuContribution(List.of(FILE_ROOT, "E_xit"), event -> {
            exitWithSaveService.optionallySaveAndThenRun(() -> {
                Platform.exit();
                System.exit(0);
            });
        }, combination);
    }

}

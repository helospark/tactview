package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.preferences.PreferencesPage;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

@Configuration
public class DefaultEditMenuItemConfiguration {
    public static final String EDIT_ROOT = "_Edit";

    @Bean
    @Order(1000)
    public MenuContribution undoContributionMenuItem(UiCommandInterpreterService commandInterpreter) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Undo"), event -> commandInterpreter.revertLast(), new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN));
    }

    @Bean
    @Order(1010)
    public MenuContribution redoContributionMenuItem(UiCommandInterpreterService commandInterpreter) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Redo"), event -> commandInterpreter.redoLast(),
                new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    }

    @Bean
    @Order(1020)
    public MenuContribution deselectAllContributionMenuItem(SelectedNodeRepository selectedNodeRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Clear selection"), event -> selectedNodeRepository.clearAllSelectedItems());
    }

    @Bean
    @Order(2000)
    public MenuContribution preferencesContributionMenuItem(PreferencesPage preferencesPage) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Preferences"), event -> preferencesPage.open());
    }

}

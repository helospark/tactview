package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;

@Configuration
public class DefaultEditMenuItemConfiguration {
    public static final String EDIT_ROOT = "_Edit";

    @Bean
    @Order(1000)
    public MenuContribution undoContributionMenuItem(UiCommandInterpreterService commandInterpreter) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Undo"), event -> commandInterpreter.revertLast());
    }

    @Bean
    @Order(1010)
    public MenuContribution redoContributionMenuItem(UiCommandInterpreterService commandInterpreter) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Redo"), event -> commandInterpreter.redoLast());
    }

    @Bean
    @Order(1020)
    public MenuContribution deselectAllContributionMenuItem(SelectedNodeRepository selectedNodeRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Clear selection"), event -> selectedNodeRepository.clearAllSelectedItems());
    }

}

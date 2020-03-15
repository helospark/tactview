package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SeparatorMenuContribution;
import com.helospark.tactview.ui.javafx.preferences.PreferencesPage;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

@Configuration
public class DefaultEditMenuItemConfiguration {
    public static final String EDIT_ROOT = "_Edit";

    @Bean
    @Order(1000)
    public SelectableMenuContribution undoContributionMenuItem(UiCommandInterpreterService commandInterpreter) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Undo"), event -> commandInterpreter.revertLast(), new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN));
    }

    @Bean
    @Order(1010)
    public SelectableMenuContribution redoContributionMenuItem(UiCommandInterpreterService commandInterpreter) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Redo"), event -> commandInterpreter.redoLast(),
                new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
    }

    @Bean
    @Order(1020)
    public SeparatorMenuContribution afterRedoSeparatorMenuItem(UiLoadHandler loadHandler) {
        return new SeparatorMenuContribution(List.of(EDIT_ROOT));
    }

    @Bean
    @Order(1500)
    public SelectableMenuContribution copyContributionMenuItem(SelectedNodeRepository selectedNodeRepository, CopyPasteRepository copyPasteRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Copy"), event -> {
            List<String> selectedClipIds = selectedNodeRepository.getSelectedClipIds();
            if (selectedClipIds.isEmpty()) {
                copyPasteRepository.pasteWithoutAdditionalInfo();
            } else {
                copyPasteRepository.pasteOnExistingClips(selectedClipIds);
            }
        }, new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN));
    }

    @Bean
    @Order(1550)
    public SelectableMenuContribution pasteContributionMenuItem(SelectedNodeRepository selectedNodeRepository, CopyPasteRepository copyPasteRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Paste"), event -> {
            List<String> selectedClipIds = selectedNodeRepository.getSelectedClipIds();
            if (selectedClipIds.isEmpty()) {
                copyPasteRepository.pasteWithoutAdditionalInfo();
            } else {
                copyPasteRepository.pasteOnExistingClips(selectedClipIds);
            }
        }, new KeyCodeCombination(KeyCode.V, KeyCodeCombination.CONTROL_DOWN));
    }

    @Bean
    @Order(1560)
    public SeparatorMenuContribution afterPasteSeparatorMenuItem(UiLoadHandler loadHandler) {
        return new SeparatorMenuContribution(List.of(EDIT_ROOT));
    }

    @Bean
    @Order(1820)
    public SelectableMenuContribution deselectAllContributionMenuItem(SelectedNodeRepository selectedNodeRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Clear selection"), event -> selectedNodeRepository.clearAllSelectedItems());
    }

    @Bean
    @Order(2000)
    public SelectableMenuContribution preferencesContributionMenuItem(PreferencesPage preferencesPage) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Preferences"), event -> preferencesPage.open());
    }

}

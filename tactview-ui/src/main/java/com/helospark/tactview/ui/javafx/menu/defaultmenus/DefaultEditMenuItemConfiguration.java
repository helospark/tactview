package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SeparatorMenuContribution;
import com.helospark.tactview.ui.javafx.preferences.PreferencesPage;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

@Configuration
public class DefaultEditMenuItemConfiguration {
    public static final String EDIT_ROOT = "_Edit";
    public static final String SELECT_ROOT = "_Select";
    public static final String JUMP_ROOT = "_Jump";

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
            if (selectedClipIds.size() > 0) { // copy ony the first for now
                String selectedClipId = selectedClipIds.get(0);
                copyPasteRepository.copyClip(selectedClipId);
            } else {
                List<String> selectedEffects = selectedNodeRepository.getSelectedEffectIds();
                if (selectedEffects.size() > 0) {
                    copyPasteRepository.copyEffect(selectedEffects);
                }
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
    @Order(1800)
    public SelectableMenuContribution selectAllClipsContextMenuItem(SelectedNodeRepository selectedNodeRepository, TimelineManagerAccessor timelineManager) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, SELECT_ROOT, "_All clips"), event -> {
            selectedNodeRepository.clearAllSelectedItems();
            selectedNodeRepository.addSelectedClips(timelineManager.getAllClipIds());
        });
    }

    @Bean
    @Order(1801)
    public SelectableMenuContribution selectClipsToLeftContextMenuItem(SelectedNodeRepository selectedNodeRepository, TimelineManagerAccessor timelineManager, TimelineState state) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, SELECT_ROOT, "Clips to _left"), event -> {
            TimelinePosition playbackPosition = state.getPlaybackPosition();

            selectedNodeRepository.clearAllSelectedItems();
            List<String> clipsIds = new ArrayList<>();
            for (var channel : timelineManager.getChannels()) {
                for (var clip : channel.getAllClips()) {
                    if (!clip.getInterval().getStartPosition().isGreaterThan(playbackPosition)) {
                        clipsIds.add(clip.getId());
                    }
                }
            }

            selectedNodeRepository.addSelectedClips(clipsIds);
        });
    }

    @Bean
    @Order(1802)
    public SelectableMenuContribution selectClipsToRightContextMenuItem(SelectedNodeRepository selectedNodeRepository, TimelineManagerAccessor timelineManager, TimelineState state) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, SELECT_ROOT, "Clips to _right"), event -> {
            TimelinePosition playbackPosition = state.getPlaybackPosition();

            selectedNodeRepository.clearAllSelectedItems();
            List<String> clipsIds = new ArrayList<>();
            for (var channel : timelineManager.getChannels()) {
                for (var clip : channel.getAllClips()) {
                    if (!clip.getInterval().getEndPosition().isLessThan(playbackPosition)) {
                        clipsIds.add(clip.getId());
                    }
                }
            }

            selectedNodeRepository.addSelectedClips(clipsIds);
        });
    }

    @Bean
    @Order(1820)
    public SelectableMenuContribution deselectAllContributionMenuItem(SelectedNodeRepository selectedNodeRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Clear selection"), event -> selectedNodeRepository.clearAllSelectedItems());
    }

    @Bean
    @Order(1899)
    public SeparatorMenuContribution afterSelectSeparatorMenuItem() {
        return new SeparatorMenuContribution(List.of(EDIT_ROOT));
    }

    @Bean
    @Order(1900)
    public SelectableMenuContribution jumpForwardOneFrameMenuItem(UiTimelineManager timelineManager) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, JUMP_ROOT, "Jump _forward one frame"), event -> timelineManager.moveForwardOneFrame());
    }

    @Bean
    @Order(1901)
    public SelectableMenuContribution jumpBackwardOneFrameMenuItem(UiTimelineManager timelineManager) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, JUMP_ROOT, "Jump _backward one frame"), event -> timelineManager.moveBackOneFrame());
    }

    @Bean
    @Order(1902)
    public SelectableMenuContribution jumpToAMarkerMenuItem(UiTimelineManager timelineManager, TimelineState timelineState) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, JUMP_ROOT, "Jump to '_A' marker"), event -> {
            Optional<TimelinePosition> loopAProperties = timelineState.getLoopALineProperties();
            if (loopAProperties.isPresent()) {
                timelineManager.jumpAbsolute(loopAProperties.get().getSeconds());
            }
        });
    }

    @Bean
    @Order(1903)
    public SelectableMenuContribution jumpToBMarkerMenuItem(UiTimelineManager timelineManager, TimelineState timelineState) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, JUMP_ROOT, "Jump to '_B' marker"), event -> {
            Optional<TimelinePosition> loopBProperties = timelineState.getLoopBLineProperties();
            if (loopBProperties.isPresent()) {
                timelineManager.jumpAbsolute(loopBProperties.get().getSeconds());
            }
        });
    }

    @Bean
    @Order(2000)
    public SelectableMenuContribution preferencesContributionMenuItem(PreferencesPage preferencesPage) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Preferences"), event -> preferencesPage.open());
    }

}

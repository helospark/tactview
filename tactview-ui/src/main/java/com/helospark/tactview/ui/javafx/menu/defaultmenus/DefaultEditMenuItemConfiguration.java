package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.chapter.ChapterRepository;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SeparatorMenuContribution;
import com.helospark.tactview.ui.javafx.preferences.PreferencesPage;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

@Configuration
public class DefaultEditMenuItemConfiguration {
    public static final String EDIT_ROOT = "_Edit";
    public static final String SELECT_ROOT = "_Select";
    public static final String JUMP_ROOT = "_Jump";
    public static final String CHAPTER_ROOT = "_Chapter";

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
    @Order(1950)
    public SelectableMenuContribution addChapterMenuItem(AlertDialogFactory dialogFactory, TimelineState timelineState, ChapterRepository chapterRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, CHAPTER_ROOT, "Add chapter at current position"), event -> {
            TimelinePosition position = timelineState.getPlaybackPosition();

            Optional<String> result = dialogFactory.showTextInputDialog("Add chapter", "Label of the chapter", "Chapter x");

            if (result.isPresent()) {
                chapterRepository.addChapter(position, result.get());
            }
        }, new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN));
    }

    @Bean
    @Order(1951)
    public SelectableMenuContribution removeAllChaptersMenuItem(ChapterRepository chapterRepository) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, CHAPTER_ROOT, "Remove all chapters"), event -> {
            chapterRepository.removeAllChapters();
        });
    }

    @Bean
    @Order(1952)
    public SelectableMenuContribution exportAsYoutubeChapterMenuItem(ChapterRepository chapterRepository, AlertDialogFactory alertDialogFactory) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, CHAPTER_ROOT, "Show as Youtube chapter"), event -> {
            String result = "";
            String errors = "";
            int numberOfChapters = 0;

            TimelinePosition previousPosition = null;
            if (chapterRepository.getChapters().get(TimelinePosition.ofZero()) == null) {
                result += "00:00 Intro\n";
                errors += "[WARN] No chapter is defined in position 00:00, added intro chapter at 0\n";
                previousPosition = TimelinePosition.ofZero();
                ++numberOfChapters;
            }

            for (var chapter : chapterRepository.getChapters().entrySet()) {
                long allSeconds = chapter.getKey().getSeconds().longValue();
                long minutes = allSeconds / 60;
                long seconds = allSeconds % 60;
                result += (String.format("%02d:%02d %s\n", minutes, seconds, chapter.getValue()));
                ++numberOfChapters;

                if (previousPosition != null && chapter.getKey().subtract(previousPosition).isLessThan(10)) {
                    errors += "[ERROR] Chapter at " + minutes + ":" + seconds + " is less than 10s from previous chapter which is unaccaptable by YouTube\n";
                }

                previousPosition = chapter.getKey();
            }
            if (numberOfChapters < 3) {
                errors += "[ERROR] Minimum 3 chapters needed by YouTube";
            }

            alertDialogFactory.showTextDialog("Youtube chapters", "Copy the below in your description to add chapters to Youtube", errors, result);
        });
    }

    @Bean
    @Order(2000)
    public SelectableMenuContribution preferencesContributionMenuItem(PreferencesPage preferencesPage) {
        return new DefaultMenuContribution(List.of(EDIT_ROOT, "_Preferences"), event -> preferencesPage.open());
    }

}

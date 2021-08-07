package com.helospark.tactview.ui.javafx.repository;

import static com.helospark.tactview.ui.javafx.repository.selection.ChangeType.ALL_SELECTION_REMOVED;
import static com.helospark.tactview.ui.javafx.repository.selection.ChangeType.PRIMARY_SELECTION_ADDED;
import static com.helospark.tactview.ui.javafx.repository.selection.ChangeType.SECONDARY_SELECTION_ADDED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.repository.selection.ChangeType;
import com.helospark.tactview.ui.javafx.repository.selection.ClipSelectionChangedMessage;
import com.helospark.tactview.ui.javafx.repository.selection.EffectSelectionChangedMessage;

@Component
public class SelectedNodeRepository implements CleanableMode {
    private List<String> selectedClips = new ArrayList<>();
    private List<String> selectedEffects = new ArrayList<>();

    private MessagingService messagingService;

    public SelectedNodeRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public Optional<String> getPrimarySelectedClip() {
        return selectedClips.stream()
                .findFirst();
    }

    public Optional<String> getPrimarySelectedClipId() {
        return getPrimarySelectedClip();
    }

    public Optional<String> getPrimarySelectedEffect() {
        return selectedEffects.stream()
                .findFirst();
    }

    public void clearAllSelectedItems() {
        clearClipSelections();
        clearEffectSelections();
    }

    public void setOnlySelectedClip(String clip) {
        clearClipSelections();
        clearEffectSelections();
        this.selectedClips.add(clip);
        messagingService.sendMessage(new ClipSelectionChangedMessage(clip, PRIMARY_SELECTION_ADDED));
    }

    private void clearClipSelections() {
        while (!selectedClips.isEmpty()) {
            String item = selectedClips.get(0);
            removeClipSelection(item);
        }
    }

    private void removeClipSelection(String item) {
        selectedClips.remove(item);
        messagingService.sendMessage(new ClipSelectionChangedMessage(item, ALL_SELECTION_REMOVED));
    }

    public void setOnlySelectedEffect(String clip) {
        clearClipSelections();
        clearEffectSelections();
        this.selectedEffects.add(clip);
        messagingService.sendMessage(new EffectSelectionChangedMessage(clip, PRIMARY_SELECTION_ADDED));
    }

    private void clearEffectSelections() {
        while (!selectedEffects.isEmpty()) {
            String item = selectedEffects.get(0);
            removeEffect(item);
        }
    }

    private void removeEffect(String item) {
        selectedEffects.remove(item);
        messagingService.sendMessage(new EffectSelectionChangedMessage(item, ALL_SELECTION_REMOVED));
    }

    public void addSelectedClip(String clip) {
        this.selectedClips.add(clip);
        ChangeType type = PRIMARY_SELECTION_ADDED;
        if (selectedClips.size() > 1) {
            type = SECONDARY_SELECTION_ADDED;
        }
        messagingService.sendMessage(new ClipSelectionChangedMessage(clip, type));
    }

    public void toggleClipSelection(String clip) {
        if (this.selectedClips.contains(clip)) {
            removeClipSelection(clip);
        } else {
            addSelectedClip(clip);
        }
    }

    public void toggleEffectSelection(String effect) {
        if (this.selectedClips.contains(effect)) {
            removeEffect(effect);
        } else {
            addSelectedEffect(effect);
        }
    }

    public void addSelectedEffect(String clip) {
        this.selectedEffects.add(clip);
        ChangeType type = PRIMARY_SELECTION_ADDED;
        if (selectedClips.size() > 1) {
            type = SECONDARY_SELECTION_ADDED;
        }
        messagingService.sendMessage(new EffectSelectionChangedMessage(clip, type));
    }

    public List<String> getSelectedClipIds() {
        return this.selectedClips.stream()
                .collect(Collectors.toList());
    }

    public List<String> getSelectedEffectIds() {
        return this.selectedEffects.stream()
                .collect(Collectors.toList());
    }

    @Override
    public void clean() {
        clearAllSelectedItems();
    }

    public void addSelectedClips(List<String> clipIds) {
        clipIds.stream()
                .forEach(clipId -> addSelectedClip(clipId));
    }

    public void clearAndSetSelectedClips(List<String> clipIds) {
        clearAllSelectedItems();
        addSelectedClips(clipIds);
    }

}

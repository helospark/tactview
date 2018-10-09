package com.helospark.tactview.ui.javafx.repository;

import static com.helospark.tactview.ui.javafx.repository.selection.ChangeType.PRIMARY_SELECTION_ADDED;
import static com.helospark.tactview.ui.javafx.repository.selection.ChangeType.PRIMARY_SELECTION_REMOVED;
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

import javafx.scene.Node;

@Component
public class SelectedNodeRepository {
    private List<Node> selectedClips = new ArrayList<>();
    private List<Node> selectedEffects = new ArrayList<>();

    private MessagingService messagingService;

    public SelectedNodeRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public Optional<Node> getPrimarySelectedClip() {
        return selectedClips.stream()
                .findFirst();
    }

    public Optional<Node> getPrimarySelectedEffect() {
        return selectedEffects.stream()
                .findFirst();
    }

    public void clearAllSelectedItems() {
        selectedClips.clear();
        selectedEffects.clear();
    }

    public void setOnlySelectedClip(Node clip) {
        clearClips();
        clearEffects();
        this.selectedClips.add(clip);
        messagingService.sendMessage(new ClipSelectionChangedMessage(clip, PRIMARY_SELECTION_ADDED));
    }

    private void clearClips() {
        while (!selectedClips.isEmpty()) {
            Node item = selectedClips.get(0);
            removeClip(item);
        }
    }

    private void removeClip(Node item) {
        selectedClips.remove(item);
        messagingService.sendMessage(new ClipSelectionChangedMessage(item, PRIMARY_SELECTION_REMOVED));
    }

    public void setOnlySelectedEffect(Node clip) {
        clearClips();
        clearEffects();
        this.selectedEffects.add(clip);
        messagingService.sendMessage(new EffectSelectionChangedMessage(clip, PRIMARY_SELECTION_ADDED));
    }

    private void clearEffects() {
        while (!selectedEffects.isEmpty()) {
            Node item = selectedEffects.get(0);
            removeEffect(item);
        }
    }

    private void removeEffect(Node item) {
        selectedEffects.remove(item);
        messagingService.sendMessage(new EffectSelectionChangedMessage(item, PRIMARY_SELECTION_REMOVED));
    }

    public void addSelectedClip(Node clip) {
        this.selectedClips.add(clip);
        ChangeType type = PRIMARY_SELECTION_ADDED;
        if (selectedClips.size() > 1) {
            type = SECONDARY_SELECTION_ADDED;
        }
        messagingService.sendMessage(new ClipSelectionChangedMessage(clip, type));
    }

    public void addSelectedEffect(Node clip) {
        this.selectedEffects.add(clip);
        ChangeType type = PRIMARY_SELECTION_ADDED;
        if (selectedClips.size() > 1) {
            type = SECONDARY_SELECTION_ADDED;
        }
        messagingService.sendMessage(new EffectSelectionChangedMessage(clip, type));
    }

    public List<String> getSelectedClipIds() {
        return this.selectedClips.stream()
                .map(node -> (String) node.getUserData())
                .collect(Collectors.toList());
    }

    public List<String> getSelectedEffectIds() {
        return this.selectedEffects.stream()
                .map(node -> (String) node.getUserData())
                .collect(Collectors.toList());
    }
}

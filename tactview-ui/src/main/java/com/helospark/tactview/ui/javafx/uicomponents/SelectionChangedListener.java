package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.messaging.MessagingServiceImpl;
import com.helospark.tactview.ui.javafx.repository.selection.ChangeType;
import com.helospark.tactview.ui.javafx.repository.selection.ClipSelectionChangedMessage;
import com.helospark.tactview.ui.javafx.repository.selection.EffectSelectionChangedMessage;

import javafx.scene.Node;

@Component
public class SelectionChangedListener {
    private static final Map<ChangeType, List<String>> CHANGE_TYPE_TO_CLASS = Map.of(
            ChangeType.PRIMARY_SELECTION_ADDED, List.of("primary-active"),
            ChangeType.SECONDARY_SELECTION_ADDED, List.of("secondary-active"));

    private static final Map<ChangeType, List<String>> CHANGE_TYPE_TO_REMOVE = Map.of(
            ChangeType.ALL_SELECTION_REMOVED, List.of("primary-active", "secondary-active"));

    private MessagingServiceImpl messagingService;
    private PropertyView propertyView;

    public SelectionChangedListener(MessagingServiceImpl messagingService, PropertyView propertyView) {
        this.messagingService = messagingService;
        this.propertyView = propertyView;
    }

    @PostConstruct
    public void init() {
        messagingService.register(EffectSelectionChangedMessage.class, message -> {
            ChangeType type = message.getType();
            Node node = message.getEffect();
            process(type, node);
        });
        messagingService.register(EffectSelectionChangedMessage.class, message -> {
            ChangeType type = message.getType();
            if (type.equals(ChangeType.PRIMARY_SELECTION_ADDED)) {
                propertyView.showEffectProperties((String) message.getEffect().getUserData());
            }
        });
        messagingService.register(ClipSelectionChangedMessage.class, message -> {
            ChangeType type = message.getType();
            Node node = message.getClip();
            process(type, node);
        });
        messagingService.register(ClipSelectionChangedMessage.class, message -> {
            ChangeType type = message.getType();
            if (type.equals(ChangeType.PRIMARY_SELECTION_ADDED)) {
                propertyView.showClipProperties((String) message.getClip().getUserData());
            }
        });
    }

    private void process(ChangeType type, Node node) {
        List<String> classToAdd = CHANGE_TYPE_TO_CLASS.get(type);
        if (classToAdd != null) {
            node.getStyleClass().addAll(classToAdd);
            return;
        }
        List<String> classToRemove = CHANGE_TYPE_TO_REMOVE.get(type);
        if (classToRemove != null) {
            node.getStyleClass().removeAll(classToRemove);
            return;
        }
    }
}

package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Map;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.repository.ChangeType;
import com.helospark.tactview.ui.javafx.repository.ClipSelectionChangedMessage;
import com.helospark.tactview.ui.javafx.repository.EffectSelectionChangedMessage;

import javafx.scene.Node;

@Component
public class SelectionChangedListener {
    private static final Map<ChangeType, String> CHANGE_TYPE_TO_CLASS = Map.of(
            ChangeType.PRIMARY_SELECTION_ADDED, "primary-active",
            ChangeType.SECONDARY_SELECTION_ADDED, "secondary-active");

    private static final Map<ChangeType, String> CHANGE_TYPE_TO_REMOVE = Map.of(
            ChangeType.PRIMARY_SELECTION_REMOVED, "primary-active",
            ChangeType.SECONDARY_SELECTION_REMOVED, "secondary-active");

    private MessagingService messagingService;
    private PropertyView propertyView;

    public SelectionChangedListener(MessagingService messagingService, PropertyView propertyView) {
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
        String classToAdd = CHANGE_TYPE_TO_CLASS.get(type);
        if (classToAdd != null) {
            node.getStyleClass().add(classToAdd);
            return;
        }
        String classToRemove = CHANGE_TYPE_TO_REMOVE.get(type);
        if (classToRemove != null) {
            node.getStyleClass().remove(classToRemove);
            return;
        }
    }
}

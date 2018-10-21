package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.EffectPropertyPage.Builder;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.EffectLine;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.PropertyValueSetterChain;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

@Component
public class PropertyView {
    private FlowPane propertyWindow;
    private Map<String, EffectPropertyPage> effectProperties = new HashMap<>();
    private Map<String, EffectPropertyPage> clipProperties = new HashMap<>();

    private MessagingService messagingService;
    private UiTimelineManager uiTimelineManager;
    private EffectPropertyPage shownEntries;
    private PropertyValueSetterChain propertyValueSetterChain;

    public PropertyView(MessagingService messagingService, UiTimelineManager uiTimelineManager, PropertyValueSetterChain propertyValueSetterChain) {
        this.messagingService = messagingService;
        this.uiTimelineManager = uiTimelineManager;
        this.propertyValueSetterChain = propertyValueSetterChain;
    }

    @PostConstruct
    public void init() {
        propertyWindow = new FlowPane();
        propertyWindow.setId("property-view");
        propertyWindow.setPrefWidth(200);

        messagingService.register(EffectDescriptorsAdded.class, message -> Platform.runLater(() -> {
            EffectPropertyPage asd = createBox(message.getDescriptors());
            effectProperties.put(message.getEffectId(), asd);
        }));
        messagingService.register(ClipDescriptorsAdded.class, message -> Platform.runLater(() -> {
            EffectPropertyPage asd = createBox(message.getDescriptors());
            clipProperties.put(message.getClipId(), asd);
        }));
    }

    private EffectPropertyPage createBox(List<ValueProviderDescriptor> descriptors) {
        Builder result = EffectPropertyPage.builder().withBox(new GridPane());
        for (int i = 0; i < descriptors.size(); ++i) {
            addElement(descriptors.get(i), result, i);
        }
        return result.build();
    }

    private void addElement(ValueProviderDescriptor descriptor, Builder result, int line) {
        Label label = new Label(descriptor.getName());
        EffectLine keyframeChange = createKeyframeUi(descriptor.getKeyframeableEffect());

        Node key = keyframeChange.getVisibleNode();
        key.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.I)) {
                keyframeChange.sendKeyframe(uiTimelineManager.getCurrentPosition());
                event.consume();
            }
        });

        result.getBox().add(label, 0, line);
        result.getBox().add(key, 1, line);

        result.addUpdateFunctions(keyframeChange::updateUi);
    }

    private EffectLine createKeyframeUi(KeyframeableEffect keyframeableEffect) {
        return propertyValueSetterChain.create(keyframeableEffect);
    }

    public FlowPane getPropertyWindow() {
        return propertyWindow;
    }

    public void showEffectProperties(String effectId) {
        showProperties(effectProperties.get(effectId));
    }

    public void showClipProperties(String clipId) {
        showProperties(clipProperties.get(clipId));
    }

    private void showProperties(EffectPropertyPage shownEntries2) {
        shownEntries = shownEntries2;
        propertyWindow.getChildren().clear();
        if (shownEntries2 != null) {
            propertyWindow.getChildren().add(shownEntries.getBox());
            shownEntries2.getUpdateFunctions().stream().forEach(a -> a.accept(uiTimelineManager.getCurrentPosition()));
        } else {
            System.out.println("Effect not found, should not happen");
        }
    }

    public void clearProperties() {
        shownEntries = null;
    }

    public void updateValues(TimelinePosition position) {
        if (shownEntries != null) {
            shownEntries.getUpdateFunctions().stream().forEach(updateFunction -> updateFunction.accept(position));
        }
    }

}

package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForEffect;
import com.helospark.tactview.ui.javafx.uicomponents.EffectPropertyPage.Builder;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

@Component
public class EffectPropertyView {
    private FlowPane propertyWindow;
    private Map<String, EffectPropertyPage> effectProperties = new HashMap<>();

    private MessagingService messagingService;
    private UiCommandInterpreterService commandInterpreter;
    private UiTimelineManager uiTimelineManager;
    private EffectPropertyPage shownEntries;
    private EffectParametersRepository effectParametersRepository;

    public EffectPropertyView(MessagingService messagingService, UiCommandInterpreterService commandInterpreter, UiTimelineManager uiTimelineManager,
            EffectParametersRepository effectParametersRepository) {
        this.messagingService = messagingService;
        this.commandInterpreter = commandInterpreter;
        this.uiTimelineManager = uiTimelineManager;
        this.effectParametersRepository = effectParametersRepository;
    }

    @PostConstruct
    public void init() {
        propertyWindow = new FlowPane();
        propertyWindow.setId("property-view");
        propertyWindow.setPrefWidth(200);

        messagingService.register(EffectDescriptorsAdded.class,
                message -> Platform.runLater(() -> createBox(message)));
    }

    private void createBox(EffectDescriptorsAdded message) {
        List<ValueProviderDescriptor> descriptors = message.getDescriptors();
        Builder result = EffectPropertyPage.builder()
                .withBox(new GridPane());
        for (int i = 0; i < descriptors.size(); ++i) {
            addElement(descriptors.get(i), result, i);
        }
        effectProperties.put(message.getEffectId(), result.build());
    }

    private void addElement(ValueProviderDescriptor descriptor, Builder result, int line) {
        Label label = new Label(descriptor.getName());
        EffectLine keyframeChange = keyframeStuff(descriptor.getKeyframeableEffect());

        Node key = keyframeChange.visibleNode;
        key.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.I)) {
                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(descriptor.getKeyframeableEffect().getId())
                        .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                        .withValue(keyframeChange.currentValueProvider.get())
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForEffect(effectParametersRepository, keyframeRequest));
            }
        });

        result.getBox().add(label, 0, line);
        result.getBox().add(key, 1, line);

        result.addUpdateFunctions(keyframeChange.updateFunction);
    }

    private EffectLine keyframeStuff(KeyframeableEffect keyframeableEffect) {
        // TODO: chain here
        EffectLine effectLine = new EffectLine();
        if (keyframeableEffect instanceof IntegerProvider) {
            IntegerProvider integerProvider = (IntegerProvider) keyframeableEffect;
            if (integerProvider.getMax() - integerProvider.getMin() < 1000) {
                Slider slider = new Slider();
                slider.setMin(integerProvider.getMin());
                slider.setMax(integerProvider.getMax());
                slider.setShowTickLabels(true);
                slider.setShowTickMarks(true);
                slider.setValue(integerProvider.getValueAt(TimelinePosition.ofZero()));
                effectLine.visibleNode = slider;
                effectLine.updateFunction = position -> slider.setValue(integerProvider.getValueAt(position));
                effectLine.currentValueProvider = () -> String.valueOf(slider.getValue());
                return effectLine;
            } else {
                TextField textField = new TextField();
                effectLine.visibleNode = textField;
                effectLine.updateFunction = position -> textField.setText(integerProvider.getValueAt(position).toString());
                effectLine.currentValueProvider = () -> textField.getText();
                return effectLine;
            }
        } else {
            System.out.println("LAter...");
            throw new IllegalArgumentException("Later...");
        }
    }

    public FlowPane getPropertyWindow() {
        return propertyWindow;
    }

    public void showProperties(String effectId) {
        propertyWindow.getChildren().clear();
        shownEntries = effectProperties.get(effectId);
        if (shownEntries != null) {
            propertyWindow.getChildren().add(shownEntries.getBox());
        } else {
            System.out.println("Effect not found, should not happen");
        }
    }

    public void clearProperties() {
        shownEntries = null;
    }

    public void updateValues(TimelinePosition position) {
        if (shownEntries != null) {
            shownEntries.getUpdateFunctions()
                    .stream()
                    .forEach(a -> a.accept(position));
        }
    }

    static class EffectLine {
        public Node visibleNode;
        public Consumer<TimelinePosition> updateFunction;
        public Supplier<String> currentValueProvider;
    }
}

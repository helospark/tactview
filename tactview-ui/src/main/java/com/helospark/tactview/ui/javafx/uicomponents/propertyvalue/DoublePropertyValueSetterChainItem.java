package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Objects;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.SliderValueType;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

@Component
public class DoublePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;
    private ContextMenuAppender contextMenuAppender;

    public DoublePropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager, ContextMenuAppender contextMenuAppender) {
        super(DoubleProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected PrimitiveEffectLine handle(DoubleProvider doubleProvider, ValueProviderDescriptor descriptor) {
        TextField textField = new TextField();
        textField.getStyleClass().add("double-property-field");

        // required because setting the value during playback also triggers keyframe setting
        CustomObservableObject userChangedValueObservable = new CustomObservableObject();

        HBox hbox = new HBox();
        hbox.getChildren().add(textField);

        if (doubleProvider.getSizeFunction().equals(SizeFunction.CLAMP_TO_MIN_MAX) && !Objects.equals(descriptor.getRenderHints().get(RenderTypeHint.TYPE), SliderValueType.INPUT_FIELD)) {
            Slider slider = new Slider();
            slider.setMin(doubleProvider.getMin());
            slider.setMax(doubleProvider.getMax());
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            StringConverter<Number> converter = new NumberStringConverter();
            Bindings.bindBidirectional(textField.textProperty(), slider.valueProperty(), converter);
            slider.valueProperty().addListener((o, old, newValue) -> {
                if (slider.isValueChanging()) {
                    userChangedValueObservable.setValue(String.valueOf(newValue));
                }
            });
            hbox.getChildren().add(slider);
        }

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(doubleProvider.getId())
                .withUpdateFunction(position -> {
                    if (!textField.isFocused()) { // otherwise user may want to type
                        String current = doubleProviderValueToString(doubleProvider.getId(), position);
                        textField.setText(current);
                        if (effectParametersRepository.isKeyframeAt(doubleProvider.getId(), position)) {
                            textField.getStyleClass().add("on-keyframe");
                        } else {
                            textField.getStyleClass().remove("on-keyframe");
                        }
                    }
                })
                .withUpdateFromValue(value -> textField.setText(String.valueOf(value)))
                .withVisibleNode(hbox)
                .withDescriptor(descriptor)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        textField.setOnKeyReleased(event -> {
            userChangedValueObservable.setValue(textField.getText());
        });

        userChangedValueObservable.registerListener(newValue -> {
            result.sendKeyframeWithValue(timelineManager.getCurrentPosition(), newValue);
        });

        contextMenuAppender.addContextMenu(result, doubleProvider, descriptor, hbox);

        return result;

    }

    private String doubleProviderValueToString(String id, TimelinePosition position) {
        return effectParametersRepository.getValueAt(id, position);
    }

}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Objects;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.SliderValueType;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
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
public class IntegerPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<IntegerProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;
    private ContextMenuAppender contextMenuAppender;

    public IntegerPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager, ContextMenuAppender contextMenuAppender) {
        super(IntegerProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(IntegerProvider integerProvider, ValueProviderDescriptor descriptor) {
        // required because setting the value during playback also triggers keyframe setting
        CustomObservableObject userChangedValueObservable = new CustomObservableObject();

        TextField textField = new TextField();
        textField.getStyleClass().add("integer-property-field");
        HBox hbox = new HBox();
        hbox.getChildren().add(textField);

        if (Objects.equals(descriptor.getRenderHints().get(RenderTypeHint.TYPE), SliderValueType.INPUT_FIELD) || integerProvider.getMin().equals(Integer.MIN_VALUE)
                || integerProvider.getMax().equals(Integer.MAX_VALUE)) {
            textField.setOnKeyReleased(newValue -> {
                userChangedValueObservable.setValue(textField.getText());
            });
        } else {
            Slider slider = new Slider();
            slider.setMin(integerProvider.getMin());
            slider.setMax(integerProvider.getMax());
            slider.setShowTickLabels(true);
            slider.setMinorTickCount(3);
            slider.setMajorTickUnit((integerProvider.getMax() - integerProvider.getMin()) / 3);
            slider.valueProperty().addListener((obs, oldval, newVal) -> {
                if (slider.isValueChanging()) {
                    userChangedValueObservable.setValue(String.valueOf(slider.getValue()));
                }
                slider.setValue(newVal.intValue());
            });

            textField.setOnKeyReleased(newValue -> {
                userChangedValueObservable.setValue(String.valueOf(slider.getValue()));
            });

            StringConverter<Number> converter = new NumberStringConverter();
            Bindings.bindBidirectional(textField.textProperty(), slider.valueProperty(), converter);

            hbox.getChildren().add(slider);
        }

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(integerProvider.getId())
                .withUpdateFunction(position -> {
                    if (!textField.isFocused()) {
                        textField.setText(integerProviderValueToString(integerProvider, position));
                    }
                    if (effectParametersRepository.isKeyframeAt(integerProvider.getId(), position)) {
                        textField.getStyleClass().add("on-keyframe");
                    } else {
                        textField.getStyleClass().remove("on-keyframe");
                    }
                })
                .withDescriptor(descriptor)
                .withVisibleNode(hbox)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .build();

        userChangedValueObservable.registerListener(value -> {
            result.sendKeyframeWithValue(timelineManager.getCurrentPosition(), value);
        });

        contextMenuAppender.addContextMenu(result, integerProvider, descriptor, result.visibleNode);

        return result;

    }

    private String integerProviderValueToString(IntegerProvider integerProvider, TimelinePosition position) {
        return Integer.toString((integerProvider.getValueAt(position)));
    }

}

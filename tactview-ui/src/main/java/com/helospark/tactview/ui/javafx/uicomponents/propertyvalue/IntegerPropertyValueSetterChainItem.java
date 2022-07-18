package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Objects;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.SliderValueType;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

@Component
public class IntegerPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<IntegerProvider> {
    private final UiCommandInterpreterService commandInterpreter;
    private final EffectParametersRepository effectParametersRepository;
    private final GlobalTimelinePositionHolder timelineManager;
    private final ContextMenuAppender contextMenuAppender;

    public IntegerPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, GlobalTimelinePositionHolder timelineManager, ContextMenuAppender contextMenuAppender) {
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
                userChangedValueObservable.setValue(textField.getText(), true);
            });
        } else {
            Slider slider = new Slider();
            slider.setMin(integerProvider.getMin());
            slider.setMax(integerProvider.getMax());
            slider.setShowTickLabels(true);
            slider.setMinorTickCount(3);
            int majorTickCount = (integerProvider.getMax() - integerProvider.getMin()) / 3;
            if (majorTickCount > 0) {
                slider.setMajorTickUnit(majorTickCount);
            }
            slider.valueProperty().addListener((obs, oldval, newVal) -> {
                if (slider.isValueChanging()) {
                    userChangedValueObservable.setValue(String.valueOf(slider.getValue()), false);
                }
                slider.setValue(newVal.intValue());
            });
            slider.valueChangingProperty().addListener((abs, oldVal, newVal) -> {
                if (newVal == false) {
                    String startValue = (String) slider.getUserData();
                    if (startValue != null) {
                        userChangedValueObservable.setValueWithRevertablePreviousValue(String.valueOf(slider.getValue()), startValue);
                    }
                    slider.setUserData(null);
                } else {
                    slider.setUserData(String.valueOf((int) (slider.getValue())));
                }
            });

            textField.setOnKeyReleased(newValue -> {
                userChangedValueObservable.setValue(String.valueOf(slider.getValue()), true);
            });

            StringConverter<Number> converter = new NumberStringConverter();
            Bindings.bindBidirectional(textField.textProperty(), slider.valueProperty(), converter);

            hbox.getChildren().add(slider);
        }

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> Integer.valueOf(textField.getText()))
                .withDescriptorId(integerProvider.getId())
                .withUpdateFunction(position -> {
                    if (!textField.isFocused()) {
                        textField.setText(integerProviderValueToString(integerProvider, position));
                    }
                    if (effectParametersRepository.isKeyframeAt(integerProvider.getId(), position)) {
                        textField.getStyleClass().add("on-keyframe");
                    } else {
                        textField.getStyleClass().removeAll("on-keyframe");
                    }
                })
                .withDescriptor(descriptor)
                .withVisibleNode(hbox)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withKeyframeConsumer(t -> {
                    KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                            .withDescriptorId(integerProvider.getId())
                            .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                            .withValue(Integer.valueOf(textField.getText()))
                            .withRevertable(true)
                            .build();

                    commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                })
                .build();

        userChangedValueObservable.registerListener((newValue, revertable) -> {
            try {
                double dValue = Double.valueOf(newValue);

                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(integerProvider.getId())
                        .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                        .withValue(dValue)
                        .withRevertable(revertable)
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
            } catch (NumberFormatException e) {
            }
        });
        userChangedValueObservable.registerPreviousValueListener((value, oldValue) -> {
            try {
                double nValue = Double.valueOf(value);
                double dValue = Double.valueOf(oldValue);

                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(integerProvider.getId())
                        .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                        .withValue(nValue)
                        .withPreviousValue(Optional.ofNullable(dValue))
                        .withRevertable(true)
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
            } catch (NumberFormatException e) {
            }
        });

        contextMenuAppender.addContextMenu(result, integerProvider, descriptor, result.visibleNode);

        return result;

    }

    private String integerProviderValueToString(IntegerProvider integerProvider, TimelinePosition position) {
        return Integer.toString((integerProvider.getValueAt(position)));
    }

}

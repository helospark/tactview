package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Objects;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.SliderValueType;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ExpressionChangedForPropertyCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.helpers.ErrorIgnoringNumberStringConverter;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

@Component
public class DoublePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private GlobalTimelinePositionHolder timelineManager;
    private ContextMenuAppender contextMenuAppender;
    private ErrorIgnoringNumberStringConverter errorIgnoringNumberStringConverter;

    public DoublePropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, GlobalTimelinePositionHolder timelineManager, ContextMenuAppender contextMenuAppender,
            ErrorIgnoringNumberStringConverter errorIgnoringNumberStringConverter) {
        super(DoubleProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
        this.errorIgnoringNumberStringConverter = errorIgnoringNumberStringConverter;
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
            StringConverter<Number> converter = errorIgnoringNumberStringConverter;
            Bindings.bindBidirectional(textField.textProperty(), slider.valueProperty(), converter);
            slider.valueProperty().addListener((o, old, newValue) -> {
                if (doubleProvider.getExpression() == null) {
                    if (slider.isValueChanging()) {
                        userChangedValueObservable.setValue(String.valueOf(newValue), true);
                    }
                }
            });
            slider.valueChangingProperty().addListener((abs, oldVal, newVal) -> {
                if (doubleProvider.getExpression() == null) {
                    if (newVal == false) {
                        String startValue = (String) slider.getUserData();
                        if (startValue != null) {
                            userChangedValueObservable.setValueWithRevertablePreviousValue(String.valueOf(slider.getValue()), startValue);
                        }
                        slider.setUserData(null);
                    } else {
                        slider.setUserData(String.valueOf(slider.getValue()));
                    }
                }
            });
            hbox.getChildren().add(slider);
        }

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> Double.valueOf(textField.getText()))
                .withDescriptorId(doubleProvider.getId())
                .withUpdateFunction(position -> {
                    if (!textField.isFocused()) { // otherwise user may want to type
                        if (doubleProvider.getExpression() == null) {
                            String current = doubleProviderValueToString(doubleProvider.getId(), position);
                            textField.setText(current);
                            if (effectParametersRepository.isKeyframeAt(doubleProvider.getId(), position)) {
                                textField.getStyleClass().add("on-keyframe");
                            } else {
                                textField.getStyleClass().removeAll("on-keyframe");
                            }
                        } else {
                            textField.setText(doubleProvider.getExpression());
                        }
                    }
                })
                .withUpdateFromValue(value -> textField.setText(String.valueOf(value)))
                .withVisibleNode(hbox)
                .withDescriptor(descriptor)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .withKeyframeConsumer(t -> {
                    KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                            .withDescriptorId(doubleProvider.getId())
                            .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                            .withValue(Double.valueOf(textField.getText()))
                            .withRevertable(true)
                            .build();

                    if (doubleProvider.getExpression() != null) {
                        commandInterpreter.sendWithResult(new ExpressionChangedForPropertyCommand(effectParametersRepository, keyframeRequest));
                    } else {
                        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                    }
                })
                .build();

        textField.setOnKeyReleased(event -> {
            userChangedValueObservable.setValue(textField.getText(), true);
        });

        userChangedValueObservable.registerListener((newValue, revertable) -> {
            try {
                double dValue = Double.valueOf(newValue);

                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(doubleProvider.getId())
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
                        .withDescriptorId(doubleProvider.getId())
                        .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                        .withValue(nValue)
                        .withPreviousValue(Optional.ofNullable(dValue))
                        .withRevertable(true)
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
            } catch (NumberFormatException e) {
            }
        });

        contextMenuAppender.addContextMenu(result, doubleProvider, descriptor, hbox);

        return result;

    }

    private String doubleProviderValueToString(String id, TimelinePosition position) {
        return effectParametersRepository.getValueAt(id, position);
    }

}

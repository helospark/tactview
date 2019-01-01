package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

@Component
public class DoublePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleProvider> implements ContextAware {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;
    private LightDiContext context;

    public DoublePropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager) {
        super(DoubleProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
    }

    @Override
    protected PrimitiveEffectLine handle(DoubleProvider doubleProvider, ValueProviderDescriptor descriptor) {
        TextField textField = new TextField();
        textField.getStyleClass().add("double-property-field");

        // required because setting the value during playback also triggers keyframe setting
        CustomObservableObject userChangedValueObservable = new CustomObservableObject();

        HBox hbox = new HBox();
        hbox.getChildren().add(textField);

        if (doubleProvider.getSizeFunction().equals(SizeFunction.CLAMP_TO_MIN_MAX)) {
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
                        System.out.println("Updating " + doubleProvider.getId() + " to " + current);
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

        return result;

    }

    private String doubleProviderValueToString(String id, TimelinePosition position) {
        return effectParametersRepository.getValueAt(id, position);
    }

    @Override
    public void setContext(LightDiContext context) {
        this.context = context;
    }

}

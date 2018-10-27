package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

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

    public IntegerPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        super(IntegerProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    protected EffectLine handle(IntegerProvider integerProvider) {
        TextField textField = new TextField();
        textField.getStyleClass().add("integer-property-field");
        HBox hbox = new HBox();

        Slider slider = new Slider();
        slider.setMin(integerProvider.getMin());
        slider.setMax(integerProvider.getMax());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        StringConverter<Number> converter = new NumberStringConverter();
        Bindings.bindBidirectional(textField.textProperty(), slider.valueProperty(), converter);

        hbox.getChildren().add(textField);
        hbox.getChildren().add(slider);

        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(integerProvider.getId())
                .withUpdateFunction(position -> textField.setText(integerProviderValueToString(integerProvider, position)))
                .withVisibleNode(hbox)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .build();

    }

    private String integerProviderValueToString(IntegerProvider integerProvider, TimelinePosition position) {
        return Integer.toString((integerProvider.getValueAt(position)));
    }

}

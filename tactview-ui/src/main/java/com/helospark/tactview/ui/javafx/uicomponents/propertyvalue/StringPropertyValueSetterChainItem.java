package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.control.TextArea;

@Component
public class StringPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<StringProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public StringPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        super(StringProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    protected EffectLine handle(StringProvider stringProvider, ValueProviderDescriptor descriptor) {
        TextArea textArea = new TextArea();
        textArea.getStyleClass().add("string-property-field");
        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textArea.getText())
                .withDescriptorId(stringProvider.getId())
                .withUpdateFunction(position -> textArea.setText(stringProvider.getValueAt(position)))
                .withVisibleNode(textArea)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();
    }

}

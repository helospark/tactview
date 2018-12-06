package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

import javafx.scene.control.CheckBox;

@Component
public class BooleanPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<BooleanProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public BooleanPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter) {
        super(BooleanProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    protected EffectLine handle(BooleanProvider booleanProvider, ValueProviderDescriptor descriptor) {
        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("boolean-property-field");

        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> checkbox.isSelected() ? "true" : "false")
                .withDescriptorId(booleanProvider.getId())
                .withUpdateFunction(position -> checkbox.setSelected(providerValueToString(booleanProvider.getId(), position)))
                .withVisibleNode(checkbox)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .build();

    }

    private boolean providerValueToString(String id, TimelinePosition position) {
        return Boolean.parseBoolean(effectParametersRepository.getValueAt(id, position));
    }

}

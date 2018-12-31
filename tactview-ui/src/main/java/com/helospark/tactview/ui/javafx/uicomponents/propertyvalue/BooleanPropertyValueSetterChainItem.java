package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.scene.control.CheckBox;

@Component
public class BooleanPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<BooleanProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;

    public BooleanPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager) {
        super(BooleanProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
    }

    @Override
    protected EffectLine handle(BooleanProvider booleanProvider, ValueProviderDescriptor descriptor) {
        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("boolean-property-field");

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> checkbox.isSelected() ? "true" : "false")
                .withDescriptorId(booleanProvider.getId())
                .withUpdateFunction(position -> checkbox.setSelected(providerValueToString(booleanProvider.getId(), position)))
                .withVisibleNode(checkbox)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .build();

        checkbox.selectedProperty().addListener((a, b, c) -> {
            result.sendKeyframe(timelineManager.getCurrentPosition());
        });

        return result;

    }

    private boolean providerValueToString(String id, TimelinePosition position) {
        return Boolean.parseBoolean(effectParametersRepository.getValueAt(id, position));
    }

}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

@Component
public class PropertyValueSetterChain {
    private List<PropertyValueSetterChainItem> items;
    protected UiCommandInterpreterService commandInterpreter;
    protected EffectParametersRepository effectParametersRepository;

    public PropertyValueSetterChain(List<PropertyValueSetterChainItem> items, UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        this.items = items;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    public EffectLine create(KeyframeableEffect effect) {
        EffectLine result = items.stream()
                .filter(e -> e.doesSupport(effect))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No chain item for " + effect))
                .create(effect);
        result.setCommandInterpreter(commandInterpreter, effectParametersRepository);
        return result;
    }
}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.scene.control.ComboBox;

@Component
public class ValueListPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<ValueListProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;

    public ValueListPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager) {
        super(ValueListProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
    }

    @Override
    protected EffectLine handle(ValueListProvider valueProvider) {
        ValueListProvider<ValueListElement> typeFixedValueProvider = valueProvider; // thanks Java
        Map<String, ValueListElement> elements = typeFixedValueProvider.getElements();

        ComboBox<ComboBoxElement> comboBox = new ComboBox<>();

        elements.values()
                .stream()
                .map(a -> new ComboBoxElement(a.getId(), a.getText()))
                .forEach(entry -> {
                    comboBox.getItems().add(entry);
                });
        comboBox.getSelectionModel().select(0);

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> comboBox.getValue().getId())
                .withDescriptorId(valueProvider.getId())
                .withUpdateFunction(position -> {
                    ValueListElement value = elements.get(typeFixedValueProvider.getValueAt(position).getId());
                    if (value != null) {
                        ComboBoxElement entry = new ComboBoxElement(value.getId(), value.getText());
                        comboBox.getSelectionModel().select(entry);
                    }
                })
                .withVisibleNode(comboBox)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        comboBox.setOnAction(e -> {
            ComboBoxElement selectedItem = comboBox.getSelectionModel().getSelectedItem();
            valueProvider.keyframeAdded(timelineManager.getCurrentPosition(), selectedItem.getId());
        });

        return result;
    }

}

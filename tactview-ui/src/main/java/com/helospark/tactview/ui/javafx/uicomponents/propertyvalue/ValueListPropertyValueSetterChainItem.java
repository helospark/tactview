package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.HashMap;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

@Component
public class ValueListPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<ValueListProvider> {
    private static final int COMBOBOX_THRESHOLD = 3;
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

        if (typeFixedValueProvider.getElements().size() > COMBOBOX_THRESHOLD) {
            return createCombobox(typeFixedValueProvider);
        } else {
            return createRadioButtons(typeFixedValueProvider);
        }

    }

    private PrimitiveEffectLine createRadioButtons(ValueListProvider<ValueListElement> typeFixedValueProvider) {
        HBox box = new HBox();
        ToggleGroup group = new ToggleGroup();

        String currentlySelectedId = currentlySelectedId(typeFixedValueProvider.getId());

        Map<String, ValueListElement> elements = typeFixedValueProvider.getElements();
        Map<String, Toggle> toggleMap = new HashMap<>();
        for (var element : elements.entrySet()) {
            RadioButton radioButton = new RadioButton(element.getValue().getText());
            radioButton.setToggleGroup(group);
            radioButton.setUserData(element.getValue());
            if (element.getKey().equals(currentlySelectedId)) {
                radioButton.setSelected(true);
            }

            toggleMap.put(element.getKey(), radioButton);
            box.getChildren().add(radioButton);
        }

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> ((ValueListElement) group.getSelectedToggle().getUserData()).getId())
                .withDescriptorId(typeFixedValueProvider.getId())
                .withUpdateFunction(position -> {
                    String id = typeFixedValueProvider.getValueAt(position).getId();
                    if (id != null) {
                        Toggle toggle = toggleMap.get(id);
                        group.selectToggle(toggle);
                    }
                })
                .withVisibleNode(box)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        group.selectedToggleProperty().addListener((a, oldValue, newValue) -> {
            result.sendKeyframe(timelineManager.getCurrentPosition());
        });
        return result;
    }

    private PrimitiveEffectLine createCombobox(ValueListProvider<ValueListElement> typeFixedValueProvider) {
        Map<String, ValueListElement> elements = typeFixedValueProvider.getElements();
        ComboBox<ComboBoxElement> comboBox = new ComboBox<>();

        String currentlySelectedId = currentlySelectedId(typeFixedValueProvider.getId());

        Map<String, ComboBoxElement> comboBoxElements = new HashMap<>();

        elements.values()
                .stream()
                .forEach(a -> {
                    var entry = new ComboBoxElement(a.getId(), a.getText());
                    comboBox.getItems().add(entry);
                    comboBoxElements.put(a.getId(), entry);
                });

        comboBox.getSelectionModel().select(comboBoxElements.get(currentlySelectedId));

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> comboBox.getValue().getId())
                .withDescriptorId(typeFixedValueProvider.getId())
                .withUpdateFunction(position -> {
                    String value = typeFixedValueProvider.getValueAt(position).getId();
                    if (value != null) {
                        comboBox.getSelectionModel().select(comboBoxElements.get(value));
                    }
                })
                .withVisibleNode(comboBox)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        comboBox.setOnAction(e -> {
            result.sendKeyframe(timelineManager.getCurrentPosition());
        });
        return result;
    }

    public String currentlySelectedId(String id) {
        return effectParametersRepository.getValueAt(id, timelineManager.getCurrentPosition());
    }

}

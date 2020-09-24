package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.LinkedHashMap;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

@Component
public class ValueListPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<ValueListProvider> {
    private static final int COMBOBOX_THRESHOLD = 3;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;
    private ContextMenuAppender contextMenuAppender;

    public ValueListPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager, ContextMenuAppender contextMenuAppender) {
        super(ValueListProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(ValueListProvider valueProvider, ValueProviderDescriptor descriptor) {
        ValueListProvider<ValueListElement> typeFixedValueProvider = valueProvider; // thanks Java

        if (typeFixedValueProvider.getElements().size() > COMBOBOX_THRESHOLD) {
            return createCombobox(typeFixedValueProvider, descriptor);
        } else {
            return createRadioButtons(typeFixedValueProvider, descriptor);
        }

    }

    private PrimitiveEffectLine createRadioButtons(ValueListProvider<ValueListElement> typeFixedValueProvider, ValueProviderDescriptor descriptor) {
        HBox box = new HBox();
        ToggleGroup group = new ToggleGroup();

        String currentlySelectedId = currentlySelectedId(typeFixedValueProvider.getId());

        Map<String, ValueListElement> elements = typeFixedValueProvider.getElements();
        Map<String, Toggle> toggleMap = new LinkedHashMap<>();
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
                    String id = effectParametersRepository.getValueAt(typeFixedValueProvider.getId(), position);
                    if (id != null) {
                        Toggle toggle = toggleMap.get(id);
                        group.selectToggle(toggle);
                    }
                })
                .withVisibleNode(box)
                .withDescriptor(descriptor)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        group.selectedToggleProperty().addListener((a, oldValue, newValue) -> {
            result.sendKeyframe(timelineManager.getCurrentPosition());
        });

        contextMenuAppender.addContextMenu(result, typeFixedValueProvider, descriptor, box);

        return result;
    }

    private PrimitiveEffectLine createCombobox(ValueListProvider<ValueListElement> typeFixedValueProvider, ValueProviderDescriptor descriptor) {
        Map<String, ValueListElement> elements = typeFixedValueProvider.getElements();
        ComboBox<ComboBoxElement> comboBox = new ComboBox<>();

        String currentlySelectedId = currentlySelectedId(typeFixedValueProvider.getId());
        System.out.println("Current selected id " + currentlySelectedId);

        Map<String, ComboBoxElement> comboBoxElements = new LinkedHashMap<>();

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
                    String value = effectParametersRepository.getValueAt(typeFixedValueProvider.getId(), position);
                    if (value != null) {
                        comboBox.getSelectionModel().select(comboBoxElements.get(value));
                    }
                })
                .withDisabledUpdater(disabled -> comboBox.setDisable(disabled))
                .withVisibleNode(comboBox)
                .withDescriptor(descriptor)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        comboBox.setOnAction(e -> {
            result.sendKeyframe(timelineManager.getCurrentPosition());
        });

        contextMenuAppender.addContextMenu(result, typeFixedValueProvider, descriptor, comboBox);

        // Do not trigger combobox dropdown to allow context-menu to be viewed
        comboBox.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                e.consume();
            }
        });

        return result;
    }

    public String currentlySelectedId(String id) {
        return effectParametersRepository.getValueAt(id, timelineManager.getCurrentPosition());
    }

}

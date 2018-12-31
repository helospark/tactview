package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;

import javafx.scene.control.TextArea;

@Component
public class StringPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<StringProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;

    public StringPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager) {
        super(StringProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
    }

    @Override
    protected EffectLine handle(StringProvider stringProvider, ValueProviderDescriptor descriptor) {
        TextArea textArea = new TextArea();
        textArea.getStyleClass().add("string-property-field");
        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textArea.getText())
                .withDescriptorId(stringProvider.getId())
                .withUpdateFunction(position -> {
                    String currentValue = stringProvider.getValueAt(position);
                    if (!textArea.isFocused()) { // otherwise user may want to type
                        textArea.setText(currentValue);
                    }
                })
                .withDescriptor(descriptor)
                .withVisibleNode(textArea)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        textArea.setOnKeyReleased(newValue -> {
            TimelinePosition position = timelineManager.getCurrentPosition();
            String currentValue = stringProvider.getValueAt(position);
            if (!textArea.getText().equals(currentValue)) {
                result.sendKeyframe(position);
            }
        });

        return result;
    }

}

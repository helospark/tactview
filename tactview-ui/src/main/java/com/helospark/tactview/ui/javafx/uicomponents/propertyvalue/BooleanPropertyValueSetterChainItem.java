package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.CheckBox;

@Component
public class BooleanPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<BooleanProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private GlobalTimelinePositionHolder timelineManager;
    private ContextMenuAppender contextMenuAppender;

    public BooleanPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, GlobalTimelinePositionHolder timelineManager, ContextMenuAppender contextMenuAppender) {
        super(BooleanProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(BooleanProvider booleanProvider, ValueProviderDescriptor descriptor) {
        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("boolean-property-field");

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> checkbox.isSelected())
                .withDescriptorId(booleanProvider.getId())
                .withUpdateFunction(position -> checkbox.setSelected(providerValueToString(booleanProvider.getId(), position)))
                .withVisibleNode(checkbox)
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .build();

        checkbox.setOnAction(event -> {
            KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                    .withDescriptorId(booleanProvider.getId())
                    .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                    .withValue(checkbox.isSelected())
                    .withRevertable(true)
                    .build();
            commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
        });
        contextMenuAppender.addContextMenu(result, booleanProvider, descriptor, checkbox);

        return result;

    }

    private boolean providerValueToString(String id, TimelinePosition position) {
        return Boolean.parseBoolean(effectParametersRepository.getValueAt(id, position));
    }

}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ExpressionChangedForPropertyCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.TextArea;

@Component
public class StringPropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<StringProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private GlobalTimelinePositionHolder timelineManager;
    private ContextMenuAppender contextMenuAppender;

    public StringPropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, GlobalTimelinePositionHolder timelineManager, ContextMenuAppender contextMenuAppender) {
        super(StringProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(StringProvider stringProvider, ValueProviderDescriptor descriptor) {
        TextArea textArea = new TextArea();
        textArea.getStyleClass().add("string-property-field");
        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textArea.getText())
                .withDescriptorId(stringProvider.getId())
                .withUpdateFunction(position -> {
                    if (!textArea.isFocused()) { // otherwise user may want to type
                        if (stringProvider.getExpression() == null) {
                            String currentValue = stringProvider.getValueAt(position);
                            textArea.setText(currentValue);
                        } else {
                            textArea.setText(stringProvider.getExpression());
                        }
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
            if (textArea.getText() != null && !textArea.getText().equals(currentValue)) {
                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(stringProvider.getId())
                        .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                        .withValue(textArea.getText())
                        .withRevertable(true)
                        .build();

                if (stringProvider.getExpression() != null) {
                    commandInterpreter.sendWithResult(new ExpressionChangedForPropertyCommand(effectParametersRepository, keyframeRequest));
                } else {
                    commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                }
            }
        });

        contextMenuAppender.addContextMenu(result, stringProvider, descriptor, textArea);

        return result;
    }

}

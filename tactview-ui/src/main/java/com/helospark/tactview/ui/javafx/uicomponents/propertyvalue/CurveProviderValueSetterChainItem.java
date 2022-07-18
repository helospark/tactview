package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KnotAwareUnivariateFunction;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.control.CurveWidget;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

@Component
public class CurveProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<CurveProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private GlobalTimelinePositionHolder uiTimelineManager;
    private InputModeRepository inputModeRepository;
    private ContextMenuAppender contextMenuAppender;

    public CurveProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, GlobalTimelinePositionHolder uiTimelineManager, InputModeRepository inputModeRepository, ContextMenuAppender contextMenuAppender) {
        super(CurveProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.inputModeRepository = inputModeRepository;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(CurveProvider colorProvider, ValueProviderDescriptor descriptor) {
        CurveWidget control = new CurveWidget(colorProvider.getValueAt(uiTimelineManager.getCurrentPosition()),
                colorProvider.getMinX(),
                colorProvider.getMaxX(),
                colorProvider.getMinY(),
                colorProvider.getMaxY());
        control.setPrefWidth(200);
        control.setPrefHeight(150);

        PrimitiveEffectLine result = PrimitiveEffectLine
                .builder()
                .withVisibleNode(control)
                .withDescriptorId(colorProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFunction(value -> {
                    KnotAwareUnivariateFunction curve = (KnotAwareUnivariateFunction) effectParametersRepository.getValueAtAsObject(colorProvider.getId(), value);
                    control.getCurveProperty().set(curve);
                })
                .withUpdateFromValue(value -> {
                    KnotAwareUnivariateFunction curve = (KnotAwareUnivariateFunction) value;
                    control.getCurveProperty().set(curve);
                })
                .build();

        control.onActionProperty()
                .addListener((e, oldValue, newValue) -> {
                    if (newValue != null) {
                        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                                .withDescriptorId(colorProvider.getId())
                                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                                .withValue(newValue)
                                .withRevertable(true)
                                .build();

                        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                    }
                });

        contextMenuAppender.addContextMenu(result, colorProvider, descriptor, control);

        return result;
    }

}

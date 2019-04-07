package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KnotAwareUnivariateFunction;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.control.CurveWidget;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

@Component
public class CurveProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<CurveProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager uiTimelineManager;
    private InputModeRepository inputModeRepository;
    private ContextMenuAppender contextMenuAppender;

    public CurveProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, UiTimelineManager uiTimelineManager, InputModeRepository inputModeRepository, ContextMenuAppender contextMenuAppender) {
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
                        try {
                            result.sendKeyframeWithValue(uiTimelineManager.getCurrentPosition(), new ObjectMapper().writeValueAsString(newValue));
                        } catch (JsonProcessingException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        //        ObjectProperty<javafx.scene.paint.Color> property = control.onActionProperty();
        //        property.addListener((e, oldValue, newValue) -> {
        //            javafx.scene.paint.Color color = newValue;
        //            //            redProvider.updateFromValue.accept(color.getRed());
        //            //            greenProvider.updateFromValue.accept(color.getGreen());
        //            //            blueProvider.updateFromValue.accept(color.getBlue());
        //            result.sendKeyframeWithValue(uiTimelineManager.getCurrentPosition(), "");
        //        });

        contextMenuAppender.addContextMenu(result, colorProvider, descriptor, control);

        return result;
    }

}

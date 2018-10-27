package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;

import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class ColorProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<ColorProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;

    public ColorProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager) {
        super(ColorProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    protected EffectLine handle(ColorProvider lineProvider) {
        PrimitiveEffectLine redProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(lineProvider.getChildren().get(0));
        PrimitiveEffectLine greenProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(lineProvider.getChildren().get(1));
        PrimitiveEffectLine blueProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(lineProvider.getChildren().get(2));

        HBox hbox = new HBox();
        VBox vbox = new VBox();

        vbox.getChildren().add(redProvider.getVisibleNode());
        vbox.getChildren().add(greenProvider.getVisibleNode());
        vbox.getChildren().add(blueProvider.getVisibleNode());

        ColorPicker colorPicker = new ColorPicker();

        hbox.getChildren().addAll(vbox, colorPicker);

        CompositeEffectLine result = CompositeEffectLine
                .builder()
                .withVisibleNode(hbox)
                .withValues(List.of(redProvider, greenProvider, blueProvider))
                .withDescriptorId(lineProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withAdditionalUpdateUi(value -> colorPicker.setValue(new javafx.scene.paint.Color(effectLineToDouble(redProvider),
                        effectLineToDouble(greenProvider),
                        effectLineToDouble(blueProvider), 1.0)))
                .withUpdateFromValue(value -> {
                    Color line = (Color) value;
                    redProvider.getUpdateFromValue().accept(line.red);
                    greenProvider.getUpdateFromValue().accept(line.green);
                    blueProvider.getUpdateFromValue().accept(line.blue);
                })
                .build();

        colorPicker.setOnAction(e -> {
            javafx.scene.paint.Color color = colorPicker.getValue();
            redProvider.updateFromValue.accept(color.getRed());
            greenProvider.updateFromValue.accept(color.getGreen());
            blueProvider.updateFromValue.accept(color.getBlue());
            result.sendKeyframe(uiTimelineManager.getCurrentPosition());
        });

        return result;
    }

    private double effectLineToDouble(PrimitiveEffectLine provider) {
        try {
            return Double.valueOf(provider.currentValueProvider.get());
        } catch (Exception e) {
            return 0.0;
        }
    }

}

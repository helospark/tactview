package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.ColorPickerType;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.control.ColorWheelPicker;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;

@Component
public class ColorProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<ColorProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager uiTimelineManager;
    private InputModeRepository inputModeRepository;

    public ColorProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, UiTimelineManager uiTimelineManager, InputModeRepository inputModeRepository) {
        super(ColorProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.inputModeRepository = inputModeRepository;
    }

    @Override
    protected EffectLine handle(ColorProvider lineProvider, ValueProviderDescriptor descriptor) {
        PrimitiveEffectLine redProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, lineProvider.getChildren().get(0));
        PrimitiveEffectLine greenProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, lineProvider.getChildren().get(1));
        PrimitiveEffectLine blueProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, lineProvider.getChildren().get(2));

        Map<Object, Object> renderHints = descriptor.getRenderHints();

        if (Objects.equals(renderHints.get(RenderTypeHint.TYPE), ColorPickerType.CIRCLE)) {
            ColorWheelPicker control = new ColorWheelPicker();
            control.setPrefWidth(150);
            control.setPrefHeight(150);

            CompositeEffectLine result = CompositeEffectLine
                    .builder()
                    .withVisibleNode(control)
                    .withValues(List.of(redProvider, greenProvider, blueProvider))
                    .withDescriptorId(lineProvider.getId())
                    .withEffectParametersRepository(effectParametersRepository)
                    .withCommandInterpreter(commandInterpreter)
                    .withDescriptor(descriptor)
                    .withAdditionalUpdateUi(value -> control.setValue(new javafx.scene.paint.Color(effectLineToDouble(redProvider),
                            effectLineToDouble(greenProvider),
                            effectLineToDouble(blueProvider), 1.0)))
                    .withUpdateFromValue(value -> {
                        Color line = (Color) value;
                        redProvider.getUpdateFromValue().accept(line.red);
                        greenProvider.getUpdateFromValue().accept(line.green);
                        blueProvider.getUpdateFromValue().accept(line.blue);
                    })
                    .build();

            ObjectProperty<javafx.scene.paint.Color> property = control.onActionProperty();
            property.addListener((e, oldValue, newValue) -> {
                javafx.scene.paint.Color color = newValue;
                redProvider.updateFromValue.accept(color.getRed());
                greenProvider.updateFromValue.accept(color.getGreen());
                blueProvider.updateFromValue.accept(color.getBlue());
                result.sendKeyframe(uiTimelineManager.getCurrentPosition());
            });

            return result;
        } else { // TODO: many duplications due to separate interfaces
            ColorPicker colorPicker = new ColorPicker();
            GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
            Button colorPickerInputButton = new Button("", fontAwesome.create('\uf1fb'));

            HBox hbox = new HBox();
            hbox.getChildren().add(colorPicker);
            hbox.getChildren().add(colorPickerInputButton);

            CompositeEffectLine result = CompositeEffectLine
                    .builder()
                    .withVisibleNode(hbox)
                    .withValues(List.of(redProvider, greenProvider, blueProvider))
                    .withDescriptorId(lineProvider.getId())
                    .withEffectParametersRepository(effectParametersRepository)
                    .withCommandInterpreter(commandInterpreter)
                    .withDescriptor(descriptor)
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
            colorPickerInputButton.setOnMouseClicked(event -> inputModeRepository.requestColor(color -> {
                redProvider.updateFromValue.accept(color.red);
                greenProvider.updateFromValue.accept(color.green);
                blueProvider.updateFromValue.accept(color.blue);
                result.sendKeyframe(uiTimelineManager.getCurrentPosition());
            }));

            return result;
        }
    }

    private double effectLineToDouble(PrimitiveEffectLine provider) {
        try {
            return Double.valueOf(provider.currentValueProvider.get());
        } catch (Exception e) {
            return 0.0;
        }
    }

}

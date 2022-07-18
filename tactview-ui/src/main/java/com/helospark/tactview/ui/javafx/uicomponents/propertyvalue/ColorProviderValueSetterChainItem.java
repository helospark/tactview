package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.ColorPickerType;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.control.ColorWheelPicker;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

@Component
public class ColorProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<ColorProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private GlobalTimelinePositionHolder uiTimelineManager;
    private InputModeRepository inputModeRepository;
    private ContextMenuAppender contextMenuAppender;

    public ColorProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, GlobalTimelinePositionHolder uiTimelineManager, InputModeRepository inputModeRepository, ContextMenuAppender contextMenuAppender) {
        super(ColorProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.inputModeRepository = inputModeRepository;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(ColorProvider colorProvider, ValueProviderDescriptor descriptor) {
        PrimitiveEffectLine redProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, colorProvider.getChildren().get(0));
        PrimitiveEffectLine greenProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, colorProvider.getChildren().get(1));
        PrimitiveEffectLine blueProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, colorProvider.getChildren().get(2));

        Map<Object, Object> renderHints = descriptor.getRenderHints();

        if (Objects.equals(renderHints.get(RenderTypeHint.TYPE), ColorPickerType.CIRCLE)) {
            ColorWheelPicker control = new ColorWheelPicker();
            control.setPrefWidth(150);
            control.setPrefHeight(150);

            CompositeEffectLine result = CompositeEffectLine
                    .builder()
                    .withVisibleNode(control)
                    .withValues(List.of(redProvider, greenProvider, blueProvider))
                    .withDescriptorId(colorProvider.getId())
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
                if (control.isListenersDisabled()) {
                    return;
                }

                javafx.scene.paint.Color color = newValue;
                Color keyframeColor = new Color(color.getRed(), color.getGreen(), color.getBlue());

                boolean revertable = !control.onValueChangingProperty().get();
                Optional<Color> previousColor = Optional.empty();
                if (revertable) {
                    previousColor = Optional.ofNullable(new Color(oldValue.getRed(), oldValue.getGreen(), oldValue.getBlue()));
                }

                addKeyframe(colorProvider, control, keyframeColor, revertable, previousColor);
            });
            control.onValueChangingProperty().addListener((e, oldValue, newValue) -> {
                if (control.isListenersDisabled()) {
                    return;
                }
                if (oldValue && !newValue) {
                    System.out.println("Not chaning anymore");
                    javafx.scene.paint.Color color = control.colorProperty().get();
                    Color keyframeColor = new Color(color.getRed(), color.getGreen(), color.getBlue());

                    Optional<Color> previousColor = Optional.ofNullable(control.getColorChangeStart())
                            .map(javafxColor -> new Color(javafxColor.getRed(), javafxColor.getGreen(), javafxColor.getBlue()));

                    addKeyframe(colorProvider, control, keyframeColor, true, previousColor);
                }
            });

            contextMenuAppender.addContextMenu(result, colorProvider, descriptor, control);

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
                    .withDescriptorId(colorProvider.getId())
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
                Color keyframeColor = new Color(colorPicker.getValue().getRed(), colorPicker.getValue().getGreen(), colorPicker.getValue().getBlue());

                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(colorProvider.getId())
                        .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                        .withValue(keyframeColor)
                        .withRevertable(true)
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));

            });
            colorPickerInputButton.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    Color previousValue = colorProvider.getValueAt(uiTimelineManager.getCurrentPosition());
                    inputModeRepository.requestColor(color -> {
                        boolean revertable = this.inputModeRepository.getResultType().equals(ResultType.DONE);

                        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                                .withDescriptorId(colorProvider.getId())
                                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                                .withValue(color)
                                .withRevertable(revertable)
                                .withPreviousValue(Optional.of(previousValue))
                                .build();

                        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                    });
                }
            });

            contextMenuAppender.addContextMenu(result, colorProvider, descriptor, hbox);

            // Do not trigger colorPicker to allow context-menu to be viewed
            colorPicker.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
                if (e.getButton().equals(MouseButton.SECONDARY)) {
                    e.consume();
                }
            });

            return result;
        }
    }

    private void addKeyframe(ColorProvider colorProvider, ColorWheelPicker control, Color keyframeColor, boolean revertable, Optional<Color> previousColor) {

        System.out.println("Previous color " + previousColor);

        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(colorProvider.getId())
                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                .withValue(keyframeColor)
                .withRevertable(revertable)
                .withPreviousValue((Optional<Object>) (Object) previousColor)
                .build();

        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
    }

    private double effectLineToDouble(PrimitiveEffectLine provider) {
        return (double) provider.currentValueSupplier.get();
    }

}

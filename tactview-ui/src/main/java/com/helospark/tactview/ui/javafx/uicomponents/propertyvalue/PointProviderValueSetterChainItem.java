package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.Optional;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.MovementType;
import com.helospark.tactview.core.timeline.effect.interpolation.hint.RenderTypeHint;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

@Component
public class PointProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<PointProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;
    private ContextMenuAppender contextMenuAppender;

    public PointProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager, ContextMenuAppender contextMenuAppender) {
        super(PointProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(PointProvider pointProvider, ValueProviderDescriptor descriptor) {
        PrimitiveEffectLine xProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, pointProvider.getxProvider());
        PrimitiveEffectLine yProvider = (PrimitiveEffectLine) doublePropertyValueSetterChainItem.create(descriptor, pointProvider.getyProvider());
        Button button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));

        HBox box = new HBox();
        box.getChildren().add(xProvider.getVisibleNode());
        box.getChildren().add(yProvider.getVisibleNode());
        box.getChildren().add(button);

        CompositeEffectLine result = CompositeEffectLine
                .builder()
                .withVisibleNode(box)
                .withValues(List.of(xProvider, yProvider))
                .withDescriptorId(pointProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFromValue(value -> {
                    Point point = (Point) value;
                    xProvider.getUpdateFromValue().accept(point.x);
                    yProvider.getUpdateFromValue().accept(point.y);
                })
                .withCurrentValueSupplier(() -> new Point(Double.valueOf(xProvider.currentValueProvider.get()), Double.valueOf(yProvider.currentValueProvider.get())))
                .build();

        button.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Object renderHint = descriptor.getRenderHints().get(RenderTypeHint.TYPE);
                Point previousValue = pointProvider.getValueAt(uiTimelineManager.getCurrentPosition());
                if (renderHint != null && renderHint.equals(MovementType.RELATIVE)) {
                    inputModeRepository.requestRelativePoint(point -> {
                        sendKeyframe(pointProvider, point, previousValue);
                    }, pointProvider.getSizeFunction(), (Point) result.getCurrentValue());
                } else {
                    inputModeRepository.requestPoint(point -> {
                        sendKeyframe(pointProvider, point, previousValue);
                    }, pointProvider.getSizeFunction());
                }
            }
        });

        contextMenuAppender.addContextMenu(result, pointProvider, descriptor, button);

        return result;
    }

    private void sendKeyframe(PointProvider pointProvider, Point currentPoint, Point previousValue) {
        boolean revertable = this.inputModeRepository.getResultType().equals(ResultType.DONE);

        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(pointProvider.getId())
                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                .withValue(currentPoint)
                .withRevertable(revertable)
                .withPreviousValue(Optional.ofNullable(previousValue))
                .build();

        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
    }

}

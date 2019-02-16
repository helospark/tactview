package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class LineProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<LineProvider> {
    private PointProviderValueSetterChainItem pointProviderValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;
    private ContextMenuAppender contextMenuAppender;

    public LineProviderValueSetterChainItem(PointProviderValueSetterChainItem pointProviderValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager, ContextMenuAppender contextMenuAppender) {
        super(LineProvider.class);
        this.pointProviderValueSetterChainItem = pointProviderValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(LineProvider lineProvider, ValueProviderDescriptor descriptor) {
        CompositeEffectLine startPointProvider = (CompositeEffectLine) pointProviderValueSetterChainItem.create(descriptor, lineProvider.getChildren().get(0));
        CompositeEffectLine endPointProvider = (CompositeEffectLine) pointProviderValueSetterChainItem.create(descriptor, lineProvider.getChildren().get(1));
        Button button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.SQUARE));

        VBox vbox = new VBox();
        vbox.getChildren().add(startPointProvider.getVisibleNode());
        vbox.getChildren().add(endPointProvider.getVisibleNode());
        HBox hbox = new HBox();
        hbox.getChildren().add(vbox);
        hbox.getChildren().add(button);

        CompositeEffectLine result = CompositeEffectLine
                .builder()
                .withVisibleNode(hbox)
                .withValues(List.of(startPointProvider, endPointProvider))
                .withDescriptorId(lineProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFromValue(value -> {
                    InterpolationLine line = (InterpolationLine) value;
                    startPointProvider.getUpdateFromValue().accept(line.start);
                    endPointProvider.getUpdateFromValue().accept(line.end);
                })
                .withCurrentValueSupplier(() -> new InterpolationLine((Point) startPointProvider.getCurrentValue(), (Point) endPointProvider.getCurrentValue()))
                .build();

        button.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                inputModeRepository.requestLine(line -> {
                    startPointProvider.getUpdateFromValue().accept(line.start);
                    endPointProvider.getUpdateFromValue().accept(line.end);
                    result.sendKeyframe(uiTimelineManager.getCurrentPosition());
                }, (InterpolationLine) result.getCurrentValue(), lineProvider.getSizeFunction());
            }
        });

        contextMenuAppender.addContextMenu(result, lineProvider, descriptor, button);

        return result;
    }

}

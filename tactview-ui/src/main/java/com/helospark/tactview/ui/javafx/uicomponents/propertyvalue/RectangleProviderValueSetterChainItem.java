package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Rectangle;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.RectangleProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class RectangleProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<RectangleProvider> {
    private PointProviderValueSetterChainItem pointProviderValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;
    private ContextMenuAppender contextMenuAppender;

    public RectangleProviderValueSetterChainItem(PointProviderValueSetterChainItem pointProviderValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager, ContextMenuAppender contextMenuAppender) {
        super(RectangleProvider.class);
        this.pointProviderValueSetterChainItem = pointProviderValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(RectangleProvider rectangleProvider, ValueProviderDescriptor descriptor) {
        List<EffectLine> pointProviders = new ArrayList<>();

        for (int i = 0; i < 4; ++i) {
            pointProviders.add(pointProviderValueSetterChainItem.create(descriptor, rectangleProvider.getChildren().get(i)));
        }
        Button button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.SQUARE));

        VBox vbox = new VBox();
        pointProviders.stream()
                .forEach(a -> vbox.getChildren().add(a.visibleNode));
        HBox hbox = new HBox();
        hbox.getChildren().add(vbox);
        hbox.getChildren().add(button);

        CompositeEffectLine result = CompositeEffectLine
                .builder()
                .withVisibleNode(hbox)
                .withValues(pointProviders)
                .withDescriptorId(rectangleProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFromValue(value -> {
                    Rectangle line = (Rectangle) value;
                    for (int i = 0; i < 4; ++i) {
                        pointProviders.get(i).getUpdateFromValue().accept(line.points.get(i));
                    }
                })
                .build();

        button.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                inputModeRepository.requestRectangle(rectangle -> {
                    for (int i = 0; i < 4; ++i) {
                        pointProviders.get(i).getUpdateFromValue().accept(rectangle.points.get(i));
                    }
                    result.sendKeyframe(uiTimelineManager.getCurrentPosition());
                }, getCurrentValue(pointProviders), rectangleProvider.getSizeFunction());
            }
        });

        contextMenuAppender.addContextMenu(result, rectangleProvider, descriptor, button);

        return result;
    }

    private List<Point> getCurrentValue(List<EffectLine> aPointProvider) {
        return aPointProvider.stream()
                .map(a -> (Point) ((CompositeEffectLine) a).getCurrentValue())
                .collect(Collectors.toList());
    }

}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Line;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.LineProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;

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

    public LineProviderValueSetterChainItem(PointProviderValueSetterChainItem pointProviderValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager) {
        super(LineProvider.class);
        this.pointProviderValueSetterChainItem = pointProviderValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    protected EffectLine handle(LineProvider lineProvider) {
        EffectLine startPointProvider = pointProviderValueSetterChainItem.create(lineProvider.getChildren().get(0));
        EffectLine endPointProvider = pointProviderValueSetterChainItem.create(lineProvider.getChildren().get(1));
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
                .withUpdateFromValue(value -> {
                    Line line = (Line) value;
                    startPointProvider.getUpdateFromValue().accept(line.start);
                    endPointProvider.getUpdateFromValue().accept(line.end);
                })
                .build();

        button.setOnMouseClicked(event -> inputModeRepository.requestLine(line -> {
            startPointProvider.getUpdateFromValue().accept(line.start);
            endPointProvider.getUpdateFromValue().accept(line.end);
            result.sendKeyframe(uiTimelineManager.getCurrentPosition());
        }, lineProvider.getSizeFunction()));

        return result;
    }

}

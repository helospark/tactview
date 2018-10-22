package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

@Component
public class PointProviderValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<PointProvider> {
    private DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;

    public PointProviderValueSetterChainItem(DoublePropertyValueSetterChainItem doublePropertyValueSetterChainItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager) {
        super(PointProvider.class);
        this.doublePropertyValueSetterChainItem = doublePropertyValueSetterChainItem;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    protected EffectLine handle(PointProvider pointProvider) {
        EffectLine xProvider = doublePropertyValueSetterChainItem.create(pointProvider.getxProvider());
        EffectLine yProvider = doublePropertyValueSetterChainItem.create(pointProvider.getyProvider());
        Button button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));

        HBox box = new HBox();
        box.getChildren().add(xProvider.getVisibleNode());
        box.getChildren().add(yProvider.getVisibleNode());
        box.getChildren().add(button);

        PointEffectLine result = PointEffectLine
                .builder()
                .withVisibleNode(box)
                .withXCoordinate(xProvider)
                .withYCoordinate(yProvider)
                .withDescriptorId(pointProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .build();

        button.setOnMouseClicked(event -> inputModeRepository.requestPoint(point -> {
            xProvider.getUpdateFromValue().accept(point.x);
            yProvider.getUpdateFromValue().accept(point.y);
            result.sendKeyframe(uiTimelineManager.getCurrentPosition());
        }));

        return result;
    }

}

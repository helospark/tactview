package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;

import javafx.scene.control.Button;

@Component
public class PolygonValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<PolygonProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;

    public PolygonValueSetterChainItem(UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager) {
        super(PolygonProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    protected EffectLine handle(PolygonProvider polygonProvider, ValueProviderDescriptor descriptor) {
        Button button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.DIAMOND));
        Polygon resultPolygon = new Polygon();

        PrimitiveEffectLine result = PrimitiveEffectLine
                .builder()
                .withCurrentValueProvider(() -> resultPolygon.serializeToString())
                .withVisibleNode(button)
                .withDescriptorId(polygonProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFunction(position -> {
                    // TODO
                })
                .withUpdateFromValue(value -> {
                    // TODO
                })
                .build();

        button.setOnMouseClicked(event -> inputModeRepository.requestPolygon(polygon -> {
            result.sendKeyframe(uiTimelineManager.getCurrentPosition());
            resultPolygon.copyFrom(polygon);
        }, polygonProvider.getSizeFunction()));

        return result;
    }

}

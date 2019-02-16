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

        PrimitiveEffectLine result = PrimitiveEffectLine
                .builder()
                .withCurrentValueProvider(() -> "???")
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

        button.setOnMouseClicked(event -> {
            if (event.isPrimaryButtonDown()) {
                Polygon currentPolygon = (Polygon) effectParametersRepository.getValueAtAsObject(polygonProvider.getId(), uiTimelineManager.getCurrentPosition());
                if (currentPolygon.getPoints().isEmpty()) {
                    inputModeRepository.requestPolygon(polygon -> {
                        result.sendKeyframeWithValue(uiTimelineManager.getCurrentPosition(), polygon.serializeToString());
                    }, polygonProvider.getSizeFunction());
                } else {
                    inputModeRepository.requestPolygonPrefilled(polygon -> {
                        result.sendKeyframeWithValue(uiTimelineManager.getCurrentPosition(), polygon.serializeToString());
                    }, polygonProvider.getSizeFunction(), currentPolygon.getPoints());
                }
            }
        });

        return result;
    }

}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Optional;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Polygon;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PolygonProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;

@Component
public class PolygonValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<PolygonProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private GlobalTimelinePositionHolder globalTimelinePositionHolder;

    public PolygonValueSetterChainItem(UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, GlobalTimelinePositionHolder globalTimelinePositionHolder) {
        super(PolygonProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.globalTimelinePositionHolder = globalTimelinePositionHolder;
    }

    @Override
    protected EffectLine handle(PolygonProvider polygonProvider, ValueProviderDescriptor descriptor) {
        Button button = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.DIAMOND));

        PrimitiveEffectLine result = PrimitiveEffectLine
                .builder()
                .withCurrentValueProvider(() -> effectParametersRepository.getValueAtAsObject(polygonProvider.getId(), globalTimelinePositionHolder.getCurrentPosition()))
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
            if (event.getButton() == MouseButton.PRIMARY) {
                Polygon currentPolygon = (Polygon) effectParametersRepository.getValueAtAsObject(polygonProvider.getId(), globalTimelinePositionHolder.getCurrentPosition());
                if (currentPolygon.getPoints().isEmpty()) {
                    inputModeRepository.requestPolygon(polygon -> {
                        boolean revertable = this.inputModeRepository.getResultType().equals(ResultType.DONE);

                        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                                .withDescriptorId(polygonProvider.getId())
                                .withGlobalTimelinePosition(globalTimelinePositionHolder.getCurrentPosition())
                                .withValue(polygon)
                                .withRevertable(revertable)
                                .withPreviousValue(Optional.of(currentPolygon))
                                .build();

                        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                    }, polygonProvider.getSizeFunction());
                } else {
                    inputModeRepository.requestPolygonPrefilled(polygon -> {

                        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                                .withDescriptorId(polygonProvider.getId())
                                .withGlobalTimelinePosition(globalTimelinePositionHolder.getCurrentPosition())
                                .withValue(polygon)
                                .withRevertable(true)
                                .build();

                        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                    }, polygonProvider.getSizeFunction(), currentPolygon.getPoints());
                }
            }
        });

        return result;
    }

}

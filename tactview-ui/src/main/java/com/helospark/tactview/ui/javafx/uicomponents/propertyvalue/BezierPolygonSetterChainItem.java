package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.Optional;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BezierPolygonProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygon;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygonPoint;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;

@Component
public class BezierPolygonSetterChainItem extends TypeBasedPropertyValueSetterChainItem<BezierPolygonProvider> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private UiTimelineManager uiTimelineManager;

    public BezierPolygonSetterChainItem(UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, UiTimelineManager uiTimelineManager) {
        super(BezierPolygonProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.inputModeRepository = inputModeRepository;
        this.uiTimelineManager = uiTimelineManager;
    }

    @Override
    protected EffectLine handle(BezierPolygonProvider polygonProvider, ValueProviderDescriptor descriptor) {
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
            if (event.getButton() == MouseButton.PRIMARY) {
                BezierPolygon currentPolygon = (BezierPolygon) effectParametersRepository.getValueAtAsObject(polygonProvider.getId(), uiTimelineManager.getCurrentPosition());
                if (currentPolygon.getPoints().isEmpty()) {
                    inputModeRepository.requestBezierPolygon(polygon -> {
                        boolean revertable = this.inputModeRepository.getResultType().equals(ResultType.DONE);

                        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                                .withDescriptorId(polygonProvider.getId())
                                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                                .withValue(polygon)
                                .withRevertable(revertable)
                                .withPreviousValue(Optional.of(currentPolygon))
                                .build();
                        commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));
                    }, polygonProvider.getSizeFunction());
                } else {
                    inputModeRepository.requestBezierPolygonPrefilled(polygon -> {
                        result.sendKeyframeWithValue(uiTimelineManager.getCurrentPosition(), getAsString(polygon.points));
                    }, polygonProvider.getSizeFunction(), currentPolygon.getPoints());
                }
            }
        });

        return result;
    }

    private String getAsString(List<BezierPolygonPoint> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

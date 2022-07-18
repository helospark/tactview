package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.Optional;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BezierPolygonProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.proceduralclip.polygon.impl.bezier.BezierPolygon;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.inputmode.InputModeRepository;
import com.helospark.tactview.ui.javafx.inputmode.strategy.ResultType;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;

@Component
public class BezierPolygonSetterChainItem extends TypeBasedPropertyValueSetterChainItem<BezierPolygonProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private InputModeRepository inputModeRepository;
    private GlobalTimelinePositionHolder uiTimelineManager;

    public BezierPolygonSetterChainItem(UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, InputModeRepository inputModeRepository, GlobalTimelinePositionHolder uiTimelineManager) {
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
                .withCurrentValueProvider(() -> effectParametersRepository.getValueAtAsObject(polygonProvider.getId(), uiTimelineManager.getCurrentPosition()))
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

                        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                                .withDescriptorId(polygonProvider.getId())
                                .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
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

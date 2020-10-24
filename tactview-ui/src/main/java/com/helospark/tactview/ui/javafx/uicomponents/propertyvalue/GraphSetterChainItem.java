package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.GraphingDialog;

import javafx.scene.control.Button;

@Component
public class GraphSetterChainItem extends TypeBasedPropertyValueSetterChainItem<GraphProvider> {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager uiTimelineManager;
    private GraphingDialog graphingDialog;

    public GraphSetterChainItem(UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, UiTimelineManager uiTimelineManager, GraphingDialog graphingDialog) {
        super(GraphProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.graphingDialog = graphingDialog;
    }

    @Override
    protected EffectLine handle(GraphProvider graphingProvider, ValueProviderDescriptor descriptor) {
        Button button = new Button("Open graph", new Glyph("FontAwesome", FontAwesome.Glyph.DIAMOND));

        PrimitiveEffectLine result = PrimitiveEffectLine
                .builder()
                .withCurrentValueProvider(() -> effectParametersRepository.getValueAtAsObject(graphingProvider.getId(), uiTimelineManager.getCurrentPosition()))
                .withVisibleNode(button)
                .withDescriptorId(graphingProvider.getId())
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
            graphingDialog.open(graphingProvider);
        });

        return result;
    }

}

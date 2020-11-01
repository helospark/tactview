package com.helospark.tactview.core.timeline.effect.graphing;

import java.util.ArrayList;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory.GraphCreatorRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.InputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.OutputElement;

@Component
public class DefaultGraphArrangementFactory {
    private GraphElementFactory inputElementFactory;
    private GraphElementFactory outputElementFactory;

    public DefaultGraphArrangementFactory(@Qualifier("inputGraphElementFactory") GraphElementFactory inputElementFactory,
            @Qualifier("outputGraphElementFactory") GraphElementFactory outputElementFactory) {
        this.inputElementFactory = inputElementFactory;
        this.outputElementFactory = outputElementFactory;
    }

    public EffectGraph createEffectGraphProviderWithInputAndOutput() {
        EffectGraph effectGraph = new EffectGraph();

        InputElement inputElement = (InputElement) inputElementFactory.createElement(new GraphCreatorRequest(null, "input"));
        OutputElement outputElement = (OutputElement) outputElementFactory.createElement(new GraphCreatorRequest(null, "output"));

        effectGraph.getGraphElements().put(GraphIndex.random(), inputElement);
        effectGraph.getGraphElements().put(GraphIndex.random(), outputElement);
        effectGraph.getConnections().put(inputElement.getOutputIndex(), new ArrayList<>(List.of(outputElement.getInputIndex())));
        effectGraph.autoArrangeUi();

        return effectGraph;
    }

    public EffectGraph createEffectGraphProviderWithOutput() {
        EffectGraph effectGraph = new EffectGraph();

        OutputElement outputElement = (OutputElement) outputElementFactory.createElement(new GraphCreatorRequest(null, "output"));

        effectGraph.getGraphElements().put(GraphIndex.random(), outputElement);
        effectGraph.autoArrangeUi();

        return effectGraph;
    }

}

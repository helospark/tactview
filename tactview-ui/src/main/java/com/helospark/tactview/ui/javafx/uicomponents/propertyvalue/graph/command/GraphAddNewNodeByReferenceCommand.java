package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class GraphAddNewNodeByReferenceCommand implements UiCommand {
    private GraphProvider provider;
    private EffectGraphAccessor effectGraphAccessor;

    private GraphElement graphElement;

    private GraphIndex graphAddedNode;

    public GraphAddNewNodeByReferenceCommand(GraphProvider provider, EffectGraphAccessor effectGraphAccessor, GraphElement graphElement) {
        this.provider = provider;
        this.effectGraphAccessor = effectGraphAccessor;
        this.graphElement = graphElement;
    }

    @Override
    public void execute() {
        graphAddedNode = effectGraphAccessor.addNode(provider, graphElement);
    }

    @Override
    public void revert() {
        effectGraphAccessor.removeElementById(provider, graphAddedNode);
    }

    @Override
    public String toString() {
        return "GraphAddNewNodeByReferenceCommand [provider=" + provider + ", effectGraphAccessor=" + effectGraphAccessor + ", graphElement=" + graphElement + ", graphAddedNode=" + graphAddedNode
                + "]";
    }

}

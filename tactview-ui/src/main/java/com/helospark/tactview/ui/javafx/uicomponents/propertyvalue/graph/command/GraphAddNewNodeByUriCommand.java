package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class GraphAddNewNodeByUriCommand implements UiCommand {
    private GraphProvider provider;
    private EffectGraphAccessor effectGraphAccessor;

    private String nodeUri;

    private GraphIndex graphAddedNode;

    public GraphAddNewNodeByUriCommand(GraphProvider provider, EffectGraphAccessor effectGraphAccessor, String nodeUri) {
        this.provider = provider;
        this.effectGraphAccessor = effectGraphAccessor;
        this.nodeUri = nodeUri;
    }

    @Override
    public void execute() {
        if (nodeUri.startsWith("clip:")) {
            graphAddedNode = effectGraphAccessor.addProceduralClip(provider, nodeUri.replaceFirst("clip:", ""));
        } else if (nodeUri.startsWith("file://")) {
            graphAddedNode = effectGraphAccessor.addClipFile(provider, nodeUri.replaceFirst("file://", ""));
        } else if (nodeUri.startsWith("effect:")) {
            graphAddedNode = effectGraphAccessor.addEffect(provider, nodeUri.replaceFirst("effect:", ""));
        }
    }

    public GraphIndex getGraphAddedNode() {
        return graphAddedNode;
    }

    @Override
    public void revert() {
        if (graphAddedNode != null) {
            effectGraphAccessor.removeElementById(provider, graphAddedNode);
        }
    }

    @Override
    public String toString() {
        return "GraphAddNewNodeByUriCommand [provider=" + provider + ", effectGraphAccessor=" + effectGraphAccessor + ", nodeUri=" + nodeUri + ", graphAddedNode=" + graphAddedNode + "]";
    }

}

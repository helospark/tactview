package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class GraphAddConnectionCommand implements UiCommand {
    private GraphProvider graphProvider;
    private EffectGraphAccessor effectGraphAccessor;

    private ConnectionIndex startIndex, endIndex;

    private Map<ConnectionIndex, List<ConnectionIndex>> removedConnections = new HashMap<>();

    public GraphAddConnectionCommand(GraphProvider graphProvider, EffectGraphAccessor effectGraphAccessor, ConnectionIndex startIndex, ConnectionIndex endIndex) {
        this.graphProvider = graphProvider;
        this.effectGraphAccessor = effectGraphAccessor;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void execute() {
        Map<ConnectionIndex, List<ConnectionIndex>> originalConnections = graphProvider.getEffectGraph().getConnections();
        for (var entry : originalConnections.entrySet()) {
            boolean removedElement = entry.getValue().remove(endIndex);
            if (removedElement) {
                List<ConnectionIndex> removedList = removedConnections.get(entry.getKey());
                if (removedList == null) {
                    removedList = new ArrayList<>();
                }
                removedList.add(endIndex);
                removedConnections.put(entry.getKey(), removedList);
            }
        }

        effectGraphAccessor.addConnection(graphProvider, startIndex, endIndex);
    }

    @Override
    public void revert() {
        effectGraphAccessor.removeConnection(graphProvider, startIndex, endIndex);

        for (var entry : removedConnections.entrySet()) {
            for (var element : entry.getValue()) {
                effectGraphAccessor.addConnection(graphProvider, entry.getKey(), element);
            }
        }
    }

    @Override
    public String toString() {
        return "GraphAddConnectionCommand [graphProvider=" + graphProvider + ", effectGraphAccessor=" + effectGraphAccessor + ", startIndex=" + startIndex + ", endIndex=" + endIndex
                + ", removedConnections=" + removedConnections + "]";
    }

}

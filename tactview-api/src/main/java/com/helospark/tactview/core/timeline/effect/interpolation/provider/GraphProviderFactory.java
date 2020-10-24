package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class GraphProviderFactory implements DesSerFactory<GraphProvider> {

    @Override
    public void addDataForDeserialize(GraphProvider instance, Map<String, Object> data) {
        // TODO: finish this
        data.put("connections", instance.getEffectGraph().getConnections());
        data.put("graph", instance.getEffectGraph().getGraphElements());
    }

    @Override
    public GraphProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        return new GraphProvider(new EffectGraph()); // TODO: finish this
    }

}

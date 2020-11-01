package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public class GraphProviderFactory implements DesSerFactory<GraphProvider> {

    @Override
    public void serializeInto(GraphProvider instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("connections", instance.getEffectGraph().getConnections());

        List<GraphElementToSerialize> graphElementsToSerialize = new ArrayList<>();

        for (var entry : instance.getEffectGraph().getGraphElements().entrySet()) {
            GraphElementToSerialize elementToSerialize = new GraphElementToSerialize();
            elementToSerialize.id = entry.getKey().getId();
            elementToSerialize.factoryId = entry.getValue().getFactoryId();
            elementToSerialize.object = entry.getValue().serialize(saveMetadata);

            graphElementsToSerialize.add(elementToSerialize);
        }

        data.put("graph", graphElementsToSerialize);
    }

    @Override
    public GraphProvider deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        ObjectMapper mapper = loadMetadata.getObjectMapperUsed();
        Map<ConnectionIndex, List<ConnectionIndex>> connections = mapper.convertValue(data.get("connections"), new TypeReference<Map<ConnectionIndex, List<ConnectionIndex>>>() {
        });
        List<GraphElementFactory> factories = loadMetadata.getLightDiContext()
                .getListOfBeans(GraphElementFactory.class);

        Map<GraphIndex, GraphElement> graph = new LinkedHashMap<>();

        for (var element : data.get("graph")) {
            String factoryId = element.get("factoryId").asText();
            GraphElement restoredElement = factories
                    .stream()
                    .filter(a -> a.getId().equals(factoryId))
                    .findFirst()
                    .get()
                    .restoreElement(element.get("object"), loadMetadata);
            GraphIndex id = new GraphIndex(element.get("id").asText());

            graph.put(id, restoredElement);
        }
        return new GraphProvider(new EffectGraph(graph, connections));
    }

    static class GraphElementToSerialize {
        String id;
        String factoryId;
        Object object;
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFactoryId() {
            return factoryId;
        }

        public void setFactoryId(String factoryId) {
            this.factoryId = factoryId;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

    }

}

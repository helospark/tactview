package com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;

public interface GraphElementFactory {

    public String getName();

    public String getId();

    public String getCategory();

    public GraphElement createElement(GraphCreatorRequest request);

    public GraphElement restoreElement(JsonNode node, LoadMetadata metadata);

    public boolean isNeedsInputParam();

    public boolean doesSupport(String uri);

    static enum GraphCategory {
        INPUT("input"),
        OUTPUT("output"),
        EFFECT("effect"),
        PROCEDURAL_CLIP("procedural clip");

        String categoryString;

        private GraphCategory(String category) {
            this.categoryString = category;
        }
    }

    public static class GraphCreatorRequest {
        GraphProvider provider;
        String uri;

        public GraphCreatorRequest(GraphProvider provider, String uri) {
            this.provider = provider;
            this.uri = uri;
        }

    }
}

package com.helospark.tactview.core.timeline;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;

@Component
public class EffectFactoryChain {
    private List<EffectFactory> effectFactoryChain;

    public EffectFactoryChain(List<EffectFactory> effectFactoryChain) {
        this.effectFactoryChain = effectFactoryChain;
    }

    public StatelessEffect createEffect(CreateEffectRequest request) {
        EffectFactory factory = effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + request));
        StatelessEffect result = factory.createEffect(request);
        result.setFactoryId(factory.getId());
        return result;
    }

    public StatelessEffect restoreEffect(JsonNode node, LoadMetadata loadMetadata) {
        String factoryId = node.get("factoryId").asText();
        EffectFactory factory = effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.getId().equals(factoryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + factoryId));
        return factory.restoreEffect(node, loadMetadata);

    }

}

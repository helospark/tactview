package com.helospark.tactview.core.timeline;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.effect.CreateEffectRequest;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.core.timeline.longprocess.LongProcessAware;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;

@Component
public class EffectFactoryChain {
    private List<EffectFactory> effectFactoryChain;
    private LongProcessRequestor longProcessRequestor;

    public EffectFactoryChain(List<EffectFactory> effectFactoryChain, LongProcessRequestor longProcessRequestor) {
        this.effectFactoryChain = effectFactoryChain;
        this.longProcessRequestor = longProcessRequestor;
    }

    public StatelessEffect createEffect(CreateEffectRequest request) {
        EffectFactory factory = effectFactoryChain.stream()
                .filter(effectFactory -> effectFactory.doesSupport(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No factory for " + request));
        StatelessEffect result = factory.createEffect(request);
        result.setFactoryId(factory.getId());

        if (result instanceof LongProcessAware) {
            ((LongProcessAware) result).setLongProcessRequestor(longProcessRequestor);
        }

        if (factory.isFullWidth()) {
            result.setInterval(new TimelineInterval(TimelinePosition.ofZero(), request.getClipInterval().getLength()));
        }

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

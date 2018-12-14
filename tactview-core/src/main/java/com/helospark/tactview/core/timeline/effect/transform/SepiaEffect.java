package com.helospark.tactview.core.timeline.effect.transform;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.transform.service.GenericMatrixTransformationService;
import com.helospark.tactview.core.timeline.effect.transform.service.TransformationServiceRequest;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

public class SepiaEffect extends StatelessVideoEffect {
    private GenericMatrixTransformationService genericMatrixTransformationService;

    public SepiaEffect(TimelineInterval interval, GenericMatrixTransformationService genericMatrixTransformationService) {
        super(interval);
        this.genericMatrixTransformationService = genericMatrixTransformationService;
    }

    public SepiaEffect(JsonNode node, LoadMetadata loadMetadata, GenericMatrixTransformationService genericMatrixTransformationService) {
        super(node, loadMetadata);
        this.genericMatrixTransformationService = genericMatrixTransformationService;
    }

    public SepiaEffect(SepiaEffect sepiaEffect) {
        super(sepiaEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(sepiaEffect, this);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        float[][] sepiaMatrix = {
                {0.272f, 0.534f, 0.131f},
                {0.349f, 0.686f, 0.168f},
                {0.393f, 0.769f, 0.189f},
        };

        TransformationServiceRequest transformRequest = TransformationServiceRequest.builder()
                .withImage(request.getCurrentFrame())
                .withConvolutionMatrix(sepiaMatrix)
                .withFlipRedAndBlue(true)
                .build();

        return genericMatrixTransformationService.transform(transformRequest);
    }

    @Override
    public void initializeValueProvider() {

    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {
        return Collections.emptyList();
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new SepiaEffect(this);
    }

}

package com.helospark.tactview.core.timeline.effect.lut;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.api.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.lut.AbstractLut;

public class LutEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private LutProviderService lutProviderService;

    private FileProvider lutFileProvider;

    public LutEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, LutProviderService lutProviderService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.lutProviderService = lutProviderService;
    }

    public LutEffect(LutEffect lutEffect) {
        super(lutEffect);
        ReflectionUtil.copyOrCloneFieldFromTo(lutEffect, this);
    }

    public LutEffect(JsonNode node, LoadMetadata loadMetadata, IndependentPixelOperation independentPixelOperation, LutProviderService lutProviderService) {
        super(node, loadMetadata);
        this.independentPixelOperation = independentPixelOperation;
        this.lutProviderService = lutProviderService;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        File lutFile = lutFileProvider.getValueAt(request.getEffectPosition());

        if (lutFile.exists()) {
            AbstractLut lut = lutProviderService.provideLutFromFile(lutFile.getAbsolutePath());

            return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
                Color color = Color.of(pixelRequest.input[0] / 255.0, pixelRequest.input[1] / 255.0, pixelRequest.input[2] / 255.0);
                Color result = lut.apply(color);
                pixelRequest.output[0] = (int) (result.red * 255.0);
                pixelRequest.output[1] = (int) (result.green * 255.0);
                pixelRequest.output[2] = (int) (result.blue * 255.0);
                pixelRequest.output[3] = pixelRequest.input[3];
            });
        } else {
            ClipImage result = ClipImage.sameSizeAs(request.getCurrentFrame());
            result.copyFrom(request.getCurrentFrame());
            return result;
        }

    }

    @Override
    public void initializeValueProvider() {
        lutFileProvider = new FileProvider("cube", new StringInterpolator(""));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor lutFileProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lutFileProvider)
                .withName("file")
                .build();

        return List.of(lutFileProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect() {
        return new LutEffect(this);
    }

}

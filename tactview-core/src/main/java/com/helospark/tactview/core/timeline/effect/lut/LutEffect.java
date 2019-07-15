package com.helospark.tactview.core.timeline.effect.lut;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.FileProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperation;
import com.helospark.tactview.core.util.ReflectionUtil;
import com.helospark.tactview.core.util.lut.AbstractLut;

public class LutEffect extends StatelessVideoEffect {
    private IndependentPixelOperation independentPixelOperation;
    private LutProviderService lutProviderService;
    private DoubleProvider intensityProvider;

    private FileProvider lutFileProvider;

    public LutEffect(TimelineInterval interval, IndependentPixelOperation independentPixelOperation, LutProviderService lutProviderService) {
        super(interval);
        this.independentPixelOperation = independentPixelOperation;
        this.lutProviderService = lutProviderService;
    }

    public LutEffect(LutEffect lutEffect, CloneRequestMetadata cloneRequestMetadata) {
        super(lutEffect, cloneRequestMetadata);
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

        double intensity = intensityProvider.getValueAt(request.getEffectPosition());

        if (lutFile.exists()) {
            AbstractLut lut = lutProviderService.provideLutFromFile(lutFile.getAbsolutePath());

            return independentPixelOperation.createNewImageWithAppliedTransformation(request.getCurrentFrame(), pixelRequest -> {
                Color color = Color.of(pixelRequest.input[0] / 255.0, pixelRequest.input[1] / 255.0, pixelRequest.input[2] / 255.0);
                Color result = lut.apply(color);
                pixelRequest.output[0] = (int) ((result.red * intensity * 255.0 + pixelRequest.input[0] * (1.0 - intensity)));
                pixelRequest.output[1] = (int) ((result.green * intensity * 255.0 + pixelRequest.input[1] * (1.0 - intensity)));
                pixelRequest.output[2] = (int) ((result.blue * intensity * 255.0 + pixelRequest.input[2] * (1.0 - intensity)));
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
        lutFileProvider = new FileProvider("cube", new StepStringInterpolator(""));
        intensityProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));
    }

    @Override
    public List<ValueProviderDescriptor> getValueProviders() {

        ValueProviderDescriptor lutFileProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lutFileProvider)
                .withName("file")
                .build();
        ValueProviderDescriptor intensityProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(intensityProvider)
                .withName("intensity")
                .build();

        return List.of(lutFileProviderDescriptor, intensityProviderDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new LutEffect(this, cloneRequestMetadata);
    }

}

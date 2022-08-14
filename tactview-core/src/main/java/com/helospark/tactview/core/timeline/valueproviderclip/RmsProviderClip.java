package com.helospark.tactview.core.timeline.valueproviderclip;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.proceduralclip.valueprovider.AbstractValueProviderClip;
import com.helospark.tactview.core.util.AudioRmsCalculator;
import com.helospark.tactview.core.util.ReflectionUtil;

public class RmsProviderClip extends AbstractValueProviderClip {
    private DependentClipProvider dependentClipProvider;
    private AudioRmsCalculator audioRmsCalculator;

    public RmsProviderClip(VisualMediaMetadata visualMediaMetadata, TimelineInterval interval, AudioRmsCalculator audioRmsCalculator) {
        super(visualMediaMetadata, interval);
        this.audioRmsCalculator = audioRmsCalculator;
    }

    public RmsProviderClip(RmsProviderClip clipToCopy, CloneRequestMetadata cloneRequestMetadata) {
        super(clipToCopy, cloneRequestMetadata);
        ReflectionUtil.copyOrCloneFieldFromTo(clipToCopy, this, cloneRequestMetadata);
        this.audioRmsCalculator = clipToCopy.audioRmsCalculator;
    }

    public RmsProviderClip(ImageMetadata metadata, JsonNode node, LoadMetadata loadMetadata, AudioRmsCalculator audioRmsCalculator) {
        super(metadata, node, loadMetadata);
        this.audioRmsCalculator = audioRmsCalculator;
    }

    @Override
    protected void provideValues(GetFrameRequest request, TimelinePosition relativePosition) {
        Optional<AudioFrameResult> audioValue = dependentClipProvider.getAudioValueAt(relativePosition, request.getRequestedAudioClips());
        double rms = 0.0;
        if (audioValue.isPresent()) {
            rms = audioRmsCalculator.calculateRms(audioValue.get(), 0);
        }
        request.addDynamic(getId(), "rms", rms);
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new RmsProviderClip(this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();

        dependentClipProvider = new DependentClipProvider(new StepStringInterpolator());
    }

    @Override
    public List<ValueProviderDescriptor> getDescriptorsInternal() {
        List<ValueProviderDescriptor> result = new ArrayList<>();

        ValueProviderDescriptor pointProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(dependentClipProvider)
                .withName("Audio clip")
                .build();

        result.add(pointProviderDescriptor);

        return result;
    }

    @Override
    protected Set<String> getClipDependency(TimelinePosition position) {
        Set<String> result = super.getClipDependency(position);
        result.add(dependentClipProvider.getValueWithoutScriptAt(position));
        return result;
    }

}

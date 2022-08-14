package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Map;
import java.util.Optional;

import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.AudioFrameResult;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.DesSerFactory;

public class DependentClipProvider extends KeyframeableEffect<String> {
    StepStringInterpolator stringInterpolator;

    public DependentClipProvider(StepStringInterpolator stringInterpolator) {
        this.stringInterpolator = stringInterpolator;
    }

    public Optional<ReadOnlyClipImage> getValueAt(TimelinePosition position, Map<String, ReadOnlyClipImage> clips) {
        return Optional.ofNullable(clips.get(stringInterpolator.valueAt(position)));
    }

    public Optional<AudioFrameResult> getAudioValueAt(TimelinePosition position, Map<String, AudioFrameResult> clips) {
        return Optional.ofNullable(clips.get(stringInterpolator.valueAt(position)));
    }

    @Override
    public String getValueWithoutScriptAt(TimelinePosition position) {
        return stringInterpolator.valueAt(position);
    }

    @Override
    public String getValueAt(TimelinePosition position, EvaluationContext evaluationContext) {
        if (expression != null && evaluationContext != null) {
            String expressionResult = evaluationContext.evaluateExpression(expression, position, String.class);
            if (expressionResult == null) {
                return getValueWithoutScriptAt(position);
            } else {
                return expressionResult;
            }
        } else {
            return getValueWithoutScriptAt(position);
        }
    }

    @Override
    public EffectInterpolator getInterpolator() {
        return stringInterpolator;
    }

    @Override
    public void keyframeAdded(TimelinePosition globalTimelinePosition, String value) {
        stringInterpolator.valueAdded(globalTimelinePosition, value);
    }

    @Override
    public void removeKeyframeAt(TimelinePosition globalTimelinePosition) {
        stringInterpolator.removeKeyframeAt(globalTimelinePosition);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Map<TimelinePosition, Object> getValues() {
        return stringInterpolator.getValues();
    }

    @Override
    public KeyframeableEffect<String> deepCloneInternal(CloneRequestMetadata cloneRequestMetadata) {
        return new DependentClipProvider(stringInterpolator.deepClone(cloneRequestMetadata));
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect<String>>> generateSerializableContent() {
        return DependentClipProviderFactory.class;
    }

    @Override
    public void setUseKeyframes(boolean useKeyframes) {
        stringInterpolator.setUseKeyframes(useKeyframes);
    }

    @Override
    public Class<?> getProvidedType() {
        return String.class;
    }

}

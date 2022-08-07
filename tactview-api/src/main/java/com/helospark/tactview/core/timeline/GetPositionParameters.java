package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;

public class GetPositionParameters {
    private TimelinePosition timelinePosition;
    private double scale;
    private int width;
    private int height;
    private EvaluationContext evaluationContext;

    public GetPositionParameters(TimelinePosition timelinePosition, double scale, int width, int height, EvaluationContext evaluationContext) {
        this.timelinePosition = timelinePosition;
        this.scale = scale;
        this.width = width;
        this.height = height;
        this.evaluationContext = evaluationContext;
    }

    public TimelinePosition getTimelinePosition() {
        return timelinePosition;
    }

    public double getScale() {
        return scale;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

}
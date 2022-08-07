package com.helospark.tactview.core.timeline;

import java.util.Map;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;

public class TimelineRenderResult {
    private AudioVideoFragment audioVideoFragment;
    private Map<String, RegularRectangle> clipRectangle;
    private EvaluationContext evaluationContext;

    public TimelineRenderResult(AudioVideoFragment audioVideoFragment, Map<String, RegularRectangle> clipRectangle, EvaluationContext evaluationContext) {
        this.audioVideoFragment = audioVideoFragment;
        this.clipRectangle = clipRectangle;
        this.evaluationContext = evaluationContext;
    }

    public AudioVideoFragment getAudioVideoFragment() {
        return audioVideoFragment;
    }

    public Map<String, RegularRectangle> getClipRectangle() {
        return clipRectangle;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    @Override
    public String toString() {
        return "TimelineRenderResult [audioVideoFragment=" + audioVideoFragment + ", clipRectangle=" + clipRectangle + "]";
    }

    public static class RegularRectangle {
        double x, y;
        double width, height;

        public RegularRectangle(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return "RegularRectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
        }

    }
}

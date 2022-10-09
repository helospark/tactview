package com.helospark.tactview.core.timeline.render;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.GetPositionParameters;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.TimelineRenderResult.RegularRectangle;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.memoryoperations.MemoryOperations;

@Component
public class FrameExtender {
    private MemoryOperations memoryOperations;

    public FrameExtender(MemoryOperations memoryOperations) {
        this.memoryOperations = memoryOperations;
    }

    public ClipImage expandFrame(FrameExtendRequest request) {
        ReadOnlyClipImage frameResult = request.getFrameResult();
        TimelinePosition timelinePosition = request.getTimelinePosition();
        VisualTimelineClip clip = request.getClip();
        EvaluationContext evaluationContext = request.getEvaluationContext();

        int previewHeight = request.getPreviewHeight();
        int previewWidth = request.getPreviewWidth();
        int anchorOffsetX = clip.getHorizontalAlignment(timelinePosition, evaluationContext).apply(frameResult.getWidth(), previewWidth);
        int anchorOffsetY = clip.getVerticalAlignment(timelinePosition, evaluationContext).apply(frameResult.getHeight(), previewHeight);

        double scale = request.getScale();

        GetPositionParameters getPositionParameters = new GetPositionParameters(timelinePosition, scale, previewWidth, previewHeight, evaluationContext);
        int requestedXPosition = anchorOffsetX + clip.getXPosition(getPositionParameters);
        int requestedYPosition = anchorOffsetY + clip.getYPosition(getPositionParameters);

        request.outBoundPositions.put(clip.getId(), new RegularRectangle(requestedXPosition, requestedYPosition, frameResult.getWidth(), frameResult.getHeight()));

        if (evaluationContext != null) {
            double x = requestedXPosition / (double) previewWidth;
            double y = requestedYPosition / (double) previewHeight;
            double width = frameResult.getWidth() / (double) previewWidth;
            double height = frameResult.getHeight() / (double) previewHeight;

            evaluationContext.addDynamicVariable(clip.getId(), "x", x);
            evaluationContext.addDynamicVariable(clip.getId(), "y", y);
            evaluationContext.addDynamicVariable(clip.getId(), "width", width);
            evaluationContext.addDynamicVariable(clip.getId(), "height", height);

            evaluationContext.addDynamicVariable(clip.getId(), "x_pixel", requestedXPosition);
            evaluationContext.addDynamicVariable(clip.getId(), "y_pixel", requestedYPosition);
            evaluationContext.addDynamicVariable(clip.getId(), "width_pixel", frameResult.getWidth());
            evaluationContext.addDynamicVariable(clip.getId(), "height_pixel", frameResult.getHeight());

            evaluationContext.addDynamicVariable(clip.getId(), "rect", new InterpolationLine(new Point(x, y), new Point(x + width, y + height)));
        }

        return expandAndTranslate(frameResult, previewWidth, previewHeight, requestedXPosition, requestedYPosition);
    }

    public ClipImage expandAndTranslate(ReadOnlyClipImage frameResult, int previewWidth, int previewHeight, int requestedXPosition, int requestedYPosition) {
        ByteBuffer outputBuffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(previewHeight * previewWidth * 4);
        ByteBuffer inputBuffer = frameResult.getBuffer();

        if (previewWidth == frameResult.getWidth() && previewHeight == frameResult.getHeight() &&
                requestedXPosition == 0 && requestedYPosition == 0) {
            memoryOperations.copyBuffer(inputBuffer, outputBuffer, inputBuffer.capacity());
        } else {
            int destinationStartX = Math.max(requestedXPosition, 0);
            int destinationStartY = Math.max(requestedYPosition, 0);

            int destinationEndX = Math.min(requestedXPosition + frameResult.getWidth(), previewWidth);
            int destinationEndY = Math.min(requestedYPosition + frameResult.getHeight(), previewHeight);

            int sourceX = Math.max(0, -requestedXPosition);
            int sourceY = Math.max(0, -requestedYPosition);

            int width = Math.max(0, destinationEndX - destinationStartX);
            int height = Math.max(0, destinationEndY - destinationStartY);

            int numberOfBytesInARow = width * 4;
            byte[] tmpBuffer = new byte[numberOfBytesInARow];

            int toY = sourceY + height;
            for (int i = sourceY; i < toY; ++i) {
                inputBuffer.position(i * frameResult.getWidth() * 4 + sourceX * 4);
                inputBuffer.get(tmpBuffer, 0, numberOfBytesInARow);

                outputBuffer.position((destinationStartY + (i - sourceY)) * previewWidth * 4 + destinationStartX * 4);
                outputBuffer.put(tmpBuffer, 0, numberOfBytesInARow);
            }
        }
        return new ClipImage(outputBuffer, previewWidth, previewHeight);
    }

    public static class FrameExtendRequest {
        ReadOnlyClipImage frameResult;
        VisualTimelineClip clip;
        int previewHeight;
        int previewWidth;
        double scale;
        TimelinePosition timelinePosition;
        Map<String, RegularRectangle> outBoundPositions;
        EvaluationContext evaluationContext;

        @Generated("SparkTools")
        private FrameExtendRequest(Builder builder) {
            this.frameResult = builder.frameResult;
            this.clip = builder.clip;
            this.previewHeight = builder.previewHeight;
            this.previewWidth = builder.previewWidth;
            this.scale = builder.scale;
            this.timelinePosition = builder.timelinePosition;
            this.outBoundPositions = builder.outBoundPositions;
            this.evaluationContext = builder.evaluationContext;
        }

        public ReadOnlyClipImage getFrameResult() {
            return frameResult;
        }

        public VisualTimelineClip getClip() {
            return clip;
        }

        public int getPreviewHeight() {
            return previewHeight;
        }

        public int getPreviewWidth() {
            return previewWidth;
        }

        public double getScale() {
            return scale;
        }

        public TimelinePosition getTimelinePosition() {
            return timelinePosition;
        }

        public EvaluationContext getEvaluationContext() {
            return evaluationContext;
        }

        @Generated("SparkTools")
        public static Builder builder() {
            return new Builder();
        }

        @Generated("SparkTools")
        public static final class Builder {
            private ReadOnlyClipImage frameResult;
            private VisualTimelineClip clip;
            private int previewHeight;
            private int previewWidth;
            private double scale;
            private TimelinePosition timelinePosition;
            Map<String, RegularRectangle> outBoundPositions = new HashMap<>();
            EvaluationContext evaluationContext;

            private Builder() {
            }

            public Builder withFrameResult(ReadOnlyClipImage frameResult) {
                this.frameResult = frameResult;
                return this;
            }

            public Builder withClip(VisualTimelineClip clip) {
                this.clip = clip;
                return this;
            }

            public Builder withPreviewHeight(int previewHeight) {
                this.previewHeight = previewHeight;
                return this;
            }

            public Builder withPreviewWidth(int previewWidth) {
                this.previewWidth = previewWidth;
                return this;
            }

            public Builder withScale(double scale) {
                this.scale = scale;
                return this;
            }

            public Builder withTimelinePosition(TimelinePosition timelinePosition) {
                this.timelinePosition = timelinePosition;
                return this;
            }

            public Builder withOutBoundPositions(Map<String, RegularRectangle> outBoundPositions) {
                this.outBoundPositions = outBoundPositions;
                return this;
            }

            public Builder withEvaluationContext(EvaluationContext evaluationContext) {
                this.evaluationContext = evaluationContext;
                return this;
            }

            public FrameExtendRequest build() {
                return new FrameExtendRequest(this);
            }
        }

    }

}

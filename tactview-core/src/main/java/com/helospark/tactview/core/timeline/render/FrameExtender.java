package com.helospark.tactview.core.timeline.render;

import java.nio.ByteBuffer;

import javax.annotation.Generated;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class FrameExtender {

    public ClipImage expandFrame(FrameExtendRequest request) {
        ReadOnlyClipImage frameResult = request.getFrameResult();
        TimelinePosition timelinePosition = request.getTimelinePosition();
        VisualTimelineClip clip = request.getClip();

        int previewHeight = request.getPreviewHeight();
        int previewWidth = request.getPreviewWidth();
        int anchorOffsetX = clip.getHorizontalAlignment(timelinePosition).apply(frameResult.getWidth(), previewWidth);
        int anchorOffsetY = clip.getVerticalAlignment(timelinePosition).apply(frameResult.getHeight(), previewHeight);

        double scale = request.getScale();

        int requestedXPosition = anchorOffsetX + clip.getXPosition(timelinePosition, scale);
        int requestedYPosition = anchorOffsetY + clip.getYPosition(timelinePosition, scale);

        return expandAndTranslate(frameResult, previewWidth, previewHeight, requestedXPosition, requestedYPosition);
    }

    public ClipImage expandAndTranslate(ReadOnlyClipImage frameResult, int previewWidth, int previewHeight, int requestedXPosition, int requestedYPosition) {
        ByteBuffer outputBuffer = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(previewHeight * previewWidth * 4);
        ByteBuffer inputBuffer = frameResult.getBuffer();

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
        return new ClipImage(outputBuffer, previewWidth, previewHeight);
    }

    public static class FrameExtendRequest {
        ReadOnlyClipImage frameResult;
        VisualTimelineClip clip;
        int previewHeight;
        int previewWidth;
        double scale;
        TimelinePosition timelinePosition;

        @Generated("SparkTools")
        private FrameExtendRequest(Builder builder) {
            this.frameResult = builder.frameResult;
            this.clip = builder.clip;
            this.previewHeight = builder.previewHeight;
            this.previewWidth = builder.previewWidth;
            this.scale = builder.scale;
            this.timelinePosition = builder.timelinePosition;
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

            public FrameExtendRequest build() {
                return new FrameExtendRequest(this);
            }
        }

    }

}

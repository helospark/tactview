package com.helospark.tactview.core.timeline.effect.rotate;

import javax.annotation.Generated;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class RotateService {
    private OpenCVRotateEffectImplementation implementation;

    public RotateService(OpenCVRotateEffectImplementation implementation) {
        this.implementation = implementation;
    }

    public ClipImage rotate(RotateServiceRequest request) {
        double degrees = request.angle;

        int originalWidth = request.image.getWidth();
        int originalHeight = request.image.getHeight();

        int newWidth = (int) Math.sqrt(originalHeight * originalHeight + originalWidth * originalWidth);
        int newHeight = newWidth;

        int rotationCenterX = (int) (originalWidth * request.centerX);
        int rotationCenterY = (int) (originalHeight * request.centerY);

        OpenCVRotateRequest nativeRequest = new OpenCVRotateRequest();
        nativeRequest.rotationDegrees = degrees;
        nativeRequest.rotationPointX = rotationCenterX;
        nativeRequest.rotationPointY = rotationCenterY;
        nativeRequest.input = request.image.getBuffer();
        nativeRequest.originalWidth = originalWidth;
        nativeRequest.originalHeight = originalHeight;
        nativeRequest.output = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(newWidth * newHeight * 4);
        nativeRequest.newWidth = newWidth;
        nativeRequest.newHeight = newHeight;

        implementation.rotateImage(nativeRequest);

        return new ClipImage(nativeRequest.output, newWidth, newHeight);
    }

    public ClipImage rotateExactSize(RotateServiceRequest request) {
        double degrees = request.angle;

        int originalWidth = request.image.getWidth();
        int originalHeight = request.image.getHeight();

        double angleRad = Math.toRadians(degrees);
        double a = Math.ceil(Math.abs(originalWidth * Math.sin(angleRad)) + Math.abs(originalHeight * Math.cos(angleRad)));
        double b = Math.ceil(Math.abs(originalWidth * Math.cos(angleRad)) + Math.abs(originalHeight * Math.sin(angleRad)));

        int newWidth = (int) b;
        int newHeight = (int) a;

        int rotationCenterX = (int) (originalWidth * request.centerX);
        int rotationCenterY = (int) (originalHeight * request.centerY);

        OpenCVRotateRequest nativeRequest = new OpenCVRotateRequest();
        nativeRequest.rotationDegrees = degrees;
        nativeRequest.rotationPointX = rotationCenterX;
        nativeRequest.rotationPointY = rotationCenterY;
        nativeRequest.input = request.image.getBuffer();
        nativeRequest.originalWidth = originalWidth;
        nativeRequest.originalHeight = originalHeight;
        nativeRequest.output = GlobalMemoryManagerAccessor.memoryManager.requestBuffer(newWidth * newHeight * 4);
        nativeRequest.newWidth = newWidth;
        nativeRequest.newHeight = newHeight;

        implementation.rotateImage(nativeRequest);

        return new ClipImage(nativeRequest.output, newWidth, newHeight);
    }

    public static class RotateServiceRequest {
        double angle;
        ReadOnlyClipImage image;

        double centerX;
        double centerY;

        @Generated("SparkTools")
        private RotateServiceRequest(Builder builder) {
            this.angle = builder.angle;
            this.image = builder.image;
            this.centerX = builder.centerX;
            this.centerY = builder.centerY;
        }

        @Generated("SparkTools")
        public static Builder builder() {
            return new Builder();
        }

        @Generated("SparkTools")
        public static final class Builder {
            private double angle;
            private ReadOnlyClipImage image;
            private double centerX;
            private double centerY;

            private Builder() {
            }

            public Builder withAngle(double angle) {
                this.angle = angle;
                return this;
            }

            public Builder withImage(ReadOnlyClipImage image) {
                this.image = image;
                return this;
            }

            public Builder withCenterX(double centerX) {
                this.centerX = centerX;
                return this;
            }

            public Builder withCenterY(double centery) {
                this.centerY = centery;
                return this;
            }

            public RotateServiceRequest build() {
                return new RotateServiceRequest(this);
            }
        }

    }
}

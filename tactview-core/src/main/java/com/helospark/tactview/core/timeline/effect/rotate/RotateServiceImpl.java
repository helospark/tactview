package com.helospark.tactview.core.timeline.effect.rotate;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.image.ClipImage;

@Component
public class RotateServiceImpl implements RotateService {
    private OpenCVRotateEffectImplementation implementation;

    public RotateServiceImpl(OpenCVRotateEffectImplementation implementation) {
        this.implementation = implementation;
    }

    @Override
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

    @Override
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

}

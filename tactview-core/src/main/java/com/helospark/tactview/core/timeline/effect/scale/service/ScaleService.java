package com.helospark.tactview.core.timeline.effect.scale.service;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.effect.scale.OpenCVScaleEffectImplementation;
import com.helospark.tactview.core.timeline.effect.scale.OpenCVScaleRequest;

@Component
public class ScaleService {
    private OpenCVScaleEffectImplementation implementation;

    public ScaleService(OpenCVScaleEffectImplementation implementation) {
        this.implementation = implementation;
    }

    public ClipFrameResult createScaledImage(ScaleRequest request) {
        ClipFrameResult currentFrame = request.getImage();
        ClipFrameResult result;
        if (request.isPadImage()) {
            result = ClipFrameResult.fromSize(currentFrame.getWidth(), currentFrame.getHeight());
        } else {
            result = ClipFrameResult.fromSize(request.getNewWidth(), request.getNewHeight());
        }

        OpenCVScaleRequest nativeRequest = new OpenCVScaleRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.interpolationType = 0; // todo
        nativeRequest.newHeight = request.getNewHeight();
        nativeRequest.newWidth = request.getNewWidth();
        nativeRequest.originalWidth = currentFrame.getWidth();
        nativeRequest.originalHeight = currentFrame.getHeight();
        nativeRequest.output = result.getBuffer();

        implementation.scaleImage(nativeRequest);

        return result;
    }

}

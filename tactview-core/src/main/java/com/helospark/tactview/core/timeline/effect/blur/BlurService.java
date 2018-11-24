package com.helospark.tactview.core.timeline.effect.blur;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVBasedGaussianBlur;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVGaussianBlurRequest;
import com.helospark.tactview.core.timeline.effect.blur.opencv.OpenCVRegion;

@Component
public class BlurService {
    private OpenCVBasedGaussianBlur openCVBasedBlur;

    public BlurService(OpenCVBasedGaussianBlur openCVBasedBlur) {
        this.openCVBasedBlur = openCVBasedBlur;
    }

    public ClipFrameResult createBlurredImage(BlurRequest request) {
        ClipFrameResult buffer = ClipFrameResult.sameSizeAs(request.getImage());
        ClipFrameResult currentFrame = request.getImage();
        OpenCVGaussianBlurRequest nativeRequest = new OpenCVGaussianBlurRequest();
        nativeRequest.input = currentFrame.getBuffer();
        nativeRequest.output = buffer.getBuffer();
        nativeRequest.width = currentFrame.getWidth();
        nativeRequest.height = currentFrame.getHeight();
        nativeRequest.kernelWidth = (request.getKernelWidth()) * 2 + 1;
        nativeRequest.kernelHeight = (request.getKernelHeight()) * 2 + 1;
        nativeRequest.blurRegion = createBlurRegion(request.getRegion(), request.getImage());
        openCVBasedBlur.applyGaussianBlur(nativeRequest);

        return buffer;
    }

    private OpenCVRegion createBlurRegion(Optional<Region> optionalRegion, ClipFrameResult input) {
        OpenCVRegion result = new OpenCVRegion();
        if (optionalRegion.isPresent()) {
            Region region = optionalRegion.get();
            result.x = region.x;
            result.y = region.y;
            result.width = region.width;
            result.height = region.height;
        } else {
            result.x = 0;
            result.y = 0;
            result.width = input.getWidth();
            result.height = input.getHeight();
        }
        return result;
    }

}

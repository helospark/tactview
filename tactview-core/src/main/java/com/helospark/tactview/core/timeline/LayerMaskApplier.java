package com.helospark.tactview.core.timeline;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.effect.scale.OpenCVScaleEffectImplementation;
import com.helospark.tactview.core.timeline.effect.scale.OpenCVScaleRequest;
import com.helospark.tactview.core.util.IndependentPixelOperation;

@Component
public class LayerMaskApplier {
    private IndependentPixelOperation independentPixelOperation;
    private OpenCVScaleEffectImplementation scaleImplementation;

    public LayerMaskApplier(IndependentPixelOperation independentPixelOperation, OpenCVScaleEffectImplementation scaleImplementation) {
        this.independentPixelOperation = independentPixelOperation;
        this.scaleImplementation = scaleImplementation;
    }

    public ClipFrameResult applyLayerMask(ClipFrameResult input, ClipFrameResult mask) {
        ClipFrameResult scaledMask = null;
        if (mask.getWidth() != input.getWidth() || mask.getHeight() != input.getHeight()) {
            scaledMask = ClipFrameResult.sameSizeAs(input);

            OpenCVScaleRequest request = new OpenCVScaleRequest();
            request.input = mask.getBuffer();
            request.output = scaledMask.getBuffer();
            request.originalWidth = mask.getWidth();
            request.originalHeight = mask.getHeight();
            request.newWidth = input.getWidth();
            request.newHeight = input.getHeight();

            scaleImplementation.scaleImage(request);
        }

        ClipFrameResult maskToUse = (scaledMask == null ? mask : scaledMask);

        ClipFrameResult result = ClipFrameResult.sameSizeAs(input);

        independentPixelOperation.executePixelTransformation(input.getWidth(), input.getHeight(), (x, y) -> {
            int intensity = (maskToUse.getRed(x, y) + maskToUse.getGreen(x, y) + maskToUse.getBlue(x, y)) / 3;
            for (int i = 0; i < 3; ++i) {
                int color = input.getColorComponentWithOffset(x, y, i);
                result.setColorComponentByOffset(color, x, y, i);
            }
            result.setAlpha(intensity, x, y);
        });
        if (scaledMask != null) {
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledMask.getBuffer());
        }
        return result;
    }

}

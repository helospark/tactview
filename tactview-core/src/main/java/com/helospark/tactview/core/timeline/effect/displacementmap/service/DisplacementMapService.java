package com.helospark.tactview.core.timeline.effect.displacementmap.service;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.IndependentPixelOperationImpl;

@Service
public class DisplacementMapService {
    private IndependentPixelOperationImpl independentPixelOperation;
    private ScaleService scaleService;

    public DisplacementMapService(IndependentPixelOperationImpl independentPixelOperation, ScaleService scaleService) {
        this.independentPixelOperation = independentPixelOperation;
        this.scaleService = scaleService;
    }

    public ClipImage applyDisplacementMap(ApplyDisplacementMapRequest parameterObject) {
        if (!parameterObject.getCurrentFrame().isSameSizeAs(parameterObject.getDisplacementMap())) {
            ScaleRequest scaleRequest = ScaleRequest.builder()
                    .withImage(parameterObject.getDisplacementMap())
                    .withNewWidth(parameterObject.getCurrentFrame().getWidth())
                    .withNewHeight(parameterObject.getCurrentFrame().getHeight())
                    .build();
            ReadOnlyClipImage scaledImage = scaleService.createScaledImage(scaleRequest);
            ClipImage result = applyDisplacementMapOnSameDisplacementMapSize(parameterObject.getCurrentFrame(), scaledImage, parameterObject.getVerticalMultiplier(),
                    parameterObject.getHorizontalMultiplier());
            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(scaledImage.getBuffer());
            return result;
        } else {
            return applyDisplacementMapOnSameDisplacementMapSize(parameterObject.getCurrentFrame(), parameterObject.getDisplacementMap(), parameterObject.getVerticalMultiplier(),
                    parameterObject.getHorizontalMultiplier());
        }

    }

    private ClipImage applyDisplacementMapOnSameDisplacementMapSize(ReadOnlyClipImage currentFrame, ReadOnlyClipImage displacementMap, double verticalMultiplier,
            double horizontalMultiplier) {
        ClipImage result = ClipImage.sameSizeAs(currentFrame);

        independentPixelOperation.executePixelTransformation(result.getWidth(), result.getHeight(), (x, y) -> {
            int displacedX = clip(x + (((displacementMap.getRed(x, y) - 128) / 128.0) * horizontalMultiplier), 0, result.getWidth() - 1);
            int displacedY = clip(y + (((displacementMap.getGreen(x, y) - 128) / 128.0) * verticalMultiplier), 0, result.getHeight() - 1);

            for (int i = 0; i < 4; ++i) {
                int component = currentFrame.getColorComponentWithOffset(displacedX, displacedY, i);
                result.setColorComponentByOffset(component, x, y, i);
            }
        });
        return result;
    }

    private int clip(double value, int min, int max) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return (int) value;
    }

}

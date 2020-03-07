package com.helospark.tactview.core.timeline.framemerge;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.blendmode.impl.NormalBlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskApplier;
import com.helospark.tactview.core.timeline.effect.layermask.LayerMaskApplyRequest;
import com.helospark.tactview.core.timeline.effect.layermask.impl.calculator.LayerMaskGrayscaleToAlpha;
import com.helospark.tactview.core.timeline.image.ClipImage;

@Component
public class FrameMergerWithMaskImpl implements FrameMergerWithMask {
    private LayerMaskApplier layerMaskApplier;
    private AlphaBlitService alphaBlitService;
    private LayerMaskGrayscaleToAlpha calculator;
    private NormalBlendModeStrategy normalBlendModeStrategy;

    public FrameMergerWithMaskImpl(LayerMaskApplier layerMaskApplier, AlphaBlitService alphaBlitService, LayerMaskGrayscaleToAlpha calculator, NormalBlendModeStrategy normalBlendModeStrategy) {
        this.layerMaskApplier = layerMaskApplier;
        this.alphaBlitService = alphaBlitService;
        this.calculator = calculator;
        this.normalBlendModeStrategy = normalBlendModeStrategy;
    }

    @Override
    public ClipImage mergeFramesWithMask(FrameMergerWithMaskRequest request) {
        LayerMaskApplyRequest layerMaskApplierRequest = LayerMaskApplyRequest.builder()
                .withCalculator(calculator)
                .withCurrentFrame(request.top)
                .withInvert(request.invert)
                .withMask(request.mask)
                .withScaleLayerMask(request.scale)
                .build();
        ClipImage maskedImage = layerMaskApplier.createNewImageWithLayerMask(layerMaskApplierRequest);
        ClipImage result = ClipImage.copyOf(request.bottom);

        int width = Math.min(maskedImage.getWidth(), request.bottom.getWidth());
        int height = Math.min(maskedImage.getHeight(), request.bottom.getHeight());

        alphaBlitService.alphaBlitFrame(result, maskedImage, width, height, normalBlendModeStrategy, 1.0);

        maskedImage.returnBuffer();

        return result;
    }

}

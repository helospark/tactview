package com.helospark.tactview.core.timeline.effect.layermask;

import com.helospark.tactview.core.timeline.image.ClipImage;

public interface LayerMaskApplier {

    ClipImage createNewImageWithLayerMask(LayerMaskApplyRequest layerMaskRequest);

    ClipImage mergeTwoImageWithLayerMask(LayerMaskBetweenTwoImageApplyRequest request);

}
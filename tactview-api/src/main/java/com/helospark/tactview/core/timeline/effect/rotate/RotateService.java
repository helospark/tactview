package com.helospark.tactview.core.timeline.effect.rotate;

import com.helospark.tactview.core.timeline.image.ClipImage;

public interface RotateService {

    ClipImage rotate(RotateServiceRequest request);

    ClipImage rotateExactSize(RotateServiceRequest request);

}
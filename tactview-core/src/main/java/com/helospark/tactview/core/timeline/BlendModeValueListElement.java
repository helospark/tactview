package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.blendmode.BlendModeStrategy;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class BlendModeValueListElement extends ValueListElement {
    private BlendModeStrategy blendMode;

    public BlendModeValueListElement(String id, String text, BlendModeStrategy blendMode) {
        super(id, text);
        this.blendMode = blendMode;
    }

    public BlendModeStrategy getBlendMode() {
        return blendMode;
    }

}

package com.helospark.tactview.core.timeline;

import com.helospark.tactview.core.timeline.blendmode.BlendMode;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class BlendModeValueListElement extends ValueListElement {
    private BlendMode blendMode;

    public BlendModeValueListElement(String id, String text, BlendMode blendMode) {
        super(id, text);
        this.blendMode = blendMode;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

}

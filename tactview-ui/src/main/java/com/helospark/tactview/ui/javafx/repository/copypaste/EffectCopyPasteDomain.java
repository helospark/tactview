package com.helospark.tactview.ui.javafx.repository.copypaste;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;

public class EffectCopyPasteDomain {
    public TimelineClip clipboardContent;
    public StatelessEffect effect;

    public EffectCopyPasteDomain(TimelineClip clipboardContent, StatelessEffect effect) {
        this.clipboardContent = clipboardContent;
        this.effect = effect;
    }

}

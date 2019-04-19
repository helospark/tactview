package com.helospark.tactview.ui.javafx.repository.copypaste;

import java.util.List;

import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineClip;

public class EffectCopyPasteDomain {
    private List<EffectCopyPasteDomainElement> elements;

    public EffectCopyPasteDomain(TimelineClip clip, StatelessEffect effect) {
        this.elements = List.of(new EffectCopyPasteDomainElement(clip, effect));
    }

    public EffectCopyPasteDomain(List<EffectCopyPasteDomainElement> elements) {
        this.elements = elements;
    }

    public List<EffectCopyPasteDomainElement> getElements() {
        return elements;
    }

    public static class EffectCopyPasteDomainElement {
        public TimelineClip clip;
        public StatelessEffect effect;

        public EffectCopyPasteDomainElement(TimelineClip clip, StatelessEffect effect) {
            this.clip = clip;
            this.effect = effect;
        }

    }

}

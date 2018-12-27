package com.helospark.tactview.ui.javafx.effect.chain;

import com.helospark.tactview.core.timeline.StatelessEffect;

public class EffectContextMenuChainItemRequest {
    private StatelessEffect effect;

    public EffectContextMenuChainItemRequest(StatelessEffect effect) {
        this.effect = effect;
    }

    public StatelessEffect getEffect() {
        return effect;
    }

    public void setEffect(StatelessEffect effect) {
        this.effect = effect;
    }

}

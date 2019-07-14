package com.helospark.tactview.core.timeline;

public interface EffectAware extends IntervalAware {

    public default void effectChanged(EffectChangedRequest request) {

    }

    public static class EffectChangedRequest {
        public String id;

        public EffectChangedRequest(String id) {
            this.id = id;
        }

    }
}

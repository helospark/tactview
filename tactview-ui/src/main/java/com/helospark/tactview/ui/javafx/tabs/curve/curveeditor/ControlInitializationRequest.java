package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;

import javafx.scene.layout.GridPane;

public class ControlInitializationRequest {
    public GridPane gridToInitialize;
    public EffectInterpolator effectInterpolator;
    public Runnable updateRunnable;

    @Generated("SparkTools")
    private ControlInitializationRequest(Builder builder) {
        this.gridToInitialize = builder.gridToInitialize;
        this.effectInterpolator = builder.effectInterpolator;
        this.updateRunnable = builder.updateRunnable;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private GridPane gridToInitialize;
        private EffectInterpolator effectInterpolator;
        private Runnable updateRunnable;

        private Builder() {
        }

        public Builder withGridToInitialize(GridPane gridToInitialize) {
            this.gridToInitialize = gridToInitialize;
            return this;
        }

        public Builder withEffectInterpolator(EffectInterpolator effectInterpolator) {
            this.effectInterpolator = effectInterpolator;
            return this;
        }

        public Builder withUpdateRunnable(Runnable updateRunnable) {
            this.updateRunnable = updateRunnable;
            return this;
        }

        public ControlInitializationRequest build() {
            return new ControlInitializationRequest(this);
        }
    }
}

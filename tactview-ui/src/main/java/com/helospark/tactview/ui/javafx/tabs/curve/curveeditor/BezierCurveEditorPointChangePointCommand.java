package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class BezierCurveEditorPointChangePointCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;

    private BezierDoubleInterpolator toUpdate;
    private TimelinePosition positionToModify;

    private TimelinePosition newTime;
    private double newPosition;

    private String providerId;

    private TimelinePosition originalPosition;
    private double originalValue;
    private boolean revertable;

    @Generated("SparkTools")
    private BezierCurveEditorPointChangePointCommand(Builder builder) {
        this.effectParametersRepository = builder.effectParametersRepository;
        this.toUpdate = builder.toUpdate;
        this.positionToModify = builder.positionToModify;
        this.newTime = builder.newTime;
        this.newPosition = builder.newPosition;
        this.providerId = builder.providerId;
        this.originalPosition = builder.originalPosition;
        this.originalValue = builder.originalValue;
        this.revertable = builder.revertable;
    }

    @Override
    public void execute() {
        toUpdate.valueModifiedAt(positionToModify, newTime, newPosition);
        effectParametersRepository.sendKeyframeChangeMessage(providerId);
    }

    @Override
    public void revert() {
        toUpdate.valueModifiedAt(newTime, originalPosition, originalValue);
        effectParametersRepository.sendKeyframeChangeMessage(providerId);
    }

    @Override
    public boolean isRevertable() {
        return revertable;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private EffectParametersRepository effectParametersRepository;
        private BezierDoubleInterpolator toUpdate;
        private TimelinePosition positionToModify;
        private TimelinePosition newTime;
        private double newPosition;
        private String providerId;
        private TimelinePosition originalPosition;
        private double originalValue;
        private boolean revertable;

        private Builder() {
        }

        public Builder withEffectParametersRepository(EffectParametersRepository effectParametersRepository) {
            this.effectParametersRepository = effectParametersRepository;
            return this;
        }

        public Builder withToUpdate(BezierDoubleInterpolator toUpdate) {
            this.toUpdate = toUpdate;
            return this;
        }

        public Builder withPositionToModify(TimelinePosition positionToModify) {
            this.positionToModify = positionToModify;
            return this;
        }

        public Builder withNewTime(TimelinePosition newTime) {
            this.newTime = newTime;
            return this;
        }

        public Builder withNewPosition(double newPosition) {
            this.newPosition = newPosition;
            return this;
        }

        public Builder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder withOriginalPosition(TimelinePosition originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withOriginalValue(double originalValue) {
            this.originalValue = originalValue;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public BezierCurveEditorPointChangePointCommand build() {
            return new BezierCurveEditorPointChangePointCommand(this);
        }
    }

}

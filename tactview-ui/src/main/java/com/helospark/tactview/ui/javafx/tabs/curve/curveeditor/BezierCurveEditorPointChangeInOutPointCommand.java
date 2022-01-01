package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

public class BezierCurveEditorPointChangeInOutPointCommand implements UiCommand {
    private EffectParametersRepository effectParametersRepository;

    private BezierDoubleInterpolator toUpdate;
    private TimelinePosition positionToModify;
    private Point newPoint;
    private BezierPointType type;
    private String providerId;

    private Point originalPosition;
    private boolean revertable;

    @Generated("SparkTools")
    private BezierCurveEditorPointChangeInOutPointCommand(Builder builder) {
        this.effectParametersRepository = builder.effectParametersRepository;
        this.toUpdate = builder.toUpdate;
        this.positionToModify = builder.positionToModify;
        this.newPoint = builder.newPoint;
        this.type = builder.type;
        this.providerId = builder.providerId;
        this.originalPosition = builder.originalPosition;
        this.revertable = builder.revertable;
    }

    @Override
    public void execute() {
        if (type == BezierPointType.IN) {
            toUpdate.updatedInControlPointAt(positionToModify, newPoint);
        } else {
            toUpdate.updatedOutControlPointAt(positionToModify, newPoint);
        }
        effectParametersRepository.sendKeyframeChangeMessage(providerId);
    }

    @Override
    public void revert() {
        if (type == BezierPointType.IN) {
            toUpdate.updatedInControlPointAt(positionToModify, originalPosition);
        } else {
            toUpdate.updatedOutControlPointAt(positionToModify, originalPosition);
        }
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
        private Point newPoint;
        private BezierPointType type;
        private String providerId;
        private Point originalPosition;
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

        public Builder withNewPoint(Point newPoint) {
            this.newPoint = newPoint;
            return this;
        }

        public Builder withType(BezierPointType type) {
            this.type = type;
            return this;
        }

        public Builder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder withOriginalPosition(Point originalPosition) {
            this.originalPosition = originalPosition;
            return this;
        }

        public Builder withRevertable(boolean revertable) {
            this.revertable = revertable;
            return this;
        }

        public BezierCurveEditorPointChangeInOutPointCommand build() {
            return new BezierCurveEditorPointChangeInOutPointCommand(this);
        }
    }

}

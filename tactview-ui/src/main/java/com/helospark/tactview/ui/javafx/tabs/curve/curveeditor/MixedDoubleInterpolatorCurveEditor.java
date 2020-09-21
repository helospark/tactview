package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.EasingInterpolatorContextMenuItem;

import javafx.scene.control.MenuItem;

@Component
public class MixedDoubleInterpolatorCurveEditor extends TypeSupportingPointBasedKeyframeDoubleCurveEditor<MixedDoubleInterpolator> {
    private EasingInterpolatorContextMenuItem easingInterpolatorContextMenuItem;

    public MixedDoubleInterpolatorCurveEditor(EasingInterpolatorContextMenuItem easingInterpolatorContextMenuItem, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository) {
        super(MixedDoubleInterpolator.class, commandInterpreter, effectParametersRepository);
        this.easingInterpolatorContextMenuItem = easingInterpolatorContextMenuItem;
    }

    @Override
    protected void valueModifiedAtInternal(MixedDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        currentKeyframeableEffect.valueModifiedAt(timelinePosition, newTime, newValue);
    }

    @Override
    protected List<MenuItem> contextMenuForElementIndex(int elementIndex, CurveEditorMouseRequest request) {
        TimelinePosition position = getKeyframePoints(((MixedDoubleInterpolator) request.currentDoubleInterpolator)).get(elementIndex).timelinePosition;
        return List.of(easingInterpolatorContextMenuItem.createInterpolators(request.currentProvider.getId(), position));
    }

}

package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.EffectInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.KeyframeSupportingDoubleInterpolator;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

public abstract class TypeSupportingPointBasedKeyframeDoubleCurveEditor<T extends EffectInterpolator> extends AbstractGeneralPointBasedCurveEditor {
    Class<T> type;

    public TypeSupportingPointBasedKeyframeDoubleCurveEditor(Class<T> type, UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        super(commandInterpreter, effectParametersRepository);
        this.type = type;
    }

    @Override
    public boolean supports(EffectInterpolator interpolator) {
        return type.isAssignableFrom(interpolator.getClass());
    }

    @Override
    protected List<KeyframePoint> getKeyframePoints(KeyframeSupportingDoubleInterpolator effect) {
        Map<TimelinePosition, Object> values = effect.getValues();
        return values.entrySet()
                .stream()
                .map(entry -> new KeyframePoint(entry.getKey(), (Double) entry.getValue()))
                .collect(Collectors.toList());
    }

}

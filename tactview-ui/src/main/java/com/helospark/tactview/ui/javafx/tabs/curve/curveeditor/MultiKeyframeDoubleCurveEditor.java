package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.function.MultiKeyframeDoubleInterpolatorFunctionFactory;

import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;

@Component
public class MultiKeyframeDoubleCurveEditor extends TypeSupportingPointBasedKeyframeDoubleCurveEditor<MultiKeyframeBasedDoubleInterpolator> {
    private List<MultiKeyframeDoubleInterpolatorFunctionFactory> interpolatorFunctionFactories;

    public MultiKeyframeDoubleCurveEditor(List<MultiKeyframeDoubleInterpolatorFunctionFactory> interpolatorFunctionFactories) {
        super(MultiKeyframeBasedDoubleInterpolator.class);
        this.interpolatorFunctionFactories = interpolatorFunctionFactories;
    }

    @Override
    public void initializeControl(ControlInitializationRequest request) {
        ComboBox<String> comboBox = new ComboBox<>();

        interpolatorFunctionFactories.stream()
                .forEach(factory -> comboBox.getItems().add(factory.getId()));
        MultiKeyframeBasedDoubleInterpolator interpolator = (MultiKeyframeBasedDoubleInterpolator) request.effectInterpolator;

        comboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((options, oldValue, newValue) -> {
                    interpolatorFunctionFactories.stream()
                            .filter(f -> f.getId().equals(newValue))
                            .findFirst()
                            .ifPresent(f -> {
                                // TODO: command interpreter
                                interpolator.setInterpolatorFunction(f.createInterpolator());
                            });

                });

    }

    @Override
    protected void valueModifiedAtInternal(MultiKeyframeBasedDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        currentKeyframeableEffect.valueModifiedAt(timelinePosition, newTime, newValue);
    }

    @Override
    protected List<MenuItem> contextMenuForElementIndex(int elementIndex, CurveEditorMouseRequest request) {
        return List.of();
    }

}

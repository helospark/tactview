package com.helospark.tactview.ui.javafx.tabs.curve.curveeditor;

import java.math.BigDecimal;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.EasingInterpolatorContextMenuItem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.input.MouseButton;

@Component
public class MixedDoubleInterpolatorCurveEditor extends TypeSupportingPointBasedKeyframeDoubleCurveEditor<MixedDoubleInterpolator> {
    private EasingInterpolatorContextMenuItem easingInterpolatorContextMenuItem;

    public MixedDoubleInterpolatorCurveEditor(EasingInterpolatorContextMenuItem easingInterpolatorContextMenuItem) {
        super(MixedDoubleInterpolator.class);
        this.easingInterpolatorContextMenuItem = easingInterpolatorContextMenuItem;
    }

    @Override
    public boolean onMouseClicked(CurveEditorMouseRequest request) {
        if (request.event.getButton().equals(MouseButton.SECONDARY)) {
            Menu menu = easingInterpolatorContextMenuItem.createInterpolators(request.currentProvider.getId(), new TimelinePosition(new BigDecimal(request.remappedMousePosition.x)));

            ContextMenu contextMenu = new ContextMenu(menu);

            contextMenu.show(request.canvas.getScene().getWindow(), request.event.getScreenX(), request.event.getScreenY());
        }
        return false;
    }

    @Override
    protected void valueModifiedAtInternal(MixedDoubleInterpolator currentKeyframeableEffect, TimelinePosition timelinePosition, TimelinePosition newTime, double newValue) {
        currentKeyframeableEffect.valueModifiedAt(timelinePosition, newTime, newValue);
    }

}

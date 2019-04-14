package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.ui.javafx.tabs.curve.CurveEditorTab;

import javafx.scene.control.MenuItem;

@Order(-9)
@Component
public class DoubleInterpolatorSupportingContextMenuItem implements PropertyValueContextMenuItem {
    private CurveEditorTab curveEditorTab;

    public DoubleInterpolatorSupportingContextMenuItem(CurveEditorTab curveEditorTab) {
        this.curveEditorTab = curveEditorTab;
    }

    @Override
    public boolean supports(PropertyValueContextMenuRequest request) {
        return request.valueProvider.getInterpolator() instanceof DoubleInterpolator;
    }

    @Override
    public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
        MenuItem revealInEditorMenuItem = new MenuItem("Reveal curve in editor");
        revealInEditorMenuItem.setOnAction(e -> {
            curveEditorTab.revealInEditor(request.valueProvider);
        });
        return revealInEditorMenuItem;
    }

}

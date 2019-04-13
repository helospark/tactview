package com.helospark.tactview.ui.javafx.tabs;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.tabs.curve.CurveEditorTab;

import javafx.scene.control.Tab;

@Component
@Order(10)
public class InterpolatorCurveEditorTabFactory implements TabFactory {
    private CurveEditorTab curveEditorTab;

    public InterpolatorCurveEditorTabFactory(CurveEditorTab curveEditorTab) {
        this.curveEditorTab = curveEditorTab;
    }

    @Override
    public Tab createTabContent() {
        return curveEditorTab;
    }

}

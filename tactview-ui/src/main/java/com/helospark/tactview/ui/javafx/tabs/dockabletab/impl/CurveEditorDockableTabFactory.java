package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.tabs.curve.CurveEditorTab;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

@Component
public class CurveEditorDockableTabFactory extends AbstractCachingDockableTabFactory {
    private CurveEditorTab curveEditorTab;

    public CurveEditorDockableTabFactory(CurveEditorTab curveEditorTab) {
        this.curveEditorTab = curveEditorTab;
    }

    @Override
    public DetachableTab createTabInternal() {
        return curveEditorTab;
    }

    @Override
    public boolean doesSupport(String id) {
        return CurveEditorTab.CURVE_EDITOR_ID.equals(id);
    }

    @Override
    public String getId() {
        return CurveEditorTab.CURVE_EDITOR_ID;
    }

}

package com.helospark.tactview.ui.javafx.layout;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.AddableContentDockableTabFactory;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.CurveEditorDockableTabFactory;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.PreviewDockableTab;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.PropertyEditorDockableTabFactory;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPaneLoadModel;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.LeafElement;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.SplitPaneElement;

@Component
public class DefaultLayoutProvider {
    private PropertyEditorDockableTabFactory propertyEditorDockableTabFactory;
    private AddableContentDockableTabFactory addableContentDockableTabFactory;
    private CurveEditorDockableTabFactory curveEditorDockableTabFactory;
    private PreviewDockableTab previewDockableTabFactory;

    public DefaultLayoutProvider(PropertyEditorDockableTabFactory propertyEditorDockableTabFactory, AddableContentDockableTabFactory addableContentDockableTabFactory,
            CurveEditorDockableTabFactory curveEditorDockableTabFactory, PreviewDockableTab previewDockableTabFactory) {
        this.propertyEditorDockableTabFactory = propertyEditorDockableTabFactory;
        this.addableContentDockableTabFactory = addableContentDockableTabFactory;
        this.curveEditorDockableTabFactory = curveEditorDockableTabFactory;
        this.previewDockableTabFactory = previewDockableTabFactory;
    }

    public DetachableTabPaneLoadModel provideDefaultLayout() {
        SplitPaneElement splitPaneElement = new SplitPaneElement();
        splitPaneElement.isVertical = false;
        splitPaneElement.size = new double[]{0.2, 0.6, 0.2};

        DetachableTab propertyEditorTab = propertyEditorDockableTabFactory.createTab();
        DetachableTab addableContentEditorTab = addableContentDockableTabFactory.createTab();
        DetachableTab curveEditorTab = curveEditorDockableTabFactory.createTab();
        DetachableTab previewTab = previewDockableTabFactory.createTab();

        splitPaneElement.children.add(new LeafElement(List.of(propertyEditorTab)));
        splitPaneElement.children.add(new LeafElement(List.of(addableContentEditorTab, curveEditorTab)));
        splitPaneElement.children.add(new LeafElement(List.of(previewTab)));
        return new DetachableTabPaneLoadModel(splitPaneElement);
    }

}

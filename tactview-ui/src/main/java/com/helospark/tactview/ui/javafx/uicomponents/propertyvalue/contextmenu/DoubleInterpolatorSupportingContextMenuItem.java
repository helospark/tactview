package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import static com.helospark.tactview.ui.javafx.tabs.dockabletab.OpenDetachableTabTarget.MAIN_WINDOW;

import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.DoubleInterpolator;
import com.helospark.tactview.ui.javafx.tabs.curve.CurveEditorTab;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository.OpenTabRequest;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.AddableContentDockableTabFactory;

import javafx.scene.control.MenuItem;

@Order(-9)
@Component
public class DoubleInterpolatorSupportingContextMenuItem implements PropertyValueContextMenuItem {
    private CurveEditorTab curveEditorTab;
    private DockableTabRepository dockableTabRepository;

    public DoubleInterpolatorSupportingContextMenuItem(CurveEditorTab curveEditorTab, DockableTabRepository dockableTabRepository) {
        this.curveEditorTab = curveEditorTab;
        this.dockableTabRepository = dockableTabRepository;
    }

    @Override
    public boolean supports(PropertyValueContextMenuRequest request) {
        return request.valueProvider.getInterpolator() instanceof DoubleInterpolator;
    }

    @Override
    public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
        MenuItem revealInEditorMenuItem = new MenuItem("Reveal curve in editor");
        revealInEditorMenuItem.setOnAction(e -> {
            OpenTabRequest tabOpenRequest = OpenTabRequest.builder()
                    .withTabToOpen(curveEditorTab)
                    .withTarget(MAIN_WINDOW)
                    .withSameTabPaneAs(Optional.of(AddableContentDockableTabFactory.ID))
                    .build();
            dockableTabRepository.openTab(tabOpenRequest);
            curveEditorTab.revealInEditor(request.valueProvider);
        });
        return revealInEditorMenuItem;
    }

}

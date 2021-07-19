package com.helospark.tactview.ui.javafx.tabs.dockabletab;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPane;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPaneLoadModel;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

@Component
public class DockableTabRepository {
    public Parent dockContainer;
    private HBox parentPane;

    private UiMessagingService uiMessagingService;
    private StylesheetAdderService stylesheetAdderService;

    public DockableTabRepository(UiMessagingService uiMessagingService, StylesheetAdderService stylesheetAdderService) {
        this.uiMessagingService = uiMessagingService;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public boolean isTabOpen(String id) {
        if (dockContainer == null) {
            return false;
        }
        return findTabpaneOpenInternal(dockContainer, id).isPresent();
    }

    private Optional<DetachableTabPane> findTabpaneOpenInternal(Parent dockContainer2, String id) {
        Set<Node> tabPanes = dockContainer2.lookupAll(".detachable-tab-pane");

        return tabPanes.stream()
                .filter(a -> a instanceof DetachableTabPane)
                .map(a -> (DetachableTabPane) a)
                .flatMap(a -> getTabOpenInPane(a, id).stream())
                .findFirst();
    }

    private Optional<DetachableTabPane> getTabOpenInPane(DetachableTabPane pane, String id) {
        ObservableList<Tab> tabs = pane.getTabs();

        Optional<DetachableTab> detachableTabResult = tabs.stream()
                .filter(a -> a instanceof DetachableTab)
                .map(a -> (DetachableTab) a)
                .filter(a -> a.getTabId().equals(id))
                .findFirst();

        if (detachableTabResult.isPresent()) {
            return Optional.of((DetachableTabPane) detachableTabResult.get().getTabPane());
        }

        for (var stage : pane.getChildStages()) {
            Optional<DetachableTabPane> result = findTabpaneOpenInternal(stage.getScene().getRoot(), id);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    public void setDockContainer(Parent dockContainer) {
        this.dockContainer = dockContainer;
    }

    public boolean closeTab(String id) {
        Optional<DetachableTabPane> optionalTabPane = findTabpaneOpenInternal(dockContainer, id);
        if (optionalTabPane.isPresent()) {
            var tabPane = optionalTabPane.get();
            for (int i = 0; i < tabPane.getTabs().size(); ++i) {
                if (((DetachableTab) tabPane.getTabs().get(i)).getTabId().equals(id)) {
                    tabPane.getTabs().remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public void openTab(DetachableTab tab) {
        Optional<DetachableTabPane> optionalTabPane = findTabpaneOpenInternal(dockContainer, tab.getTabId());
        if (optionalTabPane.isEmpty()) {
            Set<Node> tabPanes = dockContainer.lookupAll(".detachable-tab-pane");
            if (tabPanes.size() > 0) {
                DetachableTabPane planeToAddTab = tabPanes.stream()
                        .filter(a -> a instanceof DetachableTabPane)
                        .findFirst()
                        .map(a -> (DetachableTabPane) a)
                        .get();
                planeToAddTab.getTabs().add(tab);
                planeToAddTab.getSelectionModel().select(tab);
            }
        }
    }

    public List<DetachableTabPane> findNodesNotContainingId(String id) {
        Optional<DetachableTabPane> containingTabpane = findTabpaneOpenInternal(dockContainer, id);

        var result = findAllTabpanes(dockContainer);

        if (containingTabpane.isPresent()) {
            for (int i = 0; i < result.size(); ++i) {
                if (result.get(i).equals(containingTabpane.get())) {
                    result.remove(i);
                    break;
                }
            }
        }
        return result;
    }

    private List<DetachableTabPane> findAllTabpanes(Parent dockContainer2) {
        List<DetachableTabPane> result = new ArrayList<>();

        Set<Node> tabPanes = dockContainer2.lookupAll(".detachable-tab-pane");

        var directResult = tabPanes.stream()
                .filter(a -> a instanceof DetachableTabPane)
                .map(a -> (DetachableTabPane) a)
                .collect(Collectors.toList());

        for (var element : directResult) {
            if (element.getChildStages().size() > 0) {
                for (var childStage : element.getChildStages()) {
                    result.addAll(findAllTabpanes(childStage.getScene().getRoot()));
                }
            }
        }

        result.addAll(directResult);

        return result;
    }

    public DetachableTabPaneLoadModel extractLoadModel() {
        return DetachableTabPane.extractCurrentModel(dockContainer);
    }

    public void loadAndSetModelToParent(DetachableTabPaneLoadModel detachableTabPaneLoadModel) {
        if (parentPane != null && dockContainer != null) {
            DetachableTabPane.close(parentPane);
        }

        dockContainer = DetachableTabPane.loadModel(detachableTabPaneLoadModel, uiMessagingService, stylesheetAdderService);
        HBox.setHgrow(dockContainer, Priority.ALWAYS);
        parentPane.getChildren().clear();
        parentPane.getChildren().add(dockContainer);
    }

    public void setParentPane(HBox upperPane) {
        this.parentPane = upperPane;
    }

}

package com.helospark.tactview.ui.javafx.tabs.dockabletab;

import static com.helospark.tactview.ui.javafx.tabs.dockabletab.OpenDetachableTabTarget.MAIN_WINDOW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.tabs.listener.TabOpenListener;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPane;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPane.TabStage;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTabPaneLoadModel;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

@Component
public class DockableTabRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockableTabRepository.class);
    public Parent dockContainer;
    private HBox parentPane;

    private UiMessagingService uiMessagingService;
    private StylesheetAdderService stylesheetAdderService;

    public DockableTabRepository(UiMessagingService uiMessagingService, StylesheetAdderService stylesheetAdderService) {
        this.uiMessagingService = uiMessagingService;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public boolean isTabOpen(String id) {
        try {
            if (dockContainer == null) {
                return false;
            }
            return findTabpaneOpenInternal(dockContainer, id).isPresent();
        } catch (Exception e) {
            LOGGER.error("Unable to determine if tab open", e);
            return false;
        }
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

    public void openTab(OpenTabRequest request) {
        var tab = request.tabToOpen;
        Optional<DetachableTabPane> optionalTabPane = findTabpaneOpenInternal(dockContainer, tab.getTabId());
        if (optionalTabPane.isEmpty()) {

            if (request.target.equals(OpenDetachableTabTarget.MAIN_WINDOW)) {
                DetachableTabPane planeToAddTab;
                if (request.sameTabPaneAs.isPresent()) {
                    planeToAddTab = findTabPaneWithId(request.sameTabPaneAs.get()).orElseGet(() -> findMainStageTabPane());
                } else {
                    planeToAddTab = findMainStageTabPane();
                }
                planeToAddTab.getTabs().add(tab);
                planeToAddTab.getSelectionModel().select(tab);
            } else {
                DetachableTabPane mainStage = findMainStageTabPane();
                TabStage stage = mainStage.openStage(tab);
                if (request.preferredSize.isPresent()) {
                    stage.setWidth(request.preferredSize.get().x);
                    stage.setHeight(request.preferredSize.get().y);
                }
            }
        } else {
            optionalTabPane.get().getSelectionModel().select(request.tabToOpen);
        }
        var tabToOpen = request.tabToOpen;
        if (tabToOpen instanceof TabOpenListener) {
            ((TabOpenListener) tabToOpen).tabOpened();
        }
    }

    private DetachableTabPane findMainStageTabPane() {
        Set<Node> tabPanes = dockContainer.lookupAll(".detachable-tab-pane");
        return tabPanes.stream()
                .filter(a -> a instanceof DetachableTabPane)
                .findFirst()
                .map(a -> (DetachableTabPane) a)
                .get();
    }

    private Optional<DetachableTabPane> findTabPaneWithId(String tabId) {
        Set<Node> tabPanes = dockContainer.lookupAll(".detachable-tab-pane");
        Optional<DetachableTabPane> result = tabPanes.stream()
                .filter(a -> a instanceof DetachableTabPane)
                .filter(a -> isTabWithIdInTabPane((DetachableTabPane) a, tabId))
                .findFirst()
                .map(a -> (DetachableTabPane) a);

        // Handle tabpane in windows
        return result;
    }

    private boolean isTabWithIdInTabPane(DetachableTabPane tabPane, String tabId) {
        return tabPane.getTabs()
                .stream()
                .filter(tab -> ((DetachableTab) tab).getTabId().equals(tabId))
                .findFirst()
                .isPresent();
    }

    public void openTab(DetachableTab tab) {
        OpenTabRequest request = OpenTabRequest.builder()
                .withTabToOpen(tab)
                .withTarget(MAIN_WINDOW)
                .build();
        openTab(request);
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

    public boolean isTabVisibleWithId(String id) {
        return isTabOpen(id); // TODO: check if tab is selected
    }

    public static class OpenTabRequest {
        DetachableTab tabToOpen;
        OpenDetachableTabTarget target;
        Optional<String> sameTabPaneAs;
        Optional<Point> preferredSize;

        private OpenTabRequest(Builder builder) {
            this.tabToOpen = builder.tabToOpen;
            this.target = builder.target;
            this.sameTabPaneAs = builder.sameTabPaneAs;
            this.preferredSize = builder.preferredSize;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private DetachableTab tabToOpen;
            private OpenDetachableTabTarget target;
            private Optional<String> sameTabPaneAs = Optional.empty();
            private Optional<Point> preferredSize = Optional.empty();

            private Builder() {
            }

            public Builder withTabToOpen(DetachableTab tabToOpen) {
                this.tabToOpen = tabToOpen;
                return this;
            }

            public Builder withTarget(OpenDetachableTabTarget target) {
                this.target = target;
                return this;
            }

            public Builder withSameTabPaneAs(Optional<String> sameTabPaneAs) {
                this.sameTabPaneAs = sameTabPaneAs;
                return this;
            }

            public Builder withPreferredSize(Optional<Point> preferredSize) {
                this.preferredSize = preferredSize;
                return this;
            }

            public OpenTabRequest build() {
                return new OpenTabRequest(this);
            }
        }

    }

}

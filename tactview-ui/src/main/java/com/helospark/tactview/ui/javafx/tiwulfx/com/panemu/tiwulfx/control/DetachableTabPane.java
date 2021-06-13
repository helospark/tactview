/*
 * License GNU LGPL
 * Copyright (C) 2013 Amrullah .
 */
package com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author amrullah
 */
public class DetachableTabPane extends TabPane {

    /**
     * hold reference to the source of drag event. We can't use
     * event.getGestureSource() because it is null when the target on a different
     * stage
     */
    private static Side SIDE = Side.BOTTOM;
    private static DetachableTabPane DRAG_SOURCE;
    private static Tab DRAGGED_TAB;
    private StringProperty scope = new SimpleStringProperty("");
    private static final Path path = new Path();
    private static final DetachableTabPathModel pathModel = new DetachableTabPathModel(path);
    private Pos pos = null;
    private int dropIndex = 0;
    private static final Logger logger = Logger.getLogger(DetachableTabPane.class.getName());
    private List<Double> lstTabPoint = new ArrayList<>();
    private boolean closeIfEmpty = false;

    public DetachableTabPane() {
        super();
        getStyleClass().add("detachable-tab-pane");
        attachListeners();
    }

    private Button btnTop;
    private Button btnRight;
    private Button btnBottom;
    private Button btnLeft;
    private StackPane dockPosIndicator;
    private GridPane posGrid;

    private List<TabStage> childStages = new ArrayList<>();

    private void initDropButton() {
        btnTop = new Button();
        btnTop.getStyleClass().add("drop-top");
        btnRight = new Button();
        btnRight.getStyleClass().add("drop-right");
        btnBottom = new Button();
        btnBottom.getStyleClass().add("drop-bottom");
        btnLeft = new Button();
        btnLeft.getStyleClass().add("drop-left");
        posGrid = new GridPane();
        posGrid.add(btnTop, 1, 0);
        posGrid.add(btnRight, 2, 1);
        posGrid.add(btnBottom, 1, 2);
        posGrid.add(btnLeft, 0, 1);
        posGrid.getStyleClass().add("dock-pos-indicator");
        dockPosIndicator = new StackPane();
        dockPosIndicator.getChildren().add(posGrid);
        dockPosIndicator.setLayoutX(100);
        dockPosIndicator.setLayoutY(100);
    }

    /**
     * Get drag scope id
     *
     * @return
     */
    public String getScope() {
        return scope.get();
    }

    /**
     * Set scope id. Only TabPane having the same scope that could be drop
     * target. Default is empty string. So the default behavior is this TabPane
     * could receive tab from empty scope DragAwareTabPane
     *
     * @param scope
     */
    public void setScope(String scope) {
        this.scope.set(scope);
    }

    /**
     * Scope property. Only TabPane having the same scope that could be drop
     * target.
     *
     * @return
     */
    public StringProperty scopeProperty() {
        return scope;
    }

    private void attachListeners() {

        /**
         * This listener detects when the TabPane is shown. Then it will call
         * initiateDragGesture. It because the lookupAll call in that method only
         * works if the stage containing this instance is already shown.
         */
        sceneProperty().addListener((ObservableValue<? extends Scene> ov, Scene t, Scene t1) -> {
            if (t == null && t1 != null) {
                if (getScene().getWindow() != null) {
                    Platform.runLater(() -> {
                        initiateDragGesture(true);
                    });
                } else {
                    getScene().windowProperty().addListener((ObservableValue<? extends Window> ov1, Window t2, Window t3) -> {
                        if (t2 == null && t3 != null) {
                            t3.addEventHandler(WindowEvent.WINDOW_SHOWN, (t4) -> {
                                initiateDragGesture(true);
                            });
                        }
                    });
                }
            }
        });

        this.addEventHandler(DragEvent.ANY, (DragEvent event) -> {
            try {
                if (DRAG_SOURCE == null) {
                    return;
                }
                if (event.getEventType() == DragEvent.DRAG_OVER) {
                    if (DetachableTabPane.this.scope.get().equals(DRAG_SOURCE.getScope())) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        repaintPath(event, 1);
                    }
                    event.consume();
                } else if (event.getEventType() == DragEvent.DRAG_EXITED) {
                    if (DetachableTabPane.this.getSkin() instanceof TabPaneSkin) {
                        TabPaneSkin sp = (TabPaneSkin) getSkin();
                        sp.getChildren().remove(path);
                        sp.getChildren().remove(dockPosIndicator);
                        DetachableTabPane.this.requestLayout();
                    }
                } else if (event.getEventType() == DragEvent.DRAG_ENTERED) {
                    if (!DetachableTabPane.this.scope.get().equals(DRAG_SOURCE.getScope())) {
                        return;
                    }
                    calculateTabPoints();
                    if (dockPosIndicator == null) {
                        initDropButton();
                    }
                    double layoutX = DetachableTabPane.this.getWidth() / 2;
                    double layoutY = DetachableTabPane.this.getHeight() / 2;
                    dockPosIndicator.setLayoutX(layoutX);
                    dockPosIndicator.setLayoutY(layoutY);
                    if (DetachableTabPane.this.getSkin() instanceof TabPaneSkin) {
                        TabPaneSkin sp = (TabPaneSkin) getSkin();
                        if (!sp.getChildren().contains(path)) {
                            if (!getTabs().isEmpty()) {
                                sp.getChildren().add(dockPosIndicator);
                            }
                            repaintPath(event, 2);
                            sp.getChildren().add(path);
                        }
                    }
                } else if (event.getEventType() == DragEvent.DRAG_DROPPED) {
                    if (pos != null) {
                        adjacent();
                        event.setDropCompleted(true);
                        event.consume();
                        return;
                    }
                    if (DRAG_SOURCE != null && DRAG_SOURCE != DetachableTabPane.this) {
                        final Tab selectedtab = DRAGGED_TAB;
                        DetachableTabPane.this.getTabs().add(dropIndex, selectedtab);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                DetachableTabPane.this.getSelectionModel().select(selectedtab);
                            }
                        });
                        event.setDropCompleted(true);
                    } else {
                        event.setDropCompleted(DRAG_SOURCE == DetachableTabPane.this);
                        final Tab selectedtab = DRAGGED_TAB;
                        int currentSelectionIndex = getTabs().indexOf(selectedtab);
                        if (dropIndex == currentSelectionIndex) {
                            return;
                        }
                        getTabs().add(dropIndex, selectedtab);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                DetachableTabPane.this.getSelectionModel().select(selectedtab);
                            }
                        });
                    }
                    if (event.isDropCompleted()) {
                        event.getDragboard().setContent(null);
                    }
                    event.consume();
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });

        getTabs().addListener((ListChangeListener.Change<? extends Tab> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    if (getScene() != null && getScene().getWindow() != null) {
                        if (getScene().getWindow().isShowing()) {
                            Platform.runLater(() -> {
                                clearGesture();
                                initiateDragGesture(true);
                                /**
                                 * We need to use timer to wait until the
                                 * tab-add-animation finish
                                 */
                                futureCalculateTabPoints();
                            });
                        }
                    }
                } else if (change.wasRemoved()) {
                    /**
                     * We need to use timer to give the system some time to remove
                     * the tab from TabPaneSkin.
                     */
                    futureCalculateTabPoints();

                    if (DRAG_SOURCE == null) {
                        //it means we are not dragging
                        if (getScene() != null && getScene().getWindow() instanceof TabStage) {
                            TabStage stage = (TabStage) getScene().getWindow();
                            closeStageIfNeeded(stage);
                        }

                        if (getTabs().isEmpty()) {
                            removeFromParent(DetachableTabPane.this);
                        }
                    }
                }
            }
        });

    }

    private SplitPane findParentSplitPane(Node control) {
        if (control.getParent() == null)
            return null;
        Set<Node> lstSplitpane = control.getScene().getRoot().lookupAll(".split-pane");
        SplitPane parentSplitpane = null;
        for (Node node : lstSplitpane) {
            if (node instanceof SplitPane) {
                SplitPane splitpane = (SplitPane) node;
                if (splitpane.getItems().contains(control)) {
                    parentSplitpane = splitpane;
                    break;
                }
            }
        }
        return parentSplitpane;
    }

    private void adjacent() {
        SplitPane targetSplitPane = findParentSplitPane(DetachableTabPane.this);
        final Tab selectedtab = DRAGGED_TAB;

        if (getParent() == null) {
            //it means the tabpane is the root of the scene.
            Scene scene = getScene();
            StackPane wrapper = new StackPane();
            wrapper.getChildren().add(this);
            scene.setRoot(wrapper);
        }

        Parent parent = getParent();

        Orientation requestedOrientation = Orientation.HORIZONTAL;
        if (pos == Pos.BOTTOM_CENTER || pos == Pos.TOP_CENTER) {
            requestedOrientation = Orientation.VERTICAL;
        }

        int requestedIndex = 0;
        if (targetSplitPane != null && requestedOrientation == targetSplitPane.getOrientation()) {
            requestedIndex = targetSplitPane.getItems().indexOf(DetachableTabPane.this);
        }
        if (pos == Pos.CENTER_RIGHT || pos == Pos.BOTTOM_CENTER) {
            requestedIndex++;
        }

        if (targetSplitPane == null) {
            targetSplitPane = createSplitPane();
            targetSplitPane.setOrientation(requestedOrientation);

            if (parent instanceof Pane) {
                Pane pane = (Pane) parent;
                int index = pane.getChildren().indexOf(DetachableTabPane.this);
                pane.getChildren().remove(DetachableTabPane.this);
                pane.getChildren().add(index, targetSplitPane);
                targetSplitPane.getItems().add(DetachableTabPane.this);
                DetachableTabPane dt = detachableTabPaneFactory.create(this);
                dt.getTabs().add(selectedtab);
                targetSplitPane.getItems().add(requestedIndex, dt);
            }

        } else {
            if (targetSplitPane.getItems().size() == 1) {
                targetSplitPane.setOrientation(requestedOrientation);
            }
            if (targetSplitPane.getOrientation() == requestedOrientation) {
                DetachableTabPane dt = detachableTabPaneFactory.create(this);
                dt.getTabs().add(selectedtab);
                targetSplitPane.getItems().add(requestedIndex, dt);
                int itemCount = targetSplitPane.getItems().size();
                double[] dividerPos = new double[itemCount];
                dividerPos[0] = 1d / itemCount;
                for (int i = 1; i < dividerPos.length; i++) {
                    dividerPos[i] = dividerPos[i - 1] + dividerPos[0];
                }
                targetSplitPane.setDividerPositions(dividerPos);
            } else {
                int indexTabPane = targetSplitPane.getItems().indexOf(DetachableTabPane.this);
                targetSplitPane.getItems().remove(DetachableTabPane.this);
                SplitPane innerSplitpane = createSplitPane();
                targetSplitPane.getItems().add(indexTabPane, innerSplitpane);
                innerSplitpane.setOrientation(requestedOrientation);
                innerSplitpane.getItems().add(DetachableTabPane.this);
                DetachableTabPane dt = detachableTabPaneFactory.create(this);
                dt.getTabs().add(selectedtab);
                innerSplitpane.getItems().add(requestedIndex, dt);
            }
        }
    }

    private static SplitPane createSplitPane() {
        SplitPane targetSplitPane;
        targetSplitPane = new SplitPane();
        targetSplitPane.getStyleClass().add("detachable-tab-pane");
        return targetSplitPane;
    }

    private void futureCalculateTabPoints() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                calculateTabPoints();
                timer.cancel();
                timer.purge();
            }
        }, 1000);
    }

    /**
     * The lookupAll call in this method only works if the stage containing this
     * instance is already shown.
     */
    private void initiateDragGesture(boolean retryOnFailed) {
        Node tabheader = getTabHeaderArea();
        if (tabheader == null) {
            if (retryOnFailed) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        initiateDragGesture(false);
                        timer.cancel();
                        timer.purge();
                    }
                }, 500);

            }
            return;
        }
        Set<Node> tabs = tabheader.lookupAll(".tab");
        if (tabs.isEmpty() && !getTabs().isEmpty()) {
            logger.warning("Failed to initiate drag gesture. There are no tabs.");
        }
        for (Node node : tabs) {
            addGesture(this, node);
        }
    }

    private Node getTabHeaderArea() {
        Node tabheader = null;
        for (Node node : this.getChildrenUnmodifiable()) {
            if (node.getStyleClass().contains("tab-header-area")) {
                tabheader = node;
                break;
            }
        }
        return tabheader;
    }

    private void calculateTabPoints() {
        lstTabPoint.clear();
        lstTabPoint.add(0d);
        Node tabheader = getTabHeaderArea();
        if (tabheader == null)
            return;
        Set<Node> tabs = tabheader.lookupAll(".tab");
        Point2D inset = DetachableTabPane.this.localToScene(0, 0);
        for (Node node : tabs) {
            Bounds bound = node.getLayoutBounds();
            Point2D point = node.localToScene(bound.getWidth(), 0); // TODO: why is this mirrored?
            lstTabPoint.add(point.getX() + bound.getWidth() - inset.getX());
        }
        //		logger.log(Level.INFO, "tab points " + Arrays.deepToString(lstTabPoint.toArray()));
    }

    private void repaintPath(DragEvent event, int source) {
        boolean hasTab = !getTabs().isEmpty();
        if (hasTab && btnLeft.contains(btnLeft.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            pathModel.refresh(0, 0, DetachableTabPane.this.getWidth() / 2, DetachableTabPane.this.getHeight());
            pos = Pos.CENTER_LEFT;
        } else if (hasTab && btnRight.contains(btnRight.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            double pathWidth = DetachableTabPane.this.getWidth() / 2;
            pathModel.refresh(pathWidth, 0, pathWidth, DetachableTabPane.this.getHeight());
            pos = Pos.CENTER_RIGHT;
        } else if (hasTab && btnTop.contains(btnTop.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            pathModel.refresh(0, 0, getWidth(), getHeight() / 2);
            pos = Pos.TOP_CENTER;
        } else if (hasTab && btnBottom.contains(btnBottom.screenToLocal(event.getScreenX(), event.getScreenY()))) {
            double pathHeight = getHeight() / 2;
            pathModel.refresh(0, pathHeight, getWidth(), getHeight() - pathHeight);
            pos = Pos.BOTTOM_CENTER;
        } else {
            pos = null;
            double tabpos = -1;
            calculateTabPoints();
            for (int i = 1; i < lstTabPoint.size(); i++) {
                if (event.getX() < lstTabPoint.get(i)) {
                    tabpos = lstTabPoint.get(i - 1);
                    dropIndex = i - 1;
                    break;
                }
            }
            if (tabpos == -1) {
                int index = lstTabPoint.size() - 1;
                dropIndex = getTabs().size();
                if (index > -1) {
                    tabpos = lstTabPoint.get(index);
                }
            }
            //			logger.info("drop index: " + dropIndex);
            pathModel.refresh(tabpos, DetachableTabPane.this.getWidth(), DetachableTabPane.this.getHeight(), SIDE);
        }
    }

    private void clearGesture() {
        Node tabheader = getTabHeaderArea();
        if (tabheader == null)
            return;
        Set<Node> tabs = tabheader.lookupAll(".tab");
        for (Node node : tabs) {
            node.setOnDragDetected(null);
            node.setOnDragDone(null);
        }
    }
    private static final DataFormat DATA_FORMAT = new DataFormat("dragAwareTab");

    private void addGesture(final TabPane tabPane, final Node node) {
        node.setOnDragDetected((MouseEvent e) -> {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab instanceof DetachableTab && !((DetachableTab) tab).isDetachable()) {
                return;
            }
            Dragboard db = node.startDragAndDrop(TransferMode.ANY);
            db.setDragView(node.snapshot(null, null));
            Map<DataFormat, Object> dragContent = new HashMap<>();
            dragContent.put(DATA_FORMAT, "test");
            DetachableTabPane.DRAG_SOURCE = DetachableTabPane.this;
            DRAGGED_TAB = tab;
            getTabs().remove(DRAGGED_TAB);
            db.setContent(dragContent);
            e.consume();
        });

        node.setOnDragDone((DragEvent event) -> {
            if (DRAGGED_TAB != null && DRAGGED_TAB.getTabPane() == null) {
                Tab tab = DRAGGED_TAB;
                childStages.add(new TabStage(tab, this.getScene()));
            }
            if (DRAG_SOURCE.getScene() != null && DRAG_SOURCE.getScene().getWindow() instanceof TabStage) {
                TabStage stage = (TabStage) DRAG_SOURCE.getScene().getWindow();
                closeStageIfNeeded(stage);
            }

            if (DRAG_SOURCE.getTabs().isEmpty()) {
                removeFromParent(DRAG_SOURCE);
            }
            DetachableTabPane.DRAG_SOURCE = null;
            DRAGGED_TAB = null;
            event.consume();
        });

    }

    private void closeStageIfNeeded(TabStage stage) {
        Set<Node> setNode = stage.getScene().getRoot().lookupAll(".tab-pane");
        boolean empty = true;
        for (Node nodeTabpane : setNode) {
            if (nodeTabpane instanceof DetachableTabPane) {
                if (!((DetachableTabPane) nodeTabpane).getTabs().isEmpty()) {
                    empty = false;
                    break;
                }
            }
        }

        if (empty) {
            //there is a case where lookup .tab-pane style doesn't return all TabPane. So we need to lookup by SplitPane and scan through it
            Set<Node> setSplitpane = stage.getScene().getRoot().lookupAll(".split-pane");
            for (Node nodeSplitpane : setSplitpane) {
                if (nodeSplitpane instanceof SplitPane) {
                    SplitPane asplitpane = (SplitPane) nodeSplitpane;
                    for (Node child : asplitpane.getItems()) {
                        if (child instanceof DetachableTabPane) {
                            DetachableTabPane dtp = (DetachableTabPane) child;
                            if (!dtp.getTabs().isEmpty()) {
                                empty = false;
                                break;
                            }
                        }
                    }
                }
                if (!empty) {
                    break;
                }
            }
        }
        if (empty) {
            childStages.remove(stage);
            stage.close();
        }
    }

    private void removeFromParent(DetachableTabPane tabPaneToRemove) {
        SplitPane sp = findParentSplitPane(tabPaneToRemove);
        if (sp == null) {
            return;
        }
        if (!tabPaneToRemove.isCloseIfEmpty()) {
            DetachableTabPane sibling = findSibling(sp, tabPaneToRemove);
            if (sibling == null) {
                return;
            }
            List<Tab> lstTab = new ArrayList(sibling.getTabs());
            sibling.getTabs().clear();
            tabPaneToRemove.getTabs().setAll(lstTab);
            tabPaneToRemove = sibling;
        }
        sp.getItems().remove(tabPaneToRemove);
        simplifySplitPane(sp);
    }

    private DetachableTabPane findSibling(SplitPane sp, DetachableTabPane tabPaneToRemove) {
        for (Node sibling : sp.getItems()) {
            if (tabPaneToRemove != sibling
                    && sibling instanceof DetachableTabPane
                    && tabPaneToRemove.getScope().equals(((DetachableTabPane) sibling).getScope())) {
                return (DetachableTabPane) sibling;
            }
        }
        for (Node sibling : sp.getItems()) {
            if (sibling instanceof SplitPane) {
                return findSibling((SplitPane) sibling, tabPaneToRemove);
            }
        }
        return null;
    }

    private void simplifySplitPane(SplitPane sp) {
        if (sp.getItems().size() != 1) {
            return;
        }
        Node content = sp.getItems().get(0);
        SplitPane parent = findParentSplitPane(sp);
        if (parent != null) {
            int index = parent.getItems().indexOf(sp);
            parent.getItems().remove(sp);
            parent.getItems().add(index, content);
            simplifySplitPane(parent);
        }
    }

    public static Node loadModel(DetachableTabPaneLoadModel model) {
        TabPaneElement rootElement = model.root;

        Node result = loadModelRecursive(rootElement);

        return result;
    }

    private static Node loadModelRecursive(TabPaneElement rootElement) {
        if (rootElement instanceof LeafElement) {
            DetachableTabPane pane = createDetachableTabPane();
            pane.getTabs().addAll(((LeafElement) rootElement).tabs);
            return pane;
        } else if (rootElement instanceof SplitPaneElement) {
            SplitPaneElement splutPaneElementsd = (SplitPaneElement) rootElement;
            List<Node> items = new ArrayList<>();

            List<TabPaneElement> splitPaneElement = splutPaneElementsd.children;
            for (var itemElement : splitPaneElement) {
                items.add(loadModelRecursive(itemElement));
            }

            SplitPane pane = createSplitPane();
            pane.getItems().addAll(items.toArray(new Node[0]));
            pane.getStyleClass().add("detachable-tab-pane");
            pane.setDividerPositions(splutPaneElementsd.size);
            pane.setOrientation(splutPaneElementsd.isVertical ? Orientation.VERTICAL : Orientation.HORIZONTAL);

            return pane;
        } else {
            throw new IllegalStateException();
        }
    }

    private static DetachableTabPane createDetachableTabPane() {
        DetachableTabPane pane = new DetachableTabPane();
        pane.setSide(SIDE);
        return pane;
    }

    public DetachableTabPaneLoadModel getLoadModel(Parent pane) {
        SplitPane splitPane = null;
        for (Node child : pane.getChildrenUnmodifiable()) {
            if (child instanceof SplitPane) {
                splitPane = (SplitPane) child;
            }
        }

        if (splitPane != null) {
            SplitPaneElement element = new SplitPaneElement();

            boolean isVertical = splitPane.getOrientation().equals(Orientation.VERTICAL);
            double[] dividerPositions = splitPane.getDividerPositions();
            element.children.addAll(getLoadableModelInternal(0, splitPane));
            element.isVertical = isVertical;
            element.size = dividerPositions;

            return new DetachableTabPaneLoadModel(element);
        } else {
            return null;
        }
    }

    private List<TabPaneElement> getLoadableModelInternal(int space, SplitPane pane) {
        List<TabPaneElement> result = new ArrayList<>();
        String indent = "";
        for (int i = 0; i < space; ++i) {
            indent += " ";
        }
        System.out.println(indent + pane);
        for (Node child : pane.getItems()) {
            if (child instanceof SplitPane) {
                SplitPaneElement childElement = new SplitPaneElement();
                SplitPane splitPane = (SplitPane) child;
                boolean isVertical = splitPane.getOrientation().equals(Orientation.VERTICAL);
                double[] dividerPositions = splitPane.getDividerPositions();
                childElement.children.addAll(getLoadableModelInternal(space + 2, splitPane));
                childElement.isVertical = isVertical;
                childElement.size = dividerPositions;
                result.add(childElement);
            } else if (child instanceof DetachableTabPane) {
                LeafElement leafElement = new LeafElement();
                List<Tab> tabs = new ArrayList<>();
                System.out.println(indent + "---");
                for (Tab tab : ((DetachableTabPane) child).getTabs()) {
                    System.out.println(indent + " - " + tab.getText());
                    tabs.add(tab);
                }
                leafElement.tabs = tabs;
                result.add(leafElement);

                System.out.println(indent + "---");
            }
        }
        return result;
    }

    /**
     * Set factory to generate the Scene. Default SceneFactory is provided and it
     * will generate a scene with TabPane as root node. Call this method if you
     * need to have a custom scene
     * <p>
     * @param sceneFactory
     */
    public void setSceneFactory(Callback<DetachableTabPane, Scene> sceneFactory) {
        this.sceneFactory = sceneFactory;
    }

    /**
     * Getter for {@link #setSceneFactory(javafx.util.Callback)}
     *
     * @return
     */
    public Callback<DetachableTabPane, Scene> getSceneFactory() {
        return this.sceneFactory;
    }

    /**
     * By default, the stage owner is the stage that own the first TabPane. For
     * example, detaching a Tab will open a new Stage. The new stage owner is the
     * stage of the TabPane. Detaching a tab from the new stage will open another
     * stage. Their owner are the same which is the stage of the first TabPane.
     * <p>
     * @param stageOwnerFactory
     */
    public void setStageOwnerFactory(Callback<Stage, Window> stageOwnerFactory) {
        this.stageOwnerFactory = stageOwnerFactory;
    }

    /**
     * Getter for {@link #setStageOwnerFactory(javafx.util.Callback)}
     *
     * @return
     */
    public Callback<Stage, Window> getStageOwnerFactory() {
        return stageOwnerFactory;
    }

    public boolean isCloseIfEmpty() {
        return closeIfEmpty;
    }

    public void setCloseIfEmpty(boolean closeIfEmpty) {
        this.closeIfEmpty = closeIfEmpty;
    }

    private static final int STAGE_WIDTH = 400;
    private Callback<DetachableTabPane, Scene> sceneFactory = new Callback<>() {

        @Override
        public Scene call(DetachableTabPane p) {
            return new Scene(p, STAGE_WIDTH, STAGE_WIDTH);
        }
    };

    private DetachableTabPaneFactory detachableTabPaneFactory = new DetachableTabPaneFactory() {
        @Override
        protected void init(DetachableTabPane a) {
        }

        @Override
        DetachableTabPane create(DetachableTabPane source) {
            DetachableTabPane result = super.create(source);
            result.setSide(SIDE);
            return result;
        }

    };

    public DetachableTabPaneFactory getDetachableTabPaneFactory() {
        return detachableTabPaneFactory;
    }

    /**
     * Factory object to create new DetachableTabPane. We can extends
     * {@link DetachableTabPaneFactory} and set it to this method when custom
     * initialization is needed. For example when we want to set different
     * TabClosingPolicy.
     *
     * @param detachableTabPaneFactory
     */
    public void setDetachableTabPaneFactory(DetachableTabPaneFactory detachableTabPaneFactory) {
        if (detachableTabPaneFactory == null) {
            throw new IllegalArgumentException("detachableTabPaneFactory cannot null");
        }
        this.detachableTabPaneFactory = detachableTabPaneFactory;
    }

    private Callback<Stage, Window> stageOwnerFactory = new Callback<>() {

        @Override
        public Window call(Stage p) {
            if (DetachableTabPane.this.getScene() == null) {
                logger.warning("unable to get parent stage");
                return null;
            }
            return DetachableTabPane.this.getScene().getWindow();
        }
    };

    private class TabStage extends Stage {

        private final DetachableTabPane tabPane;

        public TabStage(final Tab tab, Scene parentScene) {
            super();
            tabPane = detachableTabPaneFactory.create(DetachableTabPane.this);
            initOwner(stageOwnerFactory.call(this));
            Scene scene = sceneFactory.call(tabPane);
            scene.getStylesheets().addAll(parentScene.getStylesheets());

            scene.getStylesheets().addAll(DetachableTabPane.this.getScene().getStylesheets());
            scene.getRoot().getStylesheets().addAll(DetachableTabPane.this.getScene().getRoot().getStylesheets());
            setScene(scene);

            //			if (TiwulFXUtil.isMac()) {
            //				com.sun.glass.ui.Robot robot
            //						  = com.sun.glass.ui.Application.GetApplication().createRobot();
            //
            //				setX(robot.getMouseX() - (STAGE_WIDTH / 2));
            //				setY(robot.getMouseY());
            //			} else {
            Point p = MouseInfo.getPointerInfo().getLocation();
            setX(p.x - (STAGE_WIDTH / 2));
            setY(p.y);
            //			}
            show();
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            if (tab.getContent() instanceof Parent) {
                ((Parent) tab.getContent()).requestLayout();
            }
        }
    }
}

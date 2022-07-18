package com.helospark.tactview.ui.javafx.uicomponents;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.TimelineCanvas;
import com.helospark.tactview.ui.javafx.uicomponents.quicktoolbar.QuickToolbarMenuElement;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@Component
public class UiTimeline {
    private TimelineState timelineState;
    private GlobalTimelinePositionHolder uiTimelineManager;
    private TimelineCanvas timelineCanvas;
    private List<QuickToolbarMenuElement> quickToolbarMenuElements;

    private BorderPane borderPane;

    public UiTimeline(TimelineState timelineState, GlobalTimelinePositionHolder uiTimelineManager, TimelineCanvas timelineCanvas, List<QuickToolbarMenuElement> quickToolbarMenuElements) {
        this.timelineState = timelineState;
        this.uiTimelineManager = uiTimelineManager;
        this.timelineCanvas = timelineCanvas;
        this.quickToolbarMenuElements = quickToolbarMenuElements;
    }

    public BorderPane createTimeline(VBox lower, BorderPane root) {
        borderPane = new BorderPane();

        HBox titleBarTop = createTimelineButtonPanel();

        VBox timelineTopRow = new VBox();
        timelineTopRow.getChildren().add(titleBarTop);

        borderPane.setTop(timelineTopRow);

        ScrollPane timelineTitlesScrollPane = new ScrollPane();
        timelineTitlesScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        timelineTitlesScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        timelineTitlesScrollPane.vvalueProperty().bindBidirectional(timelineState.getVscroll());

        VBox timelineTitles = new VBox();
        Bindings.bindContentBidirectional(timelineState.getChannelTitlesAsNodes(), timelineTitles.getChildren());
        timelineTitlesScrollPane.setContent(timelineTitles);
        ScrollBar zoomScrollBar = new ScrollBar();
        zoomScrollBar.setId("zoom-scroll-bar");
        zoomScrollBar.setMin(TimelineState.MIN_ZOOM);
        zoomScrollBar.setMax(TimelineState.MAX_ZOOM);
        zoomScrollBar.setValue(1.0);
        zoomScrollBar.setVisibleAmount(TimelineState.MAX_ZOOM - TimelineState.MIN_ZOOM / 0.00001);
        zoomScrollBar.valueProperty().bindBidirectional((Property<Number>) timelineState.getZoomValue());
        zoomScrollBar.setOrientation(Orientation.HORIZONTAL);

        Pane topEmptyPane = new Pane();
        topEmptyPane.setId("timeline-titles-pane-top-space");

        BorderPane timelineTitlesBorderPane = new BorderPane();
        timelineTitlesScrollPane.getStyleClass().add("timeline-titles-pane");
        timelineTitlesBorderPane.setTop(topEmptyPane);
        timelineTitlesBorderPane.setCenter(timelineTitlesScrollPane);
        timelineTitlesBorderPane.setBottom(zoomScrollBar);

        ScrollPane timelineTimeLabelsScrollPane = new ScrollPane();
        timelineTimeLabelsScrollPane.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode().equals(KeyCode.LEFT)) {
                uiTimelineManager.moveBackOneFrame();
                e.consume();
            }
            if (e.getCode().equals(KeyCode.RIGHT)) {
                uiTimelineManager.moveForwardOneFrame();
                e.consume();
            }
        });
        timelineTimeLabelsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        timelineTimeLabelsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        timelineTimeLabelsScrollPane.addEventFilter(MouseEvent.ANY, e -> {
            if (e.isPrimaryButtonDown()) {
                double xPosition = e.getX();
                jumpTo(xPosition);
            }
        });

        borderPane.setCenter(timelineCanvas.create(timelineTitlesBorderPane));

        return borderPane;
    }

    protected HBox createTimelineButtonPanel() {
        List<Node> quickMenuBarElements = quickToolbarMenuElements.stream()
                .flatMap(a -> a.getQuickMenuBarElements().stream())
                .collect(Collectors.toList());

        HBox titleBarTop = new HBox();
        titleBarTop.getStyleClass().add("timeline-title-bar");
        titleBarTop.getChildren().addAll(quickMenuBarElements);
        return titleBarTop;
    }

    private void jumpTo(double xPosition) {
        TimelinePosition position = timelineState.pixelsToSeconds(xPosition).divide(BigDecimal.valueOf(timelineState.getZoom())).add(timelineState.getTimeAtLeftSide());
        uiTimelineManager.jumpAbsolute(position.getSeconds());
    }

    public void updateLine(TimelinePosition position) {
        timelineState.setLinePosition(position);
    }

}

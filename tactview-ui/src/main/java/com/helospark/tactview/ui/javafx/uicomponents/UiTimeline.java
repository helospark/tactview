package com.helospark.tactview.ui.javafx.uicomponents;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

@Component
public class UiTimeline {

    private TimeLineZoomCallback timeLineZoomCallback;
    private TimelineDragAndDropHandler timelineDragAndDropHandler;

    private Line positionIndicatorLine;

    private ScrollPane timeLineScrollPane;

    public UiTimeline(TimelineDragAndDropHandler timelineDragAndDropHandler,
            TimeLineZoomCallback timeLineZoomCallback) {
        this.timelineDragAndDropHandler = timelineDragAndDropHandler;
        this.timeLineZoomCallback = timeLineZoomCallback;
    }

    @PostConstruct
    public void asd() {
        timeLineScrollPane = new ScrollPane();
        Group timelineGroup = new Group();
        VBox timelineBoxes = new VBox();
        timelineBoxes.setPrefWidth(2000);
        timelineGroup.getChildren().add(timelineBoxes);

        positionIndicatorLine = new Line();
        positionIndicatorLine.setStartY(0);
        positionIndicatorLine.endYProperty().bind(timelineBoxes.heightProperty());
        positionIndicatorLine.setStartX(0);
        positionIndicatorLine.setEndX(0);
        positionIndicatorLine.setId("timeline-position-line");
        timelineGroup.getChildren().add(positionIndicatorLine);

        for (int i = 0; i < 10; ++i) {
            HBox timeline = new HBox();
            timeline.setPrefHeight(50);
            timeline.setMinHeight(50);
            timeline.getStyleClass().add("timelinerow");
            timeline.setPrefWidth(2000);
            timeline.setMinWidth(2000);

            VBox timelineTitle = new VBox();
            timelineTitle.getChildren().add(new Label("Video line 1"));
            timelineTitle.setMaxWidth(200);
            timelineTitle.setPrefHeight(50);
            timelineTitle.getStyleClass().add("timeline-title");
            timeline.getChildren().add(timelineTitle);

            Pane timelineRow = new Pane();
            timelineRow.minWidth(2000);
            timelineRow.minHeight(50);
            timelineRow.getStyleClass().add("timeline-clips");
            timeline.getChildren().add(timelineRow);

            final int index = i;

            timelineDragAndDropHandler.addDragAndDrop(timeline, timelineRow, index, timeLineZoomCallback.getScale());

            timelineBoxes.getChildren().add(timeline);
        }
        timelineBoxes.setOnScroll(timeLineZoomCallback::onScroll);

        timeLineScrollPane.setContent(timelineGroup);
    }

    public Node getTimelineNode() {
        return timeLineScrollPane;
    }

    public void updateLine(TimelinePosition position) {
        int pixel = position.getSeconds().multiply(TimelineDragAndDropHandler.PIXEL_PER_SECOND).intValue();
        positionIndicatorLine.setStartX(pixel);
        positionIndicatorLine.setEndX(pixel);
    }

}

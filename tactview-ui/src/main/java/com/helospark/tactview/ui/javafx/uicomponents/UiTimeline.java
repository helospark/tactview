package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;

import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

@Component
public class UiTimeline {

    private TimeLineZoomCallback timeLineZoomCallback;
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManager timelineManager;

    private Line positionIndicatorLine;

    private ScrollPane timeLineScrollPane;

    public UiTimeline(TimeLineZoomCallback timeLineZoomCallback, MessagingService messagingService,
            TimelineState timelineState, UiCommandInterpreterService commandInterpreter,
            TimelineManager timelineManager) {
        this.timeLineZoomCallback = timeLineZoomCallback;
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
    }

    public Node createTimeline() {
        BorderPane borderPane = new BorderPane();

        Button addChannelButton = new Button("Channel", new Glyph("FontAwesome", FontAwesome.Glyph.PLUS));
        addChannelButton.setOnMouseClicked(event -> {
            commandInterpreter.sendWithResult(new CreateChannelCommand(timelineManager, LAST_INDEX));
        });

        HBox titleBarTop = new HBox();
        titleBarTop.getChildren().add(addChannelButton);

        borderPane.setTop(titleBarTop);

        timeLineScrollPane = new ScrollPane();
        Group timelineGroup = new Group();
        VBox timelineBoxes = new VBox();
        timelineBoxes.setPrefWidth(2000);
        timelineGroup.getChildren().add(timelineBoxes);

        positionIndicatorLine = new Line();
        positionIndicatorLine.setStartY(0);
        positionIndicatorLine.endYProperty().bind(timelineBoxes.heightProperty());
        positionIndicatorLine.startXProperty().bind(timelineState.getLinePosition());
        positionIndicatorLine.endXProperty().bind(timelineState.getLinePosition());
        positionIndicatorLine.setId("timeline-position-line");
        timelineGroup.getChildren().add(positionIndicatorLine);

        Bindings.bindContentBidirectional(timelineState.getChannelsAsNodes(), timelineBoxes.getChildren());

        timelineBoxes.setOnScroll(timeLineZoomCallback::onScroll);

        timeLineScrollPane.setContent(timelineGroup);

        borderPane.setCenter(timeLineScrollPane);
        return borderPane;
    }

    public Node getTimelineNode() {
        return timeLineScrollPane;
    }

    public void updateLine(TimelinePosition position) {
        timelineState.setLinePosition(position);
    }

}

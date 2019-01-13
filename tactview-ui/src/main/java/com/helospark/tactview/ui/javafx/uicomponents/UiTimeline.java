package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.UiCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

@Component
public class UiTimeline {

    private TimeLineZoomCallback timeLineZoomCallback;
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManager timelineManager;
    private UiTimelineManager uiTimelineManager;
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;

    private Line positionIndicatorLine;

    private ScrollPane timeLineScrollPane;
    private VBox timelineTitlesPane;
    private BorderPane borderPane;
    private Canvas timelineLabelCanvas;

    public UiTimeline(TimeLineZoomCallback timeLineZoomCallback, MessagingService messagingService,
            TimelineState timelineState, UiCommandInterpreterService commandInterpreter,
            TimelineManager timelineManager, UiTimelineManager uiTimelineManager, UiPlaybackPreferenceRepository playbackPreferenceRepository) {
        this.timeLineZoomCallback = timeLineZoomCallback;
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.uiTimelineManager = uiTimelineManager;
        this.playbackPreferenceRepository = playbackPreferenceRepository;
    }

    public Node createTimeline(VBox lower, BorderPane root) {
        borderPane = new BorderPane();

        Button addChannelButton = new Button("Channel", new Glyph("FontAwesome", FontAwesome.Glyph.PLUS));
        addChannelButton.setTooltip(new Tooltip("Add new channel"));
        addChannelButton.setOnMouseClicked(event -> {
            commandInterpreter.sendWithResult(new CreateChannelCommand(timelineManager, LAST_INDEX));
        });
        Button cutAllClipsButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.CUT));
        cutAllClipsButton.setTooltip(new Tooltip("Cut all clips at cursor position"));

        cutAllClipsButton.setOnMouseClicked(event -> {
            TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();
            List<String> intersectingClips = timelineManager.findIntersectingClips(currentPosition);

            List<CutClipCommand> clipsToCut = intersectingClips.stream()
                    .map(clipId -> {
                        return CutClipCommand.builder()
                                .withClipId(clipId)
                                .withGlobalTimelinePosition(currentPosition)
                                .withTimelineManager(timelineManager)
                                .build();
                    })
                    .collect(Collectors.toList());

            if (clipsToCut.size() > 0) {
                commandInterpreter.sendWithResult(new CompositeCommand(clipsToCut.toArray(new UiCommand[0])));
            }
        });

        HBox titleBarTop = new HBox();
        titleBarTop.getChildren().addAll(addChannelButton, cutAllClipsButton);

        HBox timelineTimeLabels = new HBox();

        timelineLabelCanvas = new Canvas(200, 20);
        timelineLabelCanvas.widthProperty().bind(root.widthProperty());
        timelineLabelCanvas.widthProperty().addListener(newValue -> updateTimelineLabels());
        timelineTimeLabels.prefWidthProperty().bind(lower.widthProperty());

        timelineTimeLabels.getChildren().add(timelineLabelCanvas);

        VBox timelineTopRow = new VBox();
        timelineTopRow.getChildren().add(titleBarTop);
        timelineTopRow.getChildren().add(timelineTimeLabels);

        borderPane.setTop(timelineTopRow);

        timeLineScrollPane = new ScrollPane();
        GridPane gridPane = new GridPane();

        Group timelineGroup = new Group();
        Group zoomGroup = new Group();
        VBox timelineBoxes = new VBox();
        timelineBoxes.setPrefWidth(2000);
        timelineBoxes.setPadding(new Insets(0, 0, 0, -6));
        zoomGroup.getChildren().add(timelineBoxes);

        positionIndicatorLine = new Line();
        //        positionIndicatorLine.setTranslateX(6.0); // TODO: Layout need to be fixed
        positionIndicatorLine.setStartY(0);
        positionIndicatorLine.endYProperty().bind(timelineBoxes.heightProperty());
        positionIndicatorLine.startXProperty().bind(timelineState.getLinePosition());
        positionIndicatorLine.endXProperty().bind(timelineState.getLinePosition());
        positionIndicatorLine.setId("timeline-position-line");
        zoomGroup.getChildren().add(positionIndicatorLine);

        Line specialPositionLine = new Line();
        //        specialPositionLine.setTranslateX(6.0); // TODO: Layout need to be fixed
        specialPositionLine.layoutXProperty().bind(timelineState.getMoveSpecialPointLineProperties().getStartX());
        specialPositionLine.startYProperty().bind(timelineState.getMoveSpecialPointLineProperties().getStartY());
        specialPositionLine.visibleProperty().bind(timelineState.getMoveSpecialPointLineProperties().getEnabledProperty());
        specialPositionLine.endYProperty().bind(timelineState.getMoveSpecialPointLineProperties().getEndY());
        specialPositionLine.setId("special-position-line");
        zoomGroup.getChildren().add(specialPositionLine);

        Bindings.bindContentBidirectional(timelineState.getChannelsAsNodes(), timelineBoxes.getChildren());

        timelineTitlesPane = new VBox();
        timelineTitlesPane.getStyleClass().add("timeline-titles-pane");
        ScrollPane timelineTitlesScrollPane = new ScrollPane();
        timelineTitlesScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        timelineTitlesScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        timelineTitlesScrollPane.vvalueProperty().bind(timeLineScrollPane.vvalueProperty());
        VBox timelineTitles = new VBox();
        Bindings.bindContentBidirectional(timelineState.getChannelTitlesAsNodes(), timelineTitles.getChildren());
        timelineTitlesScrollPane.setContent(timelineTitles);
        timelineTitlesPane.getChildren().add(timelineTitlesScrollPane);

        timelineGroup.getChildren().add(zoomGroup);

        timelineState.onShownLocationChange(() -> updateTimelineLabels());

        zoomGroup.addEventFilter(ScrollEvent.SCROLL, e -> {
            timeLineZoomCallback.onScroll(e, timeLineScrollPane);
            e.consume();
        });
        timeLineScrollPane.hvalueProperty().addListener((o, oldValue, newValue) -> {
            Bounds viewportBounds = timeLineScrollPane.getViewportBounds();
            Bounds contentBounds = timeLineScrollPane.getContent().getBoundsInLocal();

            double hRel = timeLineScrollPane.getHvalue() / timeLineScrollPane.getHmax();
            double translate = Math.max(0, (contentBounds.getWidth() - viewportBounds.getWidth()) * hRel);
            timelineState.setTranslate(translate);
        });

        timelineTimeLabels.setOnMouseClicked(e -> {
            double xPosition = e.getX() - timelineTitles.getWidth();
            jumpTo(xPosition);
        });
        timelineTimeLabels.setOnMouseDragged(e -> {
            double xPosition = e.getX() - timelineTitles.getWidth();
            jumpTo(xPosition);
        });

        timeLineScrollPane.setContent(timelineGroup);
        timeLineScrollPane.prefHeightProperty().bind(borderPane.heightProperty());

        gridPane.add(timelineTitlesPane, 0, 0);
        gridPane.add(timeLineScrollPane, 1, 0);
        //        gridPane.setPrefHeight(500);

        borderPane.setCenter(gridPane);

        return borderPane;
    }

    private void updateTimelineLabels() {
        Platform.runLater(() -> {
            GraphicsContext g = timelineLabelCanvas.getGraphicsContext2D();
            int width = (int) timelineLabelCanvas.getWidth();
            int height = (int) timelineLabelCanvas.getHeight();
            g.clearRect(0, 0, width, height);
            drawLines(0.1, 17, 1.0);
            drawLines(0.5, 12, 0.5);
            drawLines(1.0, 7, 0.8);
            drawLines(10.0, 3, 1.5);
            drawLines(60.0, 0, 3.0);
        });
    }

    private void drawLines(double distance, int lineStart, double lineWidth) {
        int startPosition = (int) timelineTitlesPane.getWidth() + 4;
        if (startPosition < 0) {
            startPosition = 0;
        }
        int width = (int) timelineLabelCanvas.getWidth();
        int height = (int) timelineLabelCanvas.getHeight();
        GraphicsContext g = timelineLabelCanvas.getGraphicsContext2D();
        double secondLength = timelineState.secondsToPixelsWithZoom(new TimelineLength(BigDecimal.valueOf(distance)));

        TimelinePosition startTime = timelineState.getTimeAtLeftSide();
        if (secondLength > 3) {
            double firstSecond = secondLength - (startTime.divide(BigDecimal.valueOf(distance)).decimalPart()
                    .multiply(TimelineState.PIXEL_PER_SECOND)
                    .multiply(BigDecimal.valueOf(distance))
                    .getSeconds()
                    .doubleValue());

            g.setStroke(Color.BLACK);
            g.setLineWidth(lineWidth);
            for (double i = startPosition + firstSecond; i <= width; i += secondLength) {
                g.strokeLine(i, lineStart, i, height);
            }
        }
    }

    private void jumpTo(double xPosition) {
        TimelinePosition position = timelineState.pixelsToSeconds(xPosition).divide(BigDecimal.valueOf(timelineState.getZoom())).add(timelineState.getTimeAtLeftSide());
        uiTimelineManager.jumpAbsolute(position.getSeconds());
    }

    public void updateLine(TimelinePosition position) {
        timelineState.setLinePosition(position);
    }

}

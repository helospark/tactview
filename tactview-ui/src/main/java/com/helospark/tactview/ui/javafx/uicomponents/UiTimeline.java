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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

@Component
public class UiTimeline {

    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManager timelineManager;
    private UiTimelineManager uiTimelineManager;
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;

    private Line positionIndicatorLine;

    private ZoomableScrollPane timeLineScrollPane;
    private VBox timelineTitlesPane;
    private BorderPane borderPane;
    private Canvas timelineLabelCanvas;

    public UiTimeline(MessagingService messagingService,
            TimelineState timelineState, UiCommandInterpreterService commandInterpreter,
            TimelineManager timelineManager, UiTimelineManager uiTimelineManager, UiPlaybackPreferenceRepository playbackPreferenceRepository) {
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

        VBox timelineTopRow = new VBox();
        timelineTopRow.getChildren().add(titleBarTop);

        borderPane.setTop(timelineTopRow);

        GridPane gridPane = new GridPane();

        //        Group timelineGroup = new Group();
        Group zoomGroup = new Group();
        timeLineScrollPane = new ZoomableScrollPane(zoomGroup, timelineState);
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
        timelineTitlesScrollPane.vvalueProperty().bindBidirectional(timeLineScrollPane.vvalueProperty());
        VBox timelineTitles = new VBox();
        Bindings.bindContentBidirectional(timelineState.getChannelTitlesAsNodes(), timelineTitles.getChildren());
        timelineTitlesScrollPane.setContent(timelineTitles);
        timelineTitlesPane.getChildren().add(timelineTitlesScrollPane);

        //        timelineGroup.getChildren().add(zoomGroup);
        ScrollPane timelineTimeLabelsScrollPane = new ScrollPane();
        timelineTimeLabelsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        timelineTimeLabelsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        Group timelineCanvasGroup = new Group();
        timelineLabelCanvas = new Canvas(200, 35);
        timelineLabelCanvas.widthProperty().bind(timelineBoxes.widthProperty().multiply(timeLineScrollPane.zoomProperty()));

        timelineLabelCanvas.widthProperty().addListener(newValue -> updateTimelineLabels());
        //        timelineLabelCanvas.scaleXProperty().addListener(newValue -> updateTimelineLabels());

        timelineCanvasGroup.getChildren().add(timelineLabelCanvas);

        //        timelineTimeLabelsScrollPane.prefWidthProperty().bind(timeLineScrollPane.widthProperty());
        timelineTimeLabelsScrollPane.setContent(timelineCanvasGroup);
        timelineTimeLabelsScrollPane.hvalueProperty().bind(timeLineScrollPane.hvalueProperty());
        timelineTimeLabelsScrollPane.setFitToHeight(true);
        timelineTimeLabelsScrollPane.setFitToWidth(true);

        Region timelineTitlesSpacingPane = new Region();
        timelineTitlesSpacingPane.prefWidthProperty().set(timelineState.getChannelTitlesWidth());
        timelineTitlesSpacingPane.minWidthProperty().set(timelineState.getChannelTitlesWidth());
        timelineTitlesSpacingPane.maxWidthProperty().set(timelineState.getChannelTitlesWidth());

        HBox timelineLabelsTopHbox = new HBox();
        timelineLabelsTopHbox.getChildren().add(timelineTitlesSpacingPane);
        timelineLabelsTopHbox.getChildren().add(timelineTimeLabelsScrollPane);

        timelineTopRow.getChildren().add(timelineLabelsTopHbox);

        //        timelineState.onShownLocationChange(() -> updateTimelineLabels());

        //        zoomGroup.addEventFilter(ScrollEvent.SCROLL, e -> {
        //            timeLineZoomCallback.onScroll(e, timeLineScrollPane);
        //            e.consume();
        //        });
        timeLineScrollPane.hvalueProperty().addListener((o, oldValue, newValue) -> {
            Bounds viewportBounds = timeLineScrollPane.getViewportBounds();
            Bounds contentBounds = timeLineScrollPane.getContent().getBoundsInLocal();

            double hRel = timeLineScrollPane.getHvalue() / timeLineScrollPane.getHmax();
            double translate = Math.max(0, (contentBounds.getWidth() - viewportBounds.getWidth()) * hRel);
            timelineState.setTranslate(translate);
        });

        timelineTimeLabelsScrollPane.setOnMouseClicked(e -> {
            double xPosition = e.getX();
            jumpTo(xPosition);
        });
        timelineTimeLabelsScrollPane.setOnMouseDragged(e -> {
            double xPosition = e.getX();
            jumpTo(xPosition);
        });

        //        timeLineScrollPane.setContent(timelineGroup);
        timeLineScrollPane.prefHeightProperty().bind(borderPane.heightProperty());

        gridPane.add(timelineTitlesPane, 0, 0);
        gridPane.add(timeLineScrollPane, 1, 0);
        //        gridPane.setPrefHeight(500);

        borderPane.setCenter(gridPane);

        return borderPane;
    }

    private void updateTimelineLabels() {
        Platform.runLater(() -> {
            System.out.println("Canvas width: " + timelineLabelCanvas.getWidth());
            GraphicsContext g = timelineLabelCanvas.getGraphicsContext2D();

            int width = (int) timelineLabelCanvas.getWidth();
            int height = (int) timelineLabelCanvas.getHeight();
            g.clearRect(0, 0, width, height);
            drawLines(0.1, 29, 1.0, false);
            drawLines(0.5, 27, 0.5, false);
            drawLines(1.0, 24, 0.8, false);
            drawLines(10.0, 20, 1.5, true);
            drawLines(60.0, 16, 3.0, false);
        });
    }

    private void drawLines(double time, int lineStart, double lineWidth, boolean addLabel) {
        int startPosition = 4;
        int width = (int) timelineLabelCanvas.getWidth();
        int height = (int) timelineLabelCanvas.getHeight();
        GraphicsContext g = timelineLabelCanvas.getGraphicsContext2D();
        double secondLength = timelineState.secondsToPixelsWithZoom(new TimelineLength(BigDecimal.valueOf(time)));

        if (secondLength > 3) {
            g.setStroke(Color.BLACK);
            g.setLineWidth(lineWidth);
            for (double i = startPosition, j = 0; i <= width; i += secondLength, j += time) {
                g.strokeLine(i, lineStart, i, height);
                if (addLabel && secondLength > 30) {
                    String text = formatSeconds(j);
                    double textWidth = computeTextWidth(g.getFont(), text, 0);
                    g.fillText(text, i - (textWidth / 2), lineStart - 7);
                }
            }
        }
    }

    private String formatSeconds(double j) {
        int allSeconds = (int) j;

        int hours = allSeconds / (60 * 60);
        int minutes = (allSeconds % 3600) / 60;
        int seconds = (allSeconds % 60);

        String result = "";

        if (hours > 0) {
            result += hours;
        }
        if (minutes > 0) {
            if (!result.isEmpty()) {
                result += ":";
                result += String.format("%02d", minutes);
            } else {
                result += String.format("%d", minutes);
            }
        }

        if (!result.isEmpty()) {
            result += ":";
            result += String.format("%02d", seconds);
        } else {
            result += String.format("%d", seconds);
        }

        return result;
    }

    private double computeTextWidth(Font font, String text, double wrappingWidth) {
        Text helper = new Text();
        helper.setFont(font);
        helper.setText(text);
        // Note that the wrapping width needs to be set to zero before
        // getting the text's real preferred width.
        helper.setWrappingWidth(0);
        helper.setLineSpacing(0);
        double w = Math.min(helper.prefWidth(-1), wrappingWidth);
        helper.setWrappingWidth((int) Math.ceil(w));
        double textWidth = Math.ceil(helper.getLayoutBounds().getWidth());
        return textWidth;
    }

    private void jumpTo(double xPosition) {
        TimelinePosition position = timelineState.pixelsToSeconds(xPosition).divide(BigDecimal.valueOf(timelineState.getZoom())).add(timelineState.getTimeAtLeftSide());
        uiTimelineManager.jumpAbsolute(position.getSeconds());
    }

    public void updateLine(TimelinePosition position) {
        timelineState.setLinePosition(position);
    }

}

package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

@Component
public class UiTimeline {

    private static final int MAX_CANVAS_WIDTH = 16000;
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private UiTimelineManager uiTimelineManager;
    private LinkClipRepository linkClipRepository;
    private ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter;

    private Line positionIndicatorLine;

    private ZoomableScrollPane timeLineScrollPane;
    private VBox timelineTitlesPane;
    private BorderPane borderPane;
    private Rectangle timelineLabelCanvas;

    private Rectangle rectangle;

    public UiTimeline(MessagingService messagingService,
            TimelineState timelineState, UiCommandInterpreterService commandInterpreter,
            TimelineManagerAccessor timelineManager, UiTimelineManager uiTimelineManager,
            LinkClipRepository linkClipRepository, ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter) {
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.uiTimelineManager = uiTimelineManager;
        this.linkClipRepository = linkClipRepository;
        this.byteBufferToJavaFxImageConverter = byteBufferToJavaFxImageConverter;
    }

    public BorderPane createTimeline(VBox lower, BorderPane root) {
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

            if (intersectingClips.size() > 0) {
                CutClipCommand command = CutClipCommand.builder()
                        .withClipIds(intersectingClips)
                        .withGlobalTimelinePosition(currentPosition)
                        .withLinkedClipRepository(linkClipRepository)
                        .withTimelineManager(timelineManager)
                        .build();
                commandInterpreter.sendWithResult(command);
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
        timeLineScrollPane = new ZoomableScrollPane(zoomGroup, timelineState, uiTimelineManager);
        timelineState.setTimeLineScrollPane(timeLineScrollPane);
        VBox timelineBoxes = new VBox();
        timelineBoxes.prefWidthProperty().bind(timelineState.getTimelineWidthProperty());
        timelineBoxes.setPadding(new Insets(0, 0, 0, -6));
        zoomGroup.getChildren().add(timelineBoxes);

        positionIndicatorLine = new Line();
        //        positionIndicatorLine.setTranslateX(6.0); // TODO: Layout need to be fixed
        positionIndicatorLine.setStartY(0);
        positionIndicatorLine.endYProperty().bind(timelineBoxes.heightProperty());
        positionIndicatorLine.startXProperty().bind(timelineState.getLinePosition());
        positionIndicatorLine.endXProperty().bind(timelineState.getLinePosition());
        positionIndicatorLine.setId("timeline-position-line");
        positionIndicatorLine.setStrokeWidth(1.0);
        positionIndicatorLine.scaleXProperty().bind(new SimpleDoubleProperty(1.0).divide(timelineState.getZoomValue()));
        zoomGroup.getChildren().add(positionIndicatorLine);

        rectangle = new Rectangle();
        rectangle.setVisible(false);
        rectangle.setFill(new Color(0.0, 0.0, 1.0, 0.2));
        rectangle.xProperty().set(200);
        rectangle.yProperty().set(20);
        rectangle.widthProperty().set(300);
        rectangle.heightProperty().set(100);
        zoomGroup.getChildren().add(rectangle);

        Line specialPositionLine = new Line();
        //        specialPositionLine.setTranslateX(6.0); // TODO: Layout need to be fixed
        specialPositionLine.layoutXProperty().bind(timelineState.getMoveSpecialPointLineProperties().getStartX());
        specialPositionLine.startYProperty().bind(timelineState.getMoveSpecialPointLineProperties().getStartY());
        specialPositionLine.visibleProperty().bind(timelineState.getMoveSpecialPointLineProperties().getEnabledProperty());
        specialPositionLine.endYProperty().bind(timelineState.getMoveSpecialPointLineProperties().getEndY());
        specialPositionLine.setId("special-position-line");
        specialPositionLine.scaleXProperty().bind(new SimpleDoubleProperty(1.0).divide(timelineState.getZoomValue()));
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

        Group timelineCanvasGroup = new Group();
        timelineLabelCanvas = new Rectangle(200, 35);
        timelineLabelCanvas.widthProperty().bind(timelineBoxes.widthProperty());
        timelineLabelCanvas.scaleXProperty().bind(timeLineScrollPane.zoomProperty());

        timelineBoxes.widthProperty()
                .addListener((e, oldValue, newValue) -> updateTimelineLabels());
        timeLineScrollPane.zoomProperty()
                .addListener((e, oldValue, newValue) -> updateTimelineLabels());

        //        timelineLabelCanvas.widthProperty()
        //                .bind(timelineBoxes.widthProperty().multiply(timeLineScrollPane.zoomProperty()));
        timeLineScrollPane.hvalueProperty().bindBidirectional(timelineState.getHscroll());
        timeLineScrollPane.vvalueProperty().bindBidirectional(timelineState.getVscroll());

        //        timelineLabelCanvas.widthProperty().addListener(newValue ->
        //
        //        updateTimelineLabels());
        //        timelineLabelCanvas.scaleXProperty().addListener(newValue -> updateTimelineLabels());

        timelineCanvasGroup.getChildren().add(timelineLabelCanvas);

        //        timelineTimeLabelsScrollPane.prefWidthProperty().bind(timeLineScrollPane.widthProperty());
        timelineTimeLabelsScrollPane.setContent(timelineCanvasGroup);
        timelineTimeLabelsScrollPane.hvalueProperty().bind(timeLineScrollPane.hvalueProperty());
        timelineTimeLabelsScrollPane.prefWidthProperty().bind(timeLineScrollPane.widthProperty());
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

        timelineTimeLabelsScrollPane.addEventFilter(MouseEvent.ANY, e -> {
            if (e.isPrimaryButtonDown()) {
                double xPosition = e.getX();
                jumpTo(xPosition);
            }
        });

        //        timeLineScrollPane.setContent(timelineGroup);
        timeLineScrollPane.prefHeightProperty().bind(borderPane.heightProperty());
        timeLineScrollPane.prefWidthProperty().bind(root.widthProperty());

        gridPane.add(timelineTitlesPane, 0, 0);
        gridPane.add(timeLineScrollPane, 1, 0);
        //        gridPane.setPrefHeight(500);

        borderPane.setCenter(gridPane);

        return borderPane;
    }

    private void updateTimelineLabels() {
        System.out.println("Canvas width: " + timelineLabelCanvas.getWidth());

        int width = (int) (timelineLabelCanvas.getWidth() * timelineState.getZoom());
        int height = (int) timelineLabelCanvas.getHeight();

        if (width >= 16000) {
            // this should be patched
            timelineLabelCanvas.setFill(Color.BLACK);
        } else {

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = (Graphics2D) result.getGraphics();

            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, width, height);
            g.setComposite(AlphaComposite.SrcOver);

            drawLines(0.1, 29, 1.0, false, g, width);
            drawLines(0.5, 28, 0.5, false, g, width);
            drawLines(1.0, 25, 0.8, false, g, width);
            drawLines(10.0, 23, 1.5, timelineState.getZoom() < 2.0 && timelineState.getZoom() > 1.0, g, width);
            drawLines(60.0, 21, 3.0, timelineState.getZoom() < 1.0 && timelineState.getZoom() > 0.1, g, width);
            drawLines(600.0, 19, 3.0, timelineState.getZoom() < 0.1 && timelineState.getZoom() > 0.01, g, width);
            drawLines(3600.0, 17, 5.0, timelineState.getZoom() < 0.01, g, width);
            Image texture = byteBufferToJavaFxImageConverter.convertToJavafxImage(result);

            Platform.runLater(() -> {
                timelineLabelCanvas.setFill(new ImagePattern(texture));
            });
        }
    }

    private void drawLines(double time, int lineStart, double lineWidth, boolean addLabel, Graphics2D g, int width) {
        int startPosition = 4;
        int height = (int) timelineLabelCanvas.getHeight();
        double secondLength = timelineState.secondsToPixelsWithZoom(new TimelineLength(BigDecimal.valueOf(time)));

        if (secondLength > 3) {
            g.setColor(java.awt.Color.BLACK);
            g.setStroke(new BasicStroke((float) lineWidth));
            for (double i = startPosition, j = 0; i <= width; i += secondLength, j += time) {
                g.draw(new Line2D.Double(i, lineStart, i, height));
                if (addLabel && secondLength > 30) {
                    String text = formatSeconds(j);
                    double textWidth = computeTextWidth(g, text);
                    g.drawString(text, (int) (i - (textWidth / 2)), lineStart - 7);
                }
            }
        }
    }

    private String formatSeconds(double j) {
        // https://stackoverflow.com/a/40487511/8258222
        return java.time.Duration.ofMillis((int) (j * 1000))
                .toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    private double computeTextWidth(Graphics g, String text) {
        return g.getFontMetrics().stringWidth(text);
    }

    private void jumpTo(double xPosition) {
        TimelinePosition position = timelineState.pixelsToSeconds(xPosition).divide(BigDecimal.valueOf(timelineState.getZoom())).add(timelineState.getTimeAtLeftSide());
        uiTimelineManager.jumpAbsolute(position.getSeconds());
    }

    public void updateLine(TimelinePosition position) {
        timelineState.setLinePosition(position);
    }

    public void updateSelectionBox(Point startPoint, Point endPoint) {
        rectangle.setVisible(true);

        double startX = Math.min(startPoint.x, endPoint.x);
        double endX = Math.max(startPoint.x, endPoint.x);

        double startY = Math.min(startPoint.y, endPoint.y);
        double endY = Math.max(startPoint.y, endPoint.y);

        rectangle.xProperty().set(startX);
        rectangle.yProperty().set(startY);
        rectangle.widthProperty().set(endX - startX);
        rectangle.heightProperty().set(endY - startY);
    }

    public void selectionBoxEnded() {
        rectangle.setVisible(false);
    }

    public Rectangle getSelectionRectangle() {
        return rectangle;
    }

}

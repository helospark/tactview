package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.Optional;

import com.helospark.tactview.core.timeline.NonIntersectingIntervalList;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternChangedMessage;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternRepository;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class TimelineCanvas {
    public static final double TIMELINE_TIMESCALE_HEIGHT = 20;
    private static final double CHANNEL_PADDING = 4;
    private static final double MIN_CHANNEL_HEIGHT = 50;
    private static final double EFFECT_HEIGHT = 15;
    private Canvas canvas;
    private GraphicsContext graphics;
    private TimelineState timelineState;
    private TimelineManagerAccessor timelineAccessor;
    private TimelinePatternRepository timelinePatternRepository;

    private BorderPane resultPane;
    private VBox timelineTitlesPane;
    private ScrollBar rightBar;
    private ScrollBar bottomBar;

    public TimelineCanvas(VBox timelineTitlesPane, TimelineState timelineState, TimelineManagerAccessor timelineAccessor, MessagingService messagingService,
            TimelinePatternRepository timelinePatternRepository) {
        this.timelineAccessor = timelineAccessor;
        this.timelineState = timelineState;
        this.timelinePatternRepository = timelinePatternRepository;

        BorderPane centerPane = new BorderPane();
        centerPane.getStyleClass().add("timeline-pane");

        this.bottomBar = new ScrollBar();
        bottomBar.getStyleClass().add("timeline-bottom-scroll-bar");
        bottomBar.setMin(0);
        bottomBar.setMax(100.0);
        bottomBar.setUnitIncrement(100);
        bottomBar.setBlockIncrement(100);
        bottomBar.setVisibleAmount(500);
        bottomBar.setValue(0.0);
        bottomBar.valueProperty().addListener(e -> {
            timelineState.setTranslate(bottomBar.getValue());
        });

        this.rightBar = new ScrollBar();
        rightBar.setMin(0);
        rightBar.setMax(100.0);
        rightBar.setUnitIncrement(100);
        rightBar.setBlockIncrement(100);
        rightBar.setVisibleAmount(100);
        rightBar.setValue(0.0);

        rightBar.getStyleClass().add("timeline-right-scroll-bar");
        rightBar.setOrientation(Orientation.VERTICAL);
        this.canvas = new Canvas(600, 200);
        this.graphics = canvas.getGraphicsContext2D();
        Pane wrapperPane = new Pane();
        wrapperPane.getChildren().add(canvas);

        canvas.widthProperty().bind(wrapperPane.widthProperty());
        canvas.heightProperty().bind(wrapperPane.heightProperty());

        canvas.widthProperty().addListener(e -> redraw());
        canvas.heightProperty().addListener(e -> redraw());

        centerPane.setRight(rightBar);
        centerPane.setBottom(bottomBar);
        centerPane.setCenter(wrapperPane);

        resultPane = new BorderPane();
        resultPane.setLeft(timelineTitlesPane);
        resultPane.setCenter(centerPane);

        timelineState.subscribe(() -> this.redraw());

        messagingService.register(TimelinePatternChangedMessage.class, message -> {
            Optional<TimelineClip> clip = timelineAccessor.findClipById(message.getComponentId());
            Optional<Integer> channelIndex = timelineAccessor.findChannelIndexForClipId(message.getComponentId());

            if (clip.isPresent() && channelIndex.isPresent() && isVisible(clip.get().getGlobalInterval(), channelIndex.get())) {
                redraw();
            }
        });

        canvas.setOnScroll(e -> {
            if (e.isControlDown()) {
                onScroll(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
            } else {
                rightBar.setValue(rightBar.getValue() + e.getDeltaY() * 20.0);
            }
        });

        rightBar.valueProperty().addListener(e -> {
            double normalizedScroll = rightBar.getValue() / rightBar.getMax();
            timelineState.setNormalizedVScroll(normalizedScroll);
        });

    }

    private void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomIntensity = 0.02;
        double zoomFactor = Math.exp((wheelDelta * 0.05) * zoomIntensity);

        double scaleValue = timelineState.getZoom();

        double mousePointerTime = mapCanvasPixelToTime(mousePoint.getX());
        double newTime = mousePointerTime * zoomFactor;
        double translateSeconds = getTranslateSeconds();

        double newTranslate = translateSeconds + (newTime - mousePointerTime);

        if (newTranslate < 0) {
            newTranslate = 0;
        }
        setTranslateSeconds(newTranslate);

        scaleValue = scaleValue * zoomFactor;
        if (scaleValue < 0.005)
            scaleValue = 0.005;
        if (scaleValue > 20)
            scaleValue = 20;

        timelineState.setZoom(scaleValue);
    }

    private double getTranslateSeconds() {
        return timelineState.getTranslate().get() / (TimelineState.PIXEL_PER_SECOND.doubleValue() * timelineState.getZoom());
    }

    private void setTranslateSeconds(double seconds) {
        this.bottomBar.setValue(seconds * TimelineState.PIXEL_PER_SECOND.doubleValue() * timelineState.getZoom());
    }

    private boolean isVisible(TimelineInterval globalInterval, Integer channelIndex) {
        double visibleLeft = mapCanvasPixelToTime(0);
        double visibleRight = mapCanvasPixelToTime(canvas.getWidth());

        double intervalLeftPosition = globalInterval.getStartPosition().getSeconds().doubleValue();
        double intervalRightPosition = globalInterval.getEndPosition().getSeconds().doubleValue();

        boolean notVisible = (intervalRightPosition < visibleLeft || intervalLeftPosition > visibleRight);

        return !notVisible;
    }

    public void redraw() {
        Platform.runLater(() -> redrawInternal());
    }

    public void redrawInternal() {
        double timelineWidth = timelineState.getTimelineWidthProperty().get() * timelineState.getZoom();
        bottomBar.setMin(0);
        bottomBar.setMax(timelineWidth);

        clearCanvas();

        double scrolledY = timelineState.getVscroll().get();
        double fullHeight = rightBar.getMax() - canvas.getHeight() - CHANNEL_PADDING;
        double visibleAreaStartY = fullHeight * scrolledY;
        if (visibleAreaStartY < 0) {
            visibleAreaStartY = 0;
        }

        double channelStartY = TIMELINE_TIMESCALE_HEIGHT + CHANNEL_PADDING;

        for (int i = 0; i < timelineAccessor.getChannels().size(); ++i) {
            TimelineChannel currentChannel = timelineAccessor.getChannels().get(i);
            NonIntersectingIntervalList<TimelineClip> clips = currentChannel.getAllClips();

            double clipHeight = calculateHeight(currentChannel);

            if ((channelStartY + clipHeight) >= visibleAreaStartY && (channelStartY + clipHeight + CHANNEL_PADDING) < visibleAreaStartY + canvas.getHeight()) {
                for (var clip : clips) {
                    TimelineInterval interval = clip.getGlobalInterval();

                    double clipX = timelineState.secondsToPixelsWidthZoomAndTranslate(interval.getStartPosition());
                    double clipEndX = timelineState.secondsToPixelsWidthZoomAndTranslate(interval.getEndPosition());

                    Optional<Image> pattern = timelinePatternRepository.getPatternForClipId(clip.getId());
                    double clipWidth = clipEndX - clipX;
                    double clipY = channelStartY - visibleAreaStartY;
                    if (pattern.isPresent()) {
                        graphics.drawImage(pattern.get(), clipX, clipY, clipWidth, clipHeight);
                    } else {
                        graphics.setFill(Color.AQUA);
                        graphics.fillRect(clipX, clipY, clipWidth, clipHeight);
                    }
                }

                graphics.setStroke(Color.GRAY);
                graphics.strokeLine(0, channelStartY + clipHeight + CHANNEL_PADDING - visibleAreaStartY, canvas.getWidth(), channelStartY + clipHeight + CHANNEL_PADDING - visibleAreaStartY);
            }

            double rowHeight = clipHeight + CHANNEL_PADDING * 2;
            channelStartY += rowHeight;

            VBox channelTitleVBox = (VBox) timelineState.getChannelTitlesAsNodes().get(i);
            channelTitleVBox.setMinHeight(rowHeight);
            channelTitleVBox.setMaxHeight(rowHeight);
            channelTitleVBox.setPrefHeight(rowHeight);
        }

        rightBar.setMax(channelStartY);
        drawTimelineTitles();
        drawPlaybackLine();

    }

    private void drawTimelineTitles() {
        graphics.setFill(Color.BEIGE);
        graphics.fillRect(0, 0, canvas.getWidth(), TIMELINE_TIMESCALE_HEIGHT);
        graphics.setStroke(Color.BLACK);

        double left = mapCanvasPixelToTime(0);
        double right = mapCanvasPixelToTime(canvas.getWidth());

        int numberOfTenthSeconds = (int) ((right - left) * 10);
        int numberOfSeconds = (int) (right - left);
        int numberOfMinutes = (int) ((right - left) / 60);
        int numberOfHours = (int) ((right - left) / (60 * 60));

        if (numberOfTenthSeconds < 200) {
            drawLines(left, right, 3, 10);
        }
        if (numberOfSeconds < 200) {
            drawLines(left, right, 5, 1);
        }
        if (numberOfMinutes < 200) {
            drawLines(left, right, 10, 1 / 60.0);
        }
        if (numberOfHours < 200) {
            drawLines(left, right, 18, 1.0 / 3600.0);
        }
    }

    private void drawLines(double left, double right, double height, double divider) {
        int value = (int) (left * divider);
        int newRight = (int) (Math.ceil(right * divider));

        while (value <= newRight) {
            double pos = timelineState.secondsToPixelsWidthZoomAndTranslate(TimelinePosition.ofSeconds(value / divider));
            graphics.strokeLine(pos, TIMELINE_TIMESCALE_HEIGHT - height, pos, TIMELINE_TIMESCALE_HEIGHT);
            value += 1;
        }
    }

    private double mapCanvasPixelToTime(double position) {
        double translatedPosition = timelineState.getTranslate().get() + position;
        return timelineState.pixelsToSecondsWithZoom(translatedPosition).getSeconds().doubleValue();
    }

    private double calculateHeight(TimelineChannel currentChannel) {
        NonIntersectingIntervalList<TimelineClip> clips = currentChannel.getAllClips();
        double size = MIN_CHANNEL_HEIGHT;
        for (var clip : clips) {
            double currentClipHeight = MIN_CHANNEL_HEIGHT + clip.getEffects().size() * EFFECT_HEIGHT;
            if (currentClipHeight > size) {
                size = currentClipHeight;
            }
        }
        return size;
    }

    private void clearCanvas() {
        graphics.setFill(Color.BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawPlaybackLine() {
        double x = timelineState.getLinePosition().doubleValue();
        graphics.setStroke(Color.YELLOW);
        graphics.strokeLine(x, 0, x, canvas.getHeight());
    }

    public BorderPane getResultPane() {
        return resultPane;
    }

}

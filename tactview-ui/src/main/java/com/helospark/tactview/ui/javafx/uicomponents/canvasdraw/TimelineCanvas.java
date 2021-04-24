package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.NonIntersectingIntervalList;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.DragRepository.DragDirection;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.drag.ClipDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragAdder;
import com.helospark.tactview.ui.javafx.uicomponents.EffectDragInformation;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineDragAndDropHandler;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.CollisionRectangle;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternChangedMessage;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternRepository;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

@Component
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
    private MessagingService messagingService;
    private DragRepository dragRepository;
    private TimelineDragAndDropHandler timelineDragAndDropHandler;
    private SelectedNodeRepository selectedNodeRepository;
    private UiCommandInterpreterService commandInterpreter;
    private EffectDragAdder effectDragAdder;

    private BorderPane resultPane;
    private ScrollBar rightBar;
    private ScrollBar bottomBar;

    private boolean isLoadingInprogress = false;

    private List<TimelineUiCacheElement> cachedVisibleElements = new ArrayList<>();

    public TimelineCanvas(TimelineState timelineState, TimelineManagerAccessor timelineAccessor, MessagingService messagingService,
            TimelinePatternRepository timelinePatternRepository, DragRepository dragRepository, TimelineDragAndDropHandler timelineDragAndDropHandler,
            SelectedNodeRepository selectedNodeRepository, UiCommandInterpreterService commandInterpreter, EffectDragAdder effectDragAdder) {
        this.timelineAccessor = timelineAccessor;
        this.timelineState = timelineState;
        this.timelinePatternRepository = timelinePatternRepository;
        this.messagingService = messagingService;
        this.dragRepository = dragRepository;
        this.timelineDragAndDropHandler = timelineDragAndDropHandler;
        this.selectedNodeRepository = selectedNodeRepository;
        this.commandInterpreter = commandInterpreter;
        this.effectDragAdder = effectDragAdder;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipAddedMessage.class, message -> {
            redrawClipIfVisible(message.getClipId());
        });
        messagingService.register(ClipMovedMessage.class, message -> {
            redrawClipIfVisible(message.getClipId());
        });
        messagingService.register(ClipResizedMessage.class, message -> {
            redrawClipIfVisible(message.getClipId());
        });
        messagingService.register(EffectAddedMessage.class, message -> {
            redrawClipIfVisible(message.getClipId());
        });
        messagingService.register(EffectMovedMessage.class, message -> {
            redrawClipIfVisible(message.getOriginalClipId());
        });
    }

    private void redrawClipIfVisible(String clipId) {
        Integer channel = timelineAccessor.findChannelIndexForClipId(clipId).get();
        TimelineClip clip = timelineAccessor.findClipById(clipId).get();
        if (isVisible(clip.getGlobalInterval(), channel)) {
            redraw();
        }
    }

    public BorderPane create(VBox timelineTitlesPane) {
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
                onZoom(e.getDeltaY(), new Point2D(e.getX(), e.getY()));
            } else {
                onScrollVertically(e);
            }
        });

        rightBar.valueProperty().addListener(e -> {
            double normalizedScroll = rightBar.getValue() / rightBar.getMax();
            timelineState.setNormalizedVScroll(normalizedScroll);
        });

        canvas.setOnMouseMoved(event -> {
            onMouseMoved(event.getX(), event.getY());
        });

        canvas.setOnMousePressed(event -> {
            double currentX = mapCanvasPixelToTime(event.getX());
            Optional<TimelineUiCacheElement> optionalElement = findElementAt(event.getX(), event.getY());

            if (event.isPrimaryButtonDown() && dragRepository.getInitialX() == -1 && optionalElement.isPresent()) {
                TimelineUiCacheElement element = optionalElement.get();
                boolean isResizing = isResizing(element, event.getX());
                if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
                    TimelineClip clip = timelineAccessor.findClipById(element.elementId).get();
                    String channelId = timelineAccessor.findChannelForClipId(element.elementId).get().getId();
                    double clipPositionAsDouble = clip.getGlobalInterval().getStartPosition().getSeconds().doubleValue();
                    ClipDragInformation clipDragInformation = new ClipDragInformation(clip.getGlobalInterval().getStartPosition(), element.elementId, channelId, currentX - clipPositionAsDouble);
                    if (isResizing) {
                        dragRepository.onClipResizing(clipDragInformation, isResizingLeft(element, event.getX()) ? DragDirection.LEFT : DragDirection.RIGHT);
                    } else {
                        dragRepository.onClipDragged(clipDragInformation);
                    }
                } else {
                    TimelineClip clip = timelineAccessor.findClipForEffect(element.elementId).get();
                    StatelessEffect effect = timelineAccessor.findEffectById(element.elementId).get();
                    EffectDragInformation effectDragInformation = new EffectDragInformation(clip.getId(), effect.getId(), effect.getGlobalInterval().getStartPosition(), currentX);
                    if (isResizing) {
                        dragRepository.onEffectDragged(effectDragInformation);
                    } else {
                        dragRepository.onEffectResized(effectDragInformation, isResizingLeft(element, event.getX()) ? DragDirection.LEFT : DragDirection.RIGHT);
                    }

                }
            }

        });

        canvas.setOnMouseDragOver(e -> {
            System.out.println("drag over");
        });

        canvas.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            boolean isPrimaryButtonDown = event.isPrimaryButtonDown();

            if (isPrimaryButtonDown) {
                onDrag(x, y);
            }
        });

        //        canvas.setOnDragOver(e -> {
        //            System.out.println("drag over 2");
        //        });

        canvas.setOnMouseReleased(event -> {
            System.out.println("clear dragging clip");
            dragRepository.clearClipDrag();
        });

        canvas.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasString()) {
                if (db.getString().startsWith("clip:")) {
                    onClipDraggedToCanvas(event, db);
                } else if (db.getString().startsWith("effect:")) {
                    onEffectDraggedToCanvas(event, db);
                }
            } else {
                onDrag(event.getX(), event.getY());
            }
        });

        return resultPane;
    }

    private void onEffectDraggedToCanvas(DragEvent event, Dragboard db) {
        Optional<TimelineUiCacheElement> element = findElementAt(event.getX(), event.getY());
        if (element.isPresent() && element.get().elementType.equals(TimelineUiCacheType.CLIP)) {
            TimelinePosition position = TimelinePosition.ofSeconds(mapCanvasPixelToTime(event.getX()));
            boolean result = effectDragAdder.addEffectDragOnClip(element.get().elementId, position, db);
            if (result) {
                db.clear();
            }
        }
    }

    private void onClipDraggedToCanvas(DragEvent event, Dragboard db) {
        Optional<TimelineChannel> optionalChannel = findChannelAtPosition(event.getX(), event.getY());
        if (optionalChannel.isPresent()) {
            List<File> dbFiles = db.getFiles();
            String dbString = db.getString();
            double currentX = event.getX();
            String channelId = optionalChannel.get().getId();
            AddClipRequest addClipRequest = timelineDragAndDropHandler.addClipRequest(channelId, dbFiles, dbString, currentX);
            if (!isLoadingInprogress && dragRepository.currentlyDraggedClip() == null && ((dbFiles != null && !dbFiles.isEmpty()) || timelineDragAndDropHandler.isStringClip(db))) {
                selectedNodeRepository.clearAllSelectedItems();
                isLoadingInprogress = true;

                try {
                    AddClipsCommand result = commandInterpreter.synchronousSend(new AddClipsCommand(addClipRequest, timelineAccessor));
                    String addedClipId = result.getAddedClipId();
                    System.out.println("Clip added " + addedClipId);
                    timelineState.findClipById(addedClipId).orElseThrow(() -> new RuntimeException("Not found"));
                    ClipDragInformation clipDragInformation = new ClipDragInformation(result.getRequestedPosition(), addedClipId, channelId, 0);
                    dragRepository.onClipDragged(clipDragInformation);
                    db.clear();
                } catch (Exception e1) {
                    System.out.println("Error while adding clip " + e1);
                } finally {
                    isLoadingInprogress = false;
                }
            }
        }
    }

    private void onDrag(double x, double y) {
        if ((dragRepository.currentEffectDragInformation() != null || dragRepository.currentlyDraggedClip() != null)) {
            if (dragRepository.currentlyDraggedClip() != null) {
                if (!dragRepository.isResizing()) {
                    TimelinePosition newX = TimelinePosition.ofSeconds(mapCanvasPixelToTime(x) - dragRepository.currentlyDraggedClip().getAnchorPointX());
                    String channelId = findChannelAtPosition(x, y)
                            .map(a -> a.getId())
                            .orElse(timelineAccessor.getChannels().get(0).getId());
                    timelineDragAndDropHandler.moveClip(channelId, false, newX);
                } else {
                    TimelinePosition newX = TimelinePosition.ofSeconds(mapCanvasPixelToTime(x));
                    timelineDragAndDropHandler.resizeClip(newX, false);
                }
            } else {
                if (!dragRepository.isResizing()) {
                    TimelinePosition newX = TimelinePosition.ofSeconds(mapCanvasPixelToTime(x) - dragRepository.currentEffectDragInformation().getAnchorPointX());
                    timelineDragAndDropHandler.resizeEffect(newX, false);
                } else {
                    TimelinePosition newX = TimelinePosition.ofSeconds(mapCanvasPixelToTime(x));
                    timelineDragAndDropHandler.moveEffect(newX, false);
                }
            }
        }
    }

    private boolean isResizing(TimelineUiCacheElement element, double x) {
        return isResizable(element) && (isResizingLeft(element, x) || isResizingRight(element, x));
    }

    private Optional<TimelineChannel> findChannelAtPosition(double x, double y) {
        double channelStartY = TIMELINE_TIMESCALE_HEIGHT + CHANNEL_PADDING;

        for (int i = 0; i < timelineAccessor.getChannels().size(); ++i) {
            TimelineChannel currentChannel = timelineAccessor.getChannels().get(i);

            double clipHeight = calculateHeight(currentChannel);

            if (y >= channelStartY && y <= channelStartY + clipHeight) {
                return Optional.of(currentChannel);
            }

            channelStartY += clipHeight + CHANNEL_PADDING * 2;
        }
        return Optional.empty();
    }

    private void onMouseMoved(double x, double y) {
        Optional<TimelineUiCacheElement> optionalElement = findElementAt(x, y);
        if (optionalElement.isPresent()) {
            var element = optionalElement.get();
            boolean isResizing = (isResizingLeft(element, x) || isResizingRight(element, x)) && isResizable(element);

            if (isResizing) {
                canvas.setCursor(Cursor.H_RESIZE);
            } else {
                canvas.setCursor(Cursor.HAND);
            }
        } else {
            canvas.setCursor(null);
        }

    }

    public boolean isResizingLeft(TimelineUiCacheElement element, double x) {
        return x - element.rectangle.topLeftX < 10;
    }

    public boolean isResizingRight(TimelineUiCacheElement element, double x) {
        return element.rectangle.topLeftX + element.rectangle.width - x < 10;
    }

    public Optional<TimelineUiCacheElement> findElementAt(double x, double y) {
        for (var element : cachedVisibleElements) {
            if (element.rectangle.containsPoint(x, y)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    private boolean isResizable(TimelineUiCacheElement element) {
        if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
            return timelineState.findClipById(element.elementId).map(a -> a.isResizable()).orElse(false);
        }
        if (element.elementType.equals(TimelineUiCacheType.EFFECT)) {
            return timelineState.findEffectById(element.elementId).map(a -> a.isResizable()).orElse(false);
        }
        return false;
    }

    private void onScrollVertically(ScrollEvent e) {
        double newValue = rightBar.getValue() - e.getDeltaY() * 0.7;
        if (newValue < 0.0) {
            newValue = 0.0;
        }
        if (newValue > rightBar.getMax()) {
            newValue = rightBar.getMax();
        }
        rightBar.setValue(newValue);
    }

    private void onZoom(double wheelDelta, Point2D mousePoint) {
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
        cachedVisibleElements.clear();

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

            if ((channelStartY + clipHeight) >= visibleAreaStartY && (channelStartY) <= visibleAreaStartY + canvas.getHeight()) {
                for (var clip : clips) {
                    TimelineInterval interval = clip.getGlobalInterval();

                    double clipX = timelineState.secondsToPixelsWidthZoomAndTranslate(interval.getStartPosition());
                    double clipEndX = timelineState.secondsToPixelsWidthZoomAndTranslate(interval.getEndPosition());

                    Optional<Image> pattern = timelinePatternRepository.getPatternForClipId(clip.getId());
                    double clipWidth = clipEndX - clipX;
                    double clipY = channelStartY - visibleAreaStartY;
                    if (pattern.isPresent()) {
                        graphics.drawImage(pattern.get(), clipX, clipY, clipWidth, MIN_CHANNEL_HEIGHT);
                    } else {
                        graphics.setFill(Color.AQUA);
                        graphics.fillRect(clipX, clipY, clipWidth, clipHeight);
                    }
                    cachedVisibleElements.add(new TimelineUiCacheElement(clip.getId(), TimelineUiCacheType.CLIP, new CollisionRectangle(clipX, clipY, clipWidth, clipHeight)));

                    for (int j = 0; j < clip.getEffectChannels().size(); ++j) {
                        for (var effect : clip.getEffectChannels().get(j)) {
                            graphics.setFill(Color.AQUAMARINE);
                            graphics.setStroke(Color.WHITE);

                            double effectX = timelineState.secondsToPixelsWidthZoomAndTranslate(effect.getGlobalInterval().getStartPosition());
                            double effectEndX = timelineState.secondsToPixelsWidthZoomAndTranslate(effect.getGlobalInterval().getEndPosition());
                            double effectY = clipY + MIN_CHANNEL_HEIGHT + EFFECT_HEIGHT * j;
                            double effectWidth = effectEndX - effectX;
                            double effectHeight = EFFECT_HEIGHT;

                            graphics.fillRoundRect(effectX, effectY, effectWidth, effectHeight, 4, 4);
                            //                            graphics.strokeText(effect.getId(), effectX, effectY, effectWidth);

                            cachedVisibleElements.add(new TimelineUiCacheElement(effect.getId(), TimelineUiCacheType.EFFECT, new CollisionRectangle(effectX, effectY, effectWidth, effectHeight)));
                        }
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

    static class TimelineUiCacheElement {
        String elementId;
        TimelineUiCacheType elementType;
        CollisionRectangle rectangle;

        public TimelineUiCacheElement(String elementId, TimelineUiCacheType elementType, CollisionRectangle rectangle) {
            this.elementId = elementId;
            this.elementType = elementType;
            this.rectangle = rectangle;
        }

    }

    static enum TimelineUiCacheType {
        CLIP,
        EFFECT
    }
}

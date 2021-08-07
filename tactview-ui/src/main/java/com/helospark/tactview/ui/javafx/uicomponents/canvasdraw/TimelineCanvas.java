package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.timeline.NonIntersectingIntervalList;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.marker.MarkerRepository;
import com.helospark.tactview.core.timeline.marker.MarkersChangedMessage;
import com.helospark.tactview.core.timeline.message.AbstractKeyframeChangedMessage;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipMovedMessage;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.ClipResizedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectChannelChangedMessage;
import com.helospark.tactview.core.timeline.message.EffectMovedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.core.util.messaging.EffectMovedToDifferentClipMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.repository.DragRepository;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.selection.ClipSelectionChangedMessage;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineLineProperties;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.CanvasRedrawRequest;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.ChannelHeightResponse;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.CollisionRectangle;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheElement;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.TimelineUiCacheType;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain.UiTimelineChangeType;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasClickHandler;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasClickHandler.TimelineCanvasClickHandlerRequest;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasContextMenuRequestedHandler;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasContextMenuRequestedHandler.TimelineCanvasContextMenuRequestedHandlerRequest;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasElementClickHandler;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasOnDragOverHandler;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasOnDragOverHandler.TimelineCanvasOnDragOverHandlerRequest;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasTimelineHeaderClickHandler;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.handlers.TimelineCanvasTimelineHeaderClickHandler.TimelineCanvasTimelineHeaderClickHandlerRequest;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.PatternIntervalAware;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternChangedMessage;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternRepository;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

@Component
public class TimelineCanvas implements ScenePostProcessor {
    public static final Logger LOGGER = LoggerFactory.getLogger(TimelineCanvas.class);

    public static final double DRAG_SCROLL_THRESHOLD = 25;
    public static final double TIMELINE_TIMESCALE_HEIGHT = 25;
    private static final double CHANNEL_PADDING = 4;
    private static final double MIN_CHANNEL_HEIGHT = 50;
    private static final double EFFECT_HEIGHT = 25;
    private Canvas canvas;
    private GraphicsContext graphics;
    private TimelineState timelineState;
    private TimelineManagerAccessor timelineAccessor;
    private TimelinePatternRepository timelinePatternRepository;
    private MessagingService messagingService;
    private DragRepository dragRepository;
    private SelectedNodeRepository selectedNodeRepository;
    private UiTimelineManager uiTimelineManager;
    private NameToIdRepository nameToIdRepository;
    private ScheduledExecutorService scheduledExecutorService;
    private MarkerRepository markerRepository;
    private TimelineCanvasClickHandler timelineCanvasClickHandler;
    private TimelineCanvasContextMenuRequestedHandler timelineCanvasContextMenuRequestedHandler;
    private TimelineCanvasOnDragOverHandler timelineCanvasOnDragOverHandler;
    private TimelineCanvasElementClickHandler timelineCanvasElementClickHandler;
    private TimelineCanvasTimelineHeaderClickHandler timelineCanvasTimelineHeaderClickHandler;

    private BorderPane resultPane;
    private ScrollBar rightBar;
    private ScrollBar bottomBar;
    private Scene scene;

    private CollisionRectangle selectionBox = null;
    private Tooltip tooltip = null;
    private TimelinePosition lastMarkerShownPosition;

    private List<TimelineUiCacheElement> cachedVisibleElements = new ArrayList<>();
    private Image previouslyCachedImage = null;

    private volatile AtomicReference<CanvasRedrawRequest> redrawRequest = new AtomicReference<>(null); // cache requests to avoid duplicate redraws

    public TimelineCanvas(TimelineState timelineState, TimelineManagerAccessor timelineAccessor, MessagingService messagingService,
            TimelinePatternRepository timelinePatternRepository, DragRepository dragRepository,
            SelectedNodeRepository selectedNodeRepository,
            UiTimelineManager uiTimelineManager, TimelineCanvasContextMenuRequestedHandler timelineCanvasContextMenuRequestedHandler,
            NameToIdRepository nameToIdRepository, @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,
            MarkerRepository markerRepository, TimelineCanvasClickHandler timelineCanvasClickHandler,
            TimelineCanvasOnDragOverHandler timelineCanvasOnDragOverHandler, TimelineCanvasElementClickHandler timelineCanvasElementClickHandler,
            TimelineCanvasTimelineHeaderClickHandler timelineCanvasTimelineHeaderClickHandler) {
        this.timelineAccessor = timelineAccessor;
        this.timelineState = timelineState;
        this.timelinePatternRepository = timelinePatternRepository;
        this.messagingService = messagingService;
        this.dragRepository = dragRepository;
        this.selectedNodeRepository = selectedNodeRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.nameToIdRepository = nameToIdRepository;
        this.scheduledExecutorService = scheduledExecutorService;
        this.markerRepository = markerRepository;
        this.timelineCanvasClickHandler = timelineCanvasClickHandler;
        this.timelineCanvasContextMenuRequestedHandler = timelineCanvasContextMenuRequestedHandler;
        this.timelineCanvasOnDragOverHandler = timelineCanvasOnDragOverHandler;
        this.timelineCanvasElementClickHandler = timelineCanvasElementClickHandler;
        this.timelineCanvasTimelineHeaderClickHandler = timelineCanvasTimelineHeaderClickHandler;
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
        messagingService.register(EffectResizedMessage.class, message -> {
            redrawClipIfVisible(message.getClipId());
        });
        messagingService.register(AbstractKeyframeChangedMessage.class, message -> {
            redraw(true);
        });
        messagingService.register(EffectChannelChangedMessage.class, message -> {
            redraw(true);
        });
        messagingService.register(EffectMovedToDifferentClipMessage.class, message -> {
            redraw(true);
        });
        messagingService.register(ClipRemovedMessage.class, message -> {
            redraw(true);
        });
        messagingService.register(EffectRemovedMessage.class, message -> {
            redraw(true);
        });
        messagingService.register(ClipSelectionChangedMessage.class, message -> {
            redraw(true);
        });
        messagingService.register(MarkersChangedMessage.class, message -> {
            redraw(false);
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            var oldValue = redrawRequest.getAndSet(null);

            if (oldValue != null) {
                Platform.runLater(() -> {
                    try {
                        redrawInternal(oldValue.fullRedraw);
                    } catch (Exception e) {
                        LOGGER.warn("Unable to redraw", e);
                    }
                });
            }

        }, 0, 40, TimeUnit.MILLISECONDS);
    }

    private void redrawClipIfVisible(String clipId) {
        Integer channel = timelineAccessor.findChannelIndexForClipId(clipId).get();
        TimelineClip clip = timelineAccessor.findClipById(clipId).get();
        if (isVisible(clip.getGlobalInterval(), channel)) {
            redraw(true);
        }
    }

    public BorderPane create(BorderPane timelineTitlesBorderPane) {
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
        rightBar.setVisibleAmount(200);
        rightBar.setValue(0.0);

        rightBar.getStyleClass().add("timeline-right-scroll-bar");
        rightBar.setOrientation(Orientation.VERTICAL);
        this.canvas = new Canvas(600, 200);
        this.graphics = canvas.getGraphicsContext2D();
        Pane wrapperPane = new Pane();
        wrapperPane.getChildren().add(canvas);

        canvas.widthProperty().bind(wrapperPane.widthProperty());
        canvas.heightProperty().bind(wrapperPane.heightProperty());

        canvas.widthProperty().addListener(e -> redraw(true));
        canvas.heightProperty().addListener(e -> redraw(true));

        centerPane.setRight(rightBar);
        centerPane.setBottom(bottomBar);
        centerPane.setCenter(wrapperPane);

        resultPane = new BorderPane();
        resultPane.setLeft(timelineTitlesBorderPane);
        resultPane.setCenter(centerPane);

        timelineState.subscribe(type -> {
            boolean fullRedraw = true;
            if (type.getType().equals(UiTimelineChangeType.TIMELINE_POSITION) || type.getType().equals(UiTimelineChangeType.SPECIAL_LINE_POSITION)) {
                fullRedraw = false;
            }
            this.redraw(fullRedraw);
        });

        messagingService.register(TimelinePatternChangedMessage.class, message -> {
            Optional<TimelineClip> clip = timelineAccessor.findClipById(message.getComponentId());
            Optional<Integer> channelIndex = timelineAccessor.findChannelIndexForClipId(message.getComponentId());

            if (clip.isPresent() && channelIndex.isPresent() && isVisible(clip.get().getGlobalInterval(), channelIndex.get())) {
                redraw(true);
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
            onMouseMoved(event.getX(), event.getY(), event);
            if (!event.isPrimaryButtonDown()) {
                dragRepository.clean();
                timelineState.getMoveSpecialPointLineProperties().setEnabled(false);
                selectionBox = null;
            }
        });

        canvas.setOnMouseExited(event -> {
            closeTooltip();
        });

        canvas.setOnMousePressed(event -> {
            if (scene.getFocusOwner() instanceof Control) {
                JavaFXUiMain.canvas.requestFocus(); // remove focus from any control element
            }
            double position = mapCanvasPixelToTime(event.getX());
            double currentX = position;
            Optional<TimelineUiCacheElement> optionalElement = findElementAt(event.getX(), event.getY());

            if (event.getY() < TIMELINE_TIMESCALE_HEIGHT) {
                TimelineCanvasTimelineHeaderClickHandlerRequest timelineCanvasTimelineHeaderClickHandlerRequest = TimelineCanvasTimelineHeaderClickHandlerRequest.builder()
                        .withEvent(event)
                        .withPosition(TimelinePosition.ofSeconds(position))
                        .withParentWindow(canvas.getScene().getWindow())
                        .build();

                timelineCanvasTimelineHeaderClickHandler.onHeaderClick(timelineCanvasTimelineHeaderClickHandlerRequest);
            } else if (event.isPrimaryButtonDown() && dragRepository.getInitialX() == -1 && optionalElement.isPresent()) {
                timelineCanvasElementClickHandler.onElementClick(event, currentX, optionalElement.get());
            } else {
                double positionY = event.getY() + calculateScrolledY();
                selectionBox = new CollisionRectangle(currentX, positionY, 0, 0);
            }

        });

        canvas.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            boolean isPrimaryButtonDown = event.isPrimaryButtonDown();

            if (isPrimaryButtonDown) {
                if (selectionBox != null) {

                    double positionX = mapCanvasPixelToTime(event.getX());
                    double canvasY = event.getY();
                    if (canvasY < TIMELINE_TIMESCALE_HEIGHT) {
                        canvasY = TIMELINE_TIMESCALE_HEIGHT;
                    }
                    double positionY = canvasY + calculateScrolledY();

                    selectionBox.width = (positionX - selectionBox.topLeftX);
                    selectionBox.height = (positionY - selectionBox.topLeftY);

                    recalculateSelectionModel();
                } else {
                    TimelineCanvasOnDragOverHandlerRequest request = TimelineCanvasOnDragOverHandlerRequest.builder()
                            .withChannel(findChannelAtPosition(x, y))
                            .withSelectedElement(findElementAt(x, y))
                            .withXPosition(TimelinePosition.ofSeconds(mapCanvasPixelToTime(x)))
                            .withMouseEvent(event)
                            .build();

                    timelineCanvasOnDragOverHandler.onDrag(x, y, false, request);
                }

                scrollVerticallyWhenDraggingNearEdge(y);
                scrollHorizontallyWhenDraggingNearEdge(x);

            }
            if (y < TIMELINE_TIMESCALE_HEIGHT && !dragRepository.isDraggingAnything() && selectionBox == null) {
                uiTimelineManager.jumpAbsolute(BigDecimal.valueOf(mapCanvasPixelToTime(x)));
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (!event.isStillSincePress()) {
                TimelineCanvasOnDragOverHandlerRequest request = TimelineCanvasOnDragOverHandlerRequest.builder()
                        .withChannel(findChannelAtPosition(event.getX(), event.getY()))
                        .withSelectedElement(findElementAt(event.getX(), event.getY()))
                        .withXPosition(TimelinePosition.ofSeconds(mapCanvasPixelToTime(event.getX())))
                        .withMouseEvent(event)
                        .build();

                timelineCanvasOnDragOverHandler.onDrag(event.getX(), event.getY(), true, request);
            }
            disableToolsOnMouseRelease();
        });

        canvas.setOnDragDropped(event -> {
            TimelineCanvasOnDragOverHandlerRequest request = TimelineCanvasOnDragOverHandlerRequest.builder()
                    .withChannel(findChannelAtPosition(event.getX(), event.getY()))
                    .withEvent(event)
                    .withSelectedElement(findElementAt(event.getX(), event.getY()))
                    .withXPosition(TimelinePosition.ofSeconds(mapCanvasPixelToTime(event.getX())))
                    .build();

            timelineCanvasOnDragOverHandler.onDrag(event.getX(), event.getY(), true, request);

            disableToolsOnMouseRelease();
        });

        canvas.setOnMouseDragReleased(event -> {
            disableToolsOnMouseRelease();
        });

        canvas.setOnDragExited(event -> {
            disableToolsOnMouseRelease();
        });

        canvas.setOnDragOver(event -> {
            TimelineCanvasOnDragOverHandlerRequest request = TimelineCanvasOnDragOverHandlerRequest.builder()
                    .withChannel(findChannelAtPosition(event.getX(), event.getY()))
                    .withEvent(event)
                    .withSelectedElement(findElementAt(event.getX(), event.getY()))
                    .withXPosition(TimelinePosition.ofSeconds(mapCanvasPixelToTime(event.getX())))
                    .build();

            timelineCanvasOnDragOverHandler.onDragOver(request);
        });

        canvas.setOnMouseClicked(event -> {
            TimelineCanvasClickHandlerRequest request = TimelineCanvasClickHandlerRequest.builder()
                    .withChannelSelected(findChannelAtPosition(event.getX(), event.getY()))
                    .withEvent(event)
                    .withParentWindow(canvas.getScene().getWindow())
                    .withSelectedElement(findElementAt(event.getX(), event.getY()))
                    .withXPosition(TimelinePosition.ofSeconds(mapCanvasPixelToTime(event.getX())))
                    .build();

            boolean shouldRedraw = timelineCanvasClickHandler.onClick(request);

            if (shouldRedraw) {
                redraw(true);
            }

        });

        canvas.setOnContextMenuRequested(event -> {
            Optional<TimelineUiCacheElement> optionalElement = findElementAt(event.getX(), event.getY());

            TimelineCanvasContextMenuRequestedHandlerRequest request = TimelineCanvasContextMenuRequestedHandlerRequest.builder()
                    .withEvent(event)
                    .withParentWindow(canvas.getScene().getWindow())
                    .withSelectedElement(optionalElement)
                    .build();

            timelineCanvasContextMenuRequestedHandler.onContextMenuRequested(request);
        });

        return resultPane;
    }

    private void disableToolsOnMouseRelease() {
        dragRepository.clean();
        selectionBox = null;
        timelineState.disableSpecialPointLineProperties();
        closeTooltip();
        redraw(false);
    }

    private void closeTooltip() {
        if (tooltip != null) {
            tooltip.hide();
            tooltip = null;
        }
    }

    private void scrollVerticallyWhenDraggingNearEdge(double y) {
        double maxY = rightBar.getMax();
        double currentYValue = rightBar.getValue();
        if (y >= canvas.getHeight() - DRAG_SCROLL_THRESHOLD) {
            double scaler = (y - (canvas.getHeight() - DRAG_SCROLL_THRESHOLD)) * 0.1;
            currentYValue += 10 * scaler;
            if (currentYValue > maxY) {
                currentYValue = maxY;
            }
            rightBar.setValue(currentYValue);
        }
        if (y <= TIMELINE_TIMESCALE_HEIGHT + DRAG_SCROLL_THRESHOLD && y > DRAG_SCROLL_THRESHOLD) {
            double scaler = ((TIMELINE_TIMESCALE_HEIGHT + DRAG_SCROLL_THRESHOLD) - y) * 0.1;
            currentYValue -= 10 * scaler;
            if (currentYValue < 0) {
                currentYValue = 0;
            }
            rightBar.setValue(currentYValue);
        }
    }

    private void scrollHorizontallyWhenDraggingNearEdge(double x) {
        double maxX = bottomBar.getMax();
        double currentXValue = bottomBar.getValue();
        if (x >= canvas.getWidth() - DRAG_SCROLL_THRESHOLD) {
            double scaler = (x - (canvas.getWidth() - DRAG_SCROLL_THRESHOLD)) / DRAG_SCROLL_THRESHOLD;
            currentXValue += 10 * scaler;
            if (currentXValue > maxX) {
                currentXValue = maxX;
            }
            bottomBar.setValue(currentXValue);
        }
        if (x <= TIMELINE_TIMESCALE_HEIGHT + DRAG_SCROLL_THRESHOLD) {
            double scaler = ((TIMELINE_TIMESCALE_HEIGHT + DRAG_SCROLL_THRESHOLD) - x) * 0.1;
            currentXValue -= 10 * scaler;
            if (currentXValue < 0) {
                currentXValue = 0;
            }
            bottomBar.setValue(currentXValue);
        }
    }

    private void recalculateSelectionModel() {
        if (selectionBox != null) {
            List<TimelineUiCacheElement> selectedElements = new ArrayList<>();
            CollisionRectangle rectangleInCanvasSpace = getSelectionRectangleInCanvasSpace(selectionBox);
            for (var element : cachedVisibleElements) {
                CollisionRectangle rectangle = element.rectangle;
                if (rectangle.intersects(rectangleInCanvasSpace)) {
                    selectedElements.add(element);
                }
            }

            selectedNodeRepository.clearAllSelectedItems();
            for (var element : selectedElements) {
                if (element.elementType.equals(TimelineUiCacheType.CLIP)) {
                    selectedNodeRepository.addSelectedClip(element.elementId);
                } else if (element.elementType.equals(TimelineUiCacheType.EFFECT)) {
                    selectedNodeRepository.addSelectedEffect(element.elementId);
                }
            }
            redraw(true);
        }
    }

    private Optional<TimelineChannel> findChannelAtPosition(double x, double originalY) {
        double y = originalY + calculateScrolledY();
        List<ChannelHeightResponse> channelHeights = getChannelsHeights();

        for (var element : channelHeights) {
            if (y >= element.top && y <= element.bottom) {
                return Optional.of(element.channel);
            }
        }

        return Optional.empty();
    }

    public List<ChannelHeightResponse> getChannelsHeights() {
        List<ChannelHeightResponse> result = new ArrayList<>();
        double channelStartY = TIMELINE_TIMESCALE_HEIGHT + CHANNEL_PADDING;

        for (int i = 0; i < timelineAccessor.getChannels().size(); ++i) {
            TimelineChannel currentChannel = timelineAccessor.getChannels().get(i);

            double clipHeight = calculateHeight(currentChannel) + CHANNEL_PADDING * 2;

            double top = channelStartY;
            double bottom = channelStartY + clipHeight;

            result.add(new ChannelHeightResponse(top, bottom, currentChannel));

            channelStartY += clipHeight;
        }
        return result;
    }

    private void onMouseMoved(double x, double y, MouseEvent event) {
        Optional<TimelineUiCacheElement> optionalElement = findElementAt(x, y);

        if (optionalElement.isPresent()) {
            var element = optionalElement.get();
            boolean isResizing = timelineCanvasElementClickHandler.isResizing(element, x);

            if (isResizing) {
                canvas.setCursor(Cursor.H_RESIZE);
            } else {
                canvas.setCursor(Cursor.HAND);
            }
        } else {
            canvas.setCursor(null);
        }

        if (y < TIMELINE_TIMESCALE_HEIGHT) {
            boolean success = false;
            for (var marker : markerRepository.getMarkers().entrySet()) {
                double markerPosition = timelineState.secondsToPixelsWithZoom(marker.getKey());
                double mousePosition = x;

                if (Math.abs(markerPosition - mousePosition) < 5) {
                    if (tooltip != null && marker.getKey().equals(lastMarkerShownPosition)) {
                        success = true;
                        break;
                    } else
                        closeTooltip();
                    tooltip = new Tooltip(marker.getValue().describe());
                    Point2D sceneCoordinates = canvas.localToScene(markerPosition, TIMELINE_TIMESCALE_HEIGHT);
                    tooltip.show(canvas, sceneCoordinates.getX(), sceneCoordinates.getY());
                    lastMarkerShownPosition = marker.getKey();
                    success = true;
                    break;
                }
            }
            if (!success) {
                closeTooltip();
            }
        } else {
            closeTooltip();
        }

    }

    public Optional<TimelineUiCacheElement> findElementAt(double x, double y) {
        for (var element : cachedVisibleElements) {
            if (element.rectangle.containsPoint(x, y)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
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
        scaleValue = scaleValue * zoomFactor;
        if (scaleValue < 0.001) {
            scaleValue = 0.001;
            return;
        }
        if (scaleValue > 25) {
            scaleValue = 25;
            return;
        }

        double mousePointerTime = mapCanvasPixelToTime(mousePoint.getX());
        double newTime = mousePointerTime * zoomFactor;
        double translateSeconds = getTranslateSeconds();

        double newTranslate = translateSeconds + (newTime - mousePointerTime);

        if (newTranslate < 0) {
            newTranslate = 0;
        }
        setTranslateSeconds(newTranslate);

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

    public void redraw(boolean fullRedraw) {
        boolean newState = fullRedraw || Optional.ofNullable(redrawRequest.get()).map(a -> a.fullRedraw).orElse(false);
        redrawRequest.set(new CanvasRedrawRequest(newState));
    }

    public void redrawInternal(boolean fullRedraw) {
        LOGGER.trace("Canvas redraw requested, fullRedraw={}, time={}", fullRedraw, System.currentTimeMillis());

        graphics.setLineWidth(1.0);
        timelineState.setVisibleLength(TimelineLength.ofSeconds(mapCanvasPixelToTime(canvas.getWidth())));

        double timelineWidth = timelineState.secondsToPixelsWithZoom(timelineAccessor.findEndPosition().add(TimelineLength.ofSeconds(10)));
        timelineWidth -= canvas.getWidth();
        if (timelineWidth < 1000) {
            timelineWidth = 1000;
        }
        bottomBar.setMin(0);
        bottomBar.setMax(timelineWidth);

        clearCanvas();

        double visibleAreaStartY = calculateScrolledY();
        if (visibleAreaStartY < 0) {
            visibleAreaStartY = 0;
        }
        TimelineInterval visibleInterval = TimelineInterval.fromDoubles(timelineState.getTranslateDouble(), timelineState.getTimelineLengthDouble() + timelineState.getTranslateDouble());

        double channelStartY = TIMELINE_TIMESCALE_HEIGHT + CHANNEL_PADDING;

        if (fullRedraw || previouslyCachedImage == null) {
            List<TimelineUiCacheElement> newCachedElements = new ArrayList<>();

            for (int i = 0; i < timelineAccessor.getChannels().size(); ++i) {
                TimelineChannel currentChannel = timelineAccessor.getChannels().get(i);
                NonIntersectingIntervalList<TimelineClip> clips = currentChannel.getAllClips().shallowCopy();

                double clipHeight = calculateHeight(currentChannel);

                if ((channelStartY + clipHeight) >= visibleAreaStartY && (channelStartY) <= visibleAreaStartY + canvas.getHeight()) {
                    for (var clip : clips) {
                        TimelineInterval interval = clip.getGlobalInterval();

                        double clipX = timelineState.secondsToPixelsWidthZoomAndTranslate(interval.getStartPosition());
                        double clipEndX = timelineState.secondsToPixelsWidthZoomAndTranslate(interval.getEndPosition());

                        double clipWidth = clipEndX - clipX;
                        double clipY = channelStartY - visibleAreaStartY;

                        boolean isPrimarySelectedClip = (selectedNodeRepository.getPrimarySelectedClip().map(a -> a.equals(clip.getId())).orElse(false));
                        boolean isSecondarySelectedClip = (selectedNodeRepository.getSelectedClipIds()
                                .stream()
                                .filter(a -> a.equals(clip.getId()))
                                .findAny()
                                .map(a -> true)
                                .orElse(false));

                        drawClip(visibleInterval, clip, visibleAreaStartY, clipY);

                        if (isPrimarySelectedClip) {
                            graphics.setFill(new Color(0.0, 1.0, 1.0, 0.3));
                            graphics.fillRoundRect(clipX, clipY, clipWidth, MIN_CHANNEL_HEIGHT, 4, 4);
                        } else if (isSecondarySelectedClip) {
                            graphics.setFill(new Color(0.0, 1.0, 1.0, 0.2));
                            graphics.fillRoundRect(clipX, clipY, clipWidth, MIN_CHANNEL_HEIGHT, 4, 4);
                        }

                        newCachedElements.add(new TimelineUiCacheElement(clip.getId(), TimelineUiCacheType.CLIP, new CollisionRectangle(clipX, clipY, clipWidth, MIN_CHANNEL_HEIGHT)));

                        List<NonIntersectingIntervalList<StatelessEffect>> effects = shallowCloneEffects(clip.getEffectChannels());
                        for (int j = 0; j < effects.size(); ++j) {
                            for (var effect : effects.get(j)) {

                                double effectX = timelineState.secondsToPixelsWidthZoomAndTranslate(effect.getGlobalInterval().getStartPosition());
                                double effectEndX = timelineState.secondsToPixelsWidthZoomAndTranslate(effect.getGlobalInterval().getEndPosition());
                                double effectY = clipY + MIN_CHANNEL_HEIGHT + EFFECT_HEIGHT * j;

                                if (effectEndX > clipEndX) {
                                    effectEndX = clipEndX;
                                }
                                if (effectX < clipX) {
                                    effectX = clipX;
                                }

                                double effectWidth = effectEndX - effectX;
                                double effectHeight = EFFECT_HEIGHT;

                                if (effectWidth < 0) {
                                    continue;
                                }

                                boolean isPrimarySelectedEffect = (selectedNodeRepository.getPrimarySelectedEffect().map(a -> a.equals(effect.getId())).orElse(false));
                                boolean isSecondarySelectedEffect = (selectedNodeRepository.getSelectedEffectIds()
                                        .stream()
                                        .filter(a -> a.equals(effect.getId()))
                                        .findAny()
                                        .map(a -> true)
                                        .orElse(false));

                                if (isPrimarySelectedEffect) {
                                    graphics.setFill(new Color(0, 1, 1, 1));
                                } else if (isSecondarySelectedEffect) {
                                    graphics.setFill(new Color(0, 0.8, 0.8, 1));
                                } else {
                                    graphics.setFill(new Color(0, 0.5, 0.5, 1));
                                }

                                graphics.fillRoundRect(effectX, effectY, effectWidth, effectHeight - 1, 12, 12);

                                graphics.setFont(new Font(10.0));
                                graphics.setStroke(Color.WHITE);
                                graphics.setTextAlign(TextAlignment.LEFT);

                                graphics.save();
                                graphics.beginPath();
                                graphics.rect(effectX, effectY, effectWidth, effectHeight);
                                graphics.clip();
                                String name = Optional.ofNullable(nameToIdRepository.getNameForId(effect.getId())).orElse(effect.getId());
                                graphics.strokeText(name, effectX + 2, effectY + effectHeight / 2.0);
                                graphics.restore();

                                newCachedElements.add(new TimelineUiCacheElement(effect.getId(), TimelineUiCacheType.EFFECT, new CollisionRectangle(effectX, effectY, effectWidth, effectHeight)));
                            }
                        }
                    }

                    graphics.setStroke(Color.GRAY);
                    graphics.setLineWidth(0.8);
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

            previouslyCachedImage = canvas.snapshot(new SnapshotParameters(), null);
            cachedVisibleElements = newCachedElements;
        } else {
            graphics.drawImage(previouslyCachedImage, 0, 0);
        }

        drawSelectionBox();
        drawLoopingLines();
        drawChapterLines();
        drawPlaybackLine();
        drawSpecialPositionLine(visibleAreaStartY);
    }

    private List<NonIntersectingIntervalList<StatelessEffect>> shallowCloneEffects(List<NonIntersectingIntervalList<StatelessEffect>> effectChannels) {
        List<NonIntersectingIntervalList<StatelessEffect>> result = new ArrayList<>();
        for (int i = 0; i < effectChannels.size(); ++i) {
            result.add(effectChannels.get(i).shallowCopy());
        }
        return result;
    }

    private void drawClip(TimelineInterval visibleInterval, TimelineClip clip, double xOffset, double clipY) {
        TimelinePosition clipIntervalStartPosition = clip.getInterval().getStartPosition();
        List<PatternIntervalAware> pattern = timelinePatternRepository.getPatternForClipId(clip.getId(), visibleInterval.butAddOffset(clipIntervalStartPosition.negate()));
        TimelinePosition clipEndPosition = clip.getInterval().getEndPosition();

        double clipStartPosition = timelineState.secondsToPixelsWidthZoomAndTranslate(clipIntervalStartPosition);

        if (pattern.size() == 0) {
            double segmentStartPosition = timelineState.secondsToPixelsWidthZoomAndTranslate(clipIntervalStartPosition);
            double segmentEndPosition = timelineState.secondsToPixelsWidthZoomAndTranslate(clipEndPosition);

            double segmentWidth = (segmentEndPosition - segmentStartPosition);

            if (segmentWidth > 0.01) {
                graphics.setFill(Color.BLACK);
                graphics.fillRect(segmentStartPosition, clipY, segmentWidth, MIN_CHANNEL_HEIGHT);
            }
            return;
        }

        double clipImageStartPosition = timelineState.secondsToPixelsWidthZoomAndTranslate(pattern.get(0).getInterval().getStartPosition());
        double widthOfFirstSegment = clipImageStartPosition - clipStartPosition;

        if (widthOfFirstSegment >= 0.01) {
            graphics.setFill(Color.BLACK);
            graphics.fillRect(clipStartPosition, clipY, widthOfFirstSegment, MIN_CHANNEL_HEIGHT);
        }

        for (int i = 0; i < pattern.size(); ++i) {
            PatternIntervalAware data = pattern.get(i);
            TimelinePosition imageStartPosition = data.interval.getStartPosition().add(clipIntervalStartPosition);
            double imageStartX = timelineState.secondsToPixelsWidthZoomAndTranslate(imageStartPosition);

            TimelineLength length = data.getInterval().getLength();
            TimelinePosition imageEndPosition = clipIntervalStartPosition.add(data.interval.getEndPosition());
            if (imageEndPosition.compareTo(clipEndPosition) > 0) {
                length = clipEndPosition.subtract(imageStartPosition).toLength();
            }

            if (length.compareTo(TimelineLength.ofZero()) > 0) {
                double width = timelineState.secondsToPixelsWithZoom(length);

                graphics.drawImage(data.image, imageStartX, clipY, width, MIN_CHANNEL_HEIGHT);

                double nextSegmentEnd;
                TimelinePosition nextEndPosition = null;
                if (i == pattern.size() - 1) {
                    nextEndPosition = clipEndPosition;
                } else {
                    TimelinePosition nextIntervalStartPosition = pattern.get(i + 1).getInterval().getStartPosition();
                    nextEndPosition = nextIntervalStartPosition.add(clipIntervalStartPosition);
                }

                if (nextEndPosition.compareTo(clipEndPosition) > 0) {
                    nextEndPosition = clipEndPosition;
                }

                nextSegmentEnd = timelineState.secondsToPixelsWidthZoomAndTranslate(nextEndPosition);
                double nextSegmentStartX = imageStartX + width;
                double nextSegmentWidth = (nextSegmentEnd - nextSegmentStartX);

                if (nextSegmentWidth >= 0.01) {
                    graphics.setFill(Color.BLACK);
                    graphics.fillRect(nextSegmentStartX, clipY, nextSegmentWidth, MIN_CHANNEL_HEIGHT);
                }
            }
        }

    }

    private void drawSelectionBox() {
        CollisionRectangle selection = selectionBox;

        if (selection != null) {
            CollisionRectangle rectangleInCanvasSpace = getSelectionRectangleInCanvasSpace(selection);

            double topLeftY = rectangleInCanvasSpace.topLeftY;
            double height = rectangleInCanvasSpace.height;
            if (topLeftY < TIMELINE_TIMESCALE_HEIGHT) {
                double heightDiff = TIMELINE_TIMESCALE_HEIGHT - topLeftY;
                topLeftY = TIMELINE_TIMESCALE_HEIGHT;
                height -= heightDiff;
            }

            graphics.setFill(new Color(0, 1, 1, 0.6));
            graphics.fillRect(rectangleInCanvasSpace.topLeftX, topLeftY, rectangleInCanvasSpace.width, height);
        }
    }

    private CollisionRectangle getSelectionRectangleInCanvasSpace(CollisionRectangle selection) {
        double screenX = timelineState.secondsToPixelsWidthZoomAndTranslate(TimelinePosition.ofSeconds(selection.topLeftX));
        double screenY = selection.topLeftY - calculateScrolledY();
        double screenW = timelineState.secondsToPixelsWithZoom(TimelinePosition.ofSeconds(selection.width));
        double screenH = selection.height;

        if (screenW < 0.0) {
            screenX += screenW;
            screenW *= -1.0;
        }
        if (screenH < 0.0) {
            screenY += screenH;
            screenH *= -1.0;
        }
        return new CollisionRectangle(screenX, screenY, screenW, screenH);
    }

    private double calculateScrolledY() {
        double scrolledY = timelineState.getVscroll().get();
        double fullHeight = rightBar.getMax() - canvas.getHeight() - CHANNEL_PADDING;
        double visibleAreaStartY = fullHeight * scrolledY;
        return visibleAreaStartY;
    }

    private void drawLoopingLines() {
        Optional<TimelinePosition> loopAProperties = timelineState.getLoopALineProperties();
        Optional<TimelinePosition> loopBProperties = timelineState.getLoopBLineProperties();
        if (loopAProperties.isPresent()) {
            drawVerticalLineAtPosition(loopAProperties.get(), Color.GREEN);
        }
        if (loopBProperties.isPresent()) {
            drawVerticalLineAtPosition(loopBProperties.get(), Color.GREEN);
        }
    }

    private void drawChapterLines() {
        for (var chapter : markerRepository.getMarkers().entrySet()) {
            Color colorToUse = null;
            switch (chapter.getValue().getType()) {
                case CHAPTER :
                    colorToUse = Color.GREENYELLOW;
                    break;
                case GENERAL :
                    colorToUse = Color.SIENNA;
                    break;
                case INPOINT :
                    colorToUse = Color.GREEN;
                    break;
                case OUTPOINT :
                    colorToUse = Color.RED;
                    break;
                default :
                    colorToUse = Color.PINK;
                    break;
            }

            drawVerticalLineAtPosition(chapter.getKey(), colorToUse);
        }
    }

    private void drawVerticalLineAtPosition(TimelinePosition seconds, Color color) {
        int position = timelineState.secondsToPixelsWidthZoomAndTranslate(seconds);
        graphics.setStroke(color);
        graphics.setLineWidth(1.0);
        graphics.strokeLine(position, 0, position, canvas.getHeight());
    }

    private void drawSpecialPositionLine(double visibleAreaStartY) {
        TimelineLineProperties line = timelineState.getMoveSpecialPointLineProperties();
        if (line.isEnabled()) {
            int a = timelineAccessor.findChannelIndexByChannelId(line.getStartChannel()).get();
            int b = timelineAccessor.findChannelIndexByChannelId(line.getEndChannel()).get();

            int startChannel = Math.min(a, b);
            int endChannel = Math.max(a, b);

            List<ChannelHeightResponse> channelHeights = getChannelsHeights();

            double top = channelHeights.get(startChannel).top - visibleAreaStartY;
            double bottom;
            if (startChannel == endChannel) {
                bottom = channelHeights.get(startChannel).bottom - visibleAreaStartY;
            } else {
                bottom = channelHeights.get(endChannel).bottom - visibleAreaStartY;
            }
            double x = timelineState.secondsToPixelsWidthZoomAndTranslate(line.getPosition());

            graphics.setStroke(Color.RED);
            graphics.setLineWidth(1.0);
            graphics.strokeLine(x, top, x, bottom);

        }
    }

    private void drawTimelineTitles() {
        graphics.setFill(new Color(0x33 / 255.0, 0x33 / 255.0, 0x33 / 255.0, 1.0));
        graphics.fillRect(0, 0, canvas.getWidth(), TIMELINE_TIMESCALE_HEIGHT);
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(1.0);

        double left = mapCanvasPixelToTime(0);
        double right = mapCanvasPixelToTime(canvas.getWidth());

        int numberOfTenthSeconds = (int) ((right - left) * 10);
        int numberOfSeconds = (int) (right - left);
        int numberOfTenSeconds = (int) ((right - left) / 10);
        int numberOfMinutes = (int) ((right - left) / 60);
        int numberOfTenMinutes = (int) ((right - left) / 600);
        int numberOfHours = (int) ((right - left) / (60 * 60));
        boolean appliedLabels = false;

        if (numberOfTenthSeconds < 300) {
            appliedLabels = numberOfTenthSeconds < 20 && !appliedLabels;
            drawLines(left, right, 3, 10, 1, appliedLabels);
        }
        if (numberOfSeconds < 300) {
            appliedLabels = numberOfSeconds < 20 && !appliedLabels;
            drawLines(left, right, 5, 1, 1, appliedLabels);
        }
        if (numberOfTenSeconds < 300) {
            appliedLabels = numberOfTenSeconds < 20 && !appliedLabels;
            drawLines(left, right, 8, 1 / 10.0, 1.5, appliedLabels);
        }
        if (numberOfMinutes < 300) {
            appliedLabels = numberOfMinutes < 20 && !appliedLabels;
            drawLines(left, right, 12, 1 / 60.0, 2.5, appliedLabels);
        }
        if (numberOfTenMinutes < 300) {
            appliedLabels = numberOfTenMinutes < 20 && !appliedLabels;
            drawLines(left, right, 14, 1 / 600.0, 2.5, appliedLabels);
        }
        if (numberOfHours < 300) {
            appliedLabels = numberOfHours < 20 && !appliedLabels;
            drawLines(left, right, 14, 1.0 / 3600.0, 3, appliedLabels);
        }
        graphics.setLineWidth(1.0);
        graphics.setStroke(new Color(0x66 / 255.0, 0x66 / 255.0, 0x66 / 255.0, 0.9));
        graphics.strokeLine(0, TIMELINE_TIMESCALE_HEIGHT + 1, canvas.getWidth(), TIMELINE_TIMESCALE_HEIGHT + 1);
    }

    public void drawLabel(TimelinePosition seconds, boolean writeMilliseconds) {
        String text = formatSeconds(seconds.getSeconds().doubleValue(), writeMilliseconds);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setLineWidth(0.5);
        graphics.setFont(new Font(8.0));
        double x = timelineState.secondsToPixelsWidthZoomAndTranslate(seconds);
        graphics.strokeText(text, x, 8);
    }

    private String formatSeconds(double secondsInput, boolean writeMilliseconds) {
        int hours = (int) (secondsInput / 3600);
        int minutes = (int) ((secondsInput % 3600) / 60);
        int seconds = (int) ((secondsInput % 60));
        int millis = (int) (((secondsInput % 1) * 1000));

        if (writeMilliseconds) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        } else {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    private void drawLines(double left, double right, double height, double divider, double width, boolean drawLabels) {
        int value = (int) (left * divider);
        int newRight = (int) (Math.ceil(right * divider));

        while (value <= newRight) {
            TimelinePosition seconds = TimelinePosition.ofSeconds(value / divider);
            double pos = timelineState.secondsToPixelsWidthZoomAndTranslate(seconds);
            graphics.setLineWidth(width);
            graphics.strokeLine(pos, TIMELINE_TIMESCALE_HEIGHT - height, pos, TIMELINE_TIMESCALE_HEIGHT);
            value += 1;
            if (drawLabels && seconds.compareTo(TimelinePosition.ofZero()) > 0) {
                drawLabel(seconds, divider >= 1.0);
            }
        }
    }

    private double mapCanvasPixelToTime(double position) {
        double translatedPosition = timelineState.getTranslate().get() + position;
        return timelineState.pixelsToSecondsWithZoom(translatedPosition).getSeconds().doubleValue();
    }

    private double calculateHeight(TimelineChannel currentChannel) {
        NonIntersectingIntervalList<TimelineClip> clips = currentChannel.getAllClips().shallowCopy();
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
        graphics.setFill(new Color(0x33 / 255.0, 0x33 / 255.0, 0x33 / 255.0, 1.0));
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawPlaybackLine() {
        double x = timelineState.secondsToPixelsWidthZoomAndTranslate(timelineState.getPlaybackPosition());
        graphics.setStroke(Color.YELLOW);
        graphics.setLineWidth(2.0);
        graphics.strokeLine(x, 0, x, canvas.getHeight());
    }

    @Override
    public void postProcess(Scene scene) {
        this.scene = scene;
    }
}

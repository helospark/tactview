package com.helospark.tactview.ui.javafx.uicomponents;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;
import static javafx.geometry.Orientation.VERTICAL;

import java.math.BigDecimal;
import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.LinkClipRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CutClipCommand;
import com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.TimelineCanvas;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.TimelinePatternRepository;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class UiTimeline {
    private static final String LOOP_BUTTON_ENABLED_CLASS = "loop-button-enabled";
    private TimelineState timelineState;
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private UiTimelineManager uiTimelineManager;
    private LinkClipRepository linkClipRepository;
    private TimelineCanvas timelineCanvas;

    private VBox timelineTitlesPane;
    private BorderPane borderPane;

    public UiTimeline(MessagingService messagingService,
            TimelineState timelineState, UiCommandInterpreterService commandInterpreter,
            TimelineManagerAccessor timelineManager, UiTimelineManager uiTimelineManager,
            LinkClipRepository linkClipRepository, ByteBufferToJavaFxImageConverter byteBufferToJavaFxImageConverter, TimelinePatternRepository timelinePatternRepository,
            TimelineCanvas timelineCanvas) {
        this.timelineState = timelineState;
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.uiTimelineManager = uiTimelineManager;
        this.linkClipRepository = linkClipRepository;
        this.timelineCanvas = timelineCanvas;
    }

    public BorderPane createTimeline(VBox lower, BorderPane root) {
        borderPane = new BorderPane();

        HBox titleBarTop = createTimelineButtonPanel();

        VBox timelineTopRow = new VBox();
        timelineTopRow.getChildren().add(titleBarTop);

        borderPane.setTop(timelineTopRow);

        timelineTitlesPane = new VBox();
        timelineTitlesPane.getStyleClass().add("timeline-titles-pane");
        ScrollPane timelineTitlesScrollPane = new ScrollPane();
        timelineTitlesScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        timelineTitlesScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        timelineTitlesScrollPane.vvalueProperty().bindBidirectional(timelineState.getVscroll());

        VBox timelineTitles = new VBox();
        Bindings.bindContentBidirectional(timelineState.getChannelTitlesAsNodes(), timelineTitles.getChildren());
        timelineTitlesScrollPane.setContent(timelineTitles);
        timelineTitlesPane.getChildren().add(timelineTitlesScrollPane);

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

        borderPane.setCenter(timelineCanvas.create(timelineTitlesPane));

        return borderPane;
    }

    protected HBox createTimelineButtonPanel() {
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

        Button eraserMarkerButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.ERASER));
        eraserMarkerButton.setTooltip(new Tooltip("Erase loop markers"));
        eraserMarkerButton.disableProperty().set(true);
        eraserMarkerButton.setOnMouseClicked(event -> {
            timelineState.setLoopBProperties(null);
            timelineState.setLoopAProperties(null);
            disableClearMarker(eraserMarkerButton);
        });

        Button addAMarkerButton = new Button("A", new Glyph("FontAwesome", FontAwesome.Glyph.RETWEET));
        addAMarkerButton.setTooltip(new Tooltip("Loop start time"));
        addAMarkerButton.setOnAction(event -> {
            if (timelineState.getLoopBLineProperties().isPresent() && timelineState.getLoopBLineProperties().get().isLessOrEqualToThan(uiTimelineManager.getCurrentPosition())) {
                timelineState.setLoopBProperties(null);
            }
            timelineState.setLoopAProperties(uiTimelineManager.getCurrentPosition());
            enableClearMarker(eraserMarkerButton);
        });

        Button addBMarkerButton = new Button("B", new Glyph("FontAwesome", FontAwesome.Glyph.RETWEET));
        addBMarkerButton.setTooltip(new Tooltip("Loop end time"));
        addBMarkerButton.setOnMouseClicked(event -> {
            if (timelineState.getLoopALineProperties().isPresent() && timelineState.getLoopALineProperties().get().isGreaterOrEqualToThan(uiTimelineManager.getCurrentPosition())) {
                timelineState.setLoopAProperties(null);
            }
            timelineState.setLoopBProperties(uiTimelineManager.getCurrentPosition());
            enableClearMarker(eraserMarkerButton);
        });

        HBox titleBarTop = new HBox();
        titleBarTop.getStyleClass().add("timeline-title-bar");
        titleBarTop.getChildren().addAll(addChannelButton, new Separator(VERTICAL), cutAllClipsButton, new Separator(VERTICAL), addAMarkerButton, addBMarkerButton, eraserMarkerButton,
                new Separator(VERTICAL));
        return titleBarTop;
    }

    private void disableClearMarker(Button eraserMarkerButton) {
        eraserMarkerButton.getStyleClass().remove(LOOP_BUTTON_ENABLED_CLASS);
        eraserMarkerButton.disableProperty().set(true);
    }

    private void enableClearMarker(Button eraserMarkerButton) {
        eraserMarkerButton.getStyleClass().add(LOOP_BUTTON_ENABLED_CLASS);
        eraserMarkerButton.disableProperty().set(false);
    }

    private void jumpTo(double xPosition) {
        TimelinePosition position = timelineState.pixelsToSeconds(xPosition).divide(BigDecimal.valueOf(timelineState.getZoom())).add(timelineState.getTimeAtLeftSide());
        uiTimelineManager.jumpAbsolute(position.getSeconds());
    }

    public void updateLine(TimelinePosition position) {
        timelineState.setLinePosition(position);
    }

}

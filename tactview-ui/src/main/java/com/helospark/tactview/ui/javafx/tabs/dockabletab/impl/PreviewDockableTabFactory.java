package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import java.math.BigDecimal;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponent;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

@Component
public class PreviewDockableTabFactory extends AbstractCachingDockableTabFactory {
    public static final String ID = "preview";
    private UiPlaybackPreferenceRepository playbackPreferenceRepository;
    private AudioVisualizationComponent audioVisualazationComponent;
    private UiProjectRepository uiProjectRepository;
    private UiTimelineManager uiTimelineManager;
    private SingleFullImageViewController fullScreenRenderer;
    private ScaleComboBoxFactory scaleComboBoxFactory;

    public Label videoTimestampLabel;

    public PreviewDockableTabFactory(UiPlaybackPreferenceRepository playbackPreferenceRepository, AudioVisualizationComponent audioVisualazationComponent, UiProjectRepository uiProjectRepository,
            UiTimelineManager uiTimelineManager, SingleFullImageViewController fullScreenRenderer, ScaleComboBoxFactory scaleComboBoxFactory) {
        this.playbackPreferenceRepository = playbackPreferenceRepository;
        this.audioVisualazationComponent = audioVisualazationComponent;
        this.uiProjectRepository = uiProjectRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.fullScreenRenderer = fullScreenRenderer;
        this.scaleComboBoxFactory = scaleComboBoxFactory;
    }

    private BorderPane createPreviewRightVBox() {
        ScrollPane previewScrollPane = new ScrollPane(
                createCentered(JavaFXUiMain.canvas));
        previewScrollPane.setFitToWidth(true);
        previewScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        previewScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        VBox rightVBox = new VBox(3);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setId("clip-view");
        rightVBox.getChildren().add(previewScrollPane);
        rightVBox.getChildren().add(audioVisualazationComponent.getCanvas());
        audioVisualazationComponent.clearCanvas();

        videoTimestampLabel = new Label("00:00:00.000");
        videoTimestampLabel.setId("video-timestamp-label");
        HBox videoStatusBar = new HBox(10);
        videoStatusBar.setId("video-status-bar");
        videoStatusBar.getChildren().add(videoTimestampLabel);
        rightVBox.getChildren().add(videoStatusBar);

        HBox underVideoBar = new HBox(1);
        ToggleButton muteButton = new ToggleButton("", new Glyph("FontAwesome",
                FontAwesome.Glyph.VOLUME_OFF));
        muteButton.setSelected(false);
        muteButton.setOnAction(event -> playbackPreferenceRepository.setMute(muteButton.isSelected()));
        muteButton.setTooltip(new Tooltip("Mute"));

        Button fullscreenButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.IMAGE));
        fullscreenButton.setOnMouseClicked(e -> fullScreenRenderer.renderFullScreenAtCurrentLocation());
        fullscreenButton.setTooltip(new Tooltip("Show full scale preview"));

        ToggleButton halfImageEffectButton = new ToggleButton("", new Glyph("FontAwesome", FontAwesome.Glyph.STAR_HALF_ALT));
        halfImageEffectButton.setSelected(false);
        halfImageEffectButton.setOnAction(e -> {
            playbackPreferenceRepository.setHalfEffect(halfImageEffectButton.isSelected());
            uiTimelineManager.refreshDisplay(true);
        });
        halfImageEffectButton.setTooltip(new Tooltip("Apply effects only on left side of preview"));

        Button playButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.PLAY));
        playButton.setOnMouseClicked(e -> uiTimelineManager.startPlayback());
        playButton.setTooltip(new Tooltip("Play"));

        Button stopButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.STOP));
        stopButton.setOnMouseClicked(e -> uiTimelineManager.stopPlayback());
        stopButton.setTooltip(new Tooltip("Stop"));

        Button jumpBackOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_BACKWARD));
        jumpBackOnFrameButton.setOnMouseClicked(e -> uiTimelineManager.moveBackOneFrame());
        jumpBackOnFrameButton.setTooltip(new Tooltip("Step one frame back"));

        Button jumpForwardOnFrameButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.STEP_FORWARD));
        jumpForwardOnFrameButton.setOnMouseClicked(e -> uiTimelineManager.moveForwardOneFrame());
        jumpForwardOnFrameButton.setTooltip(new Tooltip("Step one frame forward"));

        Button jumpBackButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.BACKWARD));
        jumpBackButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(-10)));
        jumpBackButton.setTooltip(new Tooltip("Step 10s back"));

        Button jumpForwardButton = new Button("", new Glyph("FontAwesome",
                FontAwesome.Glyph.FORWARD));
        jumpForwardButton.setOnMouseClicked(e -> uiTimelineManager.jumpRelative(BigDecimal.valueOf(10)));
        jumpForwardButton.setTooltip(new Tooltip("Step 10s forward"));

        ComboBox<String> sizeDropDown = scaleComboBoxFactory.create();

        ComboBox<String> playbackSpeedDropDown = new ComboBox<>();
        playbackSpeedDropDown.getStyleClass().add("size-drop-down");
        playbackSpeedDropDown.getItems().add("0.5x");
        playbackSpeedDropDown.getItems().add("0.75x");
        playbackSpeedDropDown.getItems().add("1.0x");
        playbackSpeedDropDown.getItems().add("1.25x");
        playbackSpeedDropDown.getItems().add("1.5x");
        playbackSpeedDropDown.getItems().add("2.0x");
        playbackSpeedDropDown.getSelectionModel().select("1.0x");
        playbackSpeedDropDown.setTooltip(new Tooltip("Playback speed"));
        playbackSpeedDropDown.valueProperty().addListener((o, oldValue, newValue2) -> {
            playbackPreferenceRepository.setPlaybackSpeedMultiplier(new BigDecimal(newValue2.replace("x", "")));
        });

        underVideoBar.getChildren().add(sizeDropDown);
        underVideoBar.getChildren().add(muteButton);
        underVideoBar.getChildren().add(halfImageEffectButton);
        underVideoBar.getChildren().add(fullscreenButton);
        underVideoBar.getChildren().add(jumpBackButton);
        underVideoBar.getChildren().add(jumpBackOnFrameButton);
        underVideoBar.getChildren().add(playButton);
        underVideoBar.getChildren().add(stopButton);
        underVideoBar.getChildren().add(jumpForwardOnFrameButton);
        underVideoBar.getChildren().add(jumpForwardButton);
        underVideoBar.getChildren().add(playbackSpeedDropDown);
        underVideoBar.setId("video-button-bar");
        rightVBox.getChildren().add(underVideoBar);

        rightVBox.widthProperty().addListener((e, oldV, newV) -> {
            int availableWidth = (int) (rightVBox.getWidth() - 20); // TODO: calculate magic value
            uiProjectRepository.setPreviewAvailableWidth(availableWidth);
        });
        rightVBox.heightProperty().addListener((e, oldV, newV) -> {
            int availableHeight = (int) (rightVBox.getHeight() - 100); // TODO: calculate magic value
            uiProjectRepository.setPreviewAvailableHeight(availableHeight);
        });

        BorderPane rightBorderPane = new BorderPane();
        rightBorderPane.setCenter(rightVBox);

        uiTimelineManager.registerUiPlaybackConsumer(position -> updateTime(position));
        return rightBorderPane;
    }

    private void updateTime(TimelinePosition position) {
        long wholePartOfTime = position.getSeconds().longValue();
        long hours = wholePartOfTime / 3600;
        long minutes = (wholePartOfTime - hours * 3600) / 60;
        long seconds = (wholePartOfTime - hours * 3600 - minutes * 60);
        long millis = position.getSeconds().remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(1000)).longValue();

        String newLabel = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);

        videoTimestampLabel.setText(newLabel);
    }

    private Node createCentered(Canvas canvas2) {
        GridPane outerPane = new GridPane();
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        row.setFillHeight(false);
        row.setValignment(VPos.CENTER);
        outerPane.getRowConstraints().add(row);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        col.setFillWidth(false);
        col.setHalignment(HPos.CENTER);
        outerPane.getColumnConstraints().add(col);

        outerPane.add(canvas2, 0, 0);
        return outerPane;
    }

    @Override
    public DetachableTab createTabInternal() {
        return new DetachableTab("Preview", createPreviewRightVBox(), ID);
    }

    @Override
    public boolean doesSupport(String id) {
        return ID.equals(id);
    }

    @Override
    public String getId() {
        return ID;
    }
}

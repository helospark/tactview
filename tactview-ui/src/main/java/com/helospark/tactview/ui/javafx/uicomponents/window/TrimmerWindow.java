package com.helospark.tactview.ui.javafx.uicomponents.window;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.CanvasStates;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.UiTimelineManagerFactory;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.AbstractPreviewDockableTabFactory;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponentFactory;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.FlowPane;

@Component
public class TrimmerWindow extends AbstractPreviewDockableTabFactory implements ResettableBean {
    public static final String ID = "trimmer-window";

    private FlowPane pane;
    ContextMenu elementContextMenu = null;

    Canvas canvas;

    public TrimmerWindow(UiPlaybackPreferenceRepository playbackPreferenceRepository, AudioVisualizationComponentFactory audioVisualazationComponentFactory, UiProjectRepository uiProjectRepository,
            SingleFullImageViewController fullScreenRenderer, ScaleComboBoxFactory scaleComboBoxFactory,
            MessagingService messagingService, CanvasStates canvasStates, UiTimelineManagerFactory uiTimelineManagerFactory) {
        super(ID, playbackPreferenceRepository, uiProjectRepository, audioVisualazationComponentFactory,
                fullScreenRenderer, scaleComboBoxFactory, messagingService, canvasStates, uiTimelineManagerFactory);
    }

    @Override
    public void resetDefaults() {
        if (pane != null) {
            pane.getChildren().clear();
        }
    }

    @Override
    public DetachableTab createTabInternal() {
        return new DetachableTab("Trimmer", createPreviewRightVBox(), ID);
    }

}

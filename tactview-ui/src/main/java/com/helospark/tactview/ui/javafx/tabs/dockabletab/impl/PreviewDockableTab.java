package com.helospark.tactview.ui.javafx.tabs.dockabletab.impl;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.CanvasStates;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.UiTimelineManagerFactory;
import com.helospark.tactview.ui.javafx.render.SingleFullImageViewController;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.ScaleComboBoxFactory;
import com.helospark.tactview.ui.javafx.uicomponents.audiocomponent.AudioVisualizationComponentFactory;

import javafx.scene.control.Label;

@Component
public class PreviewDockableTab extends AbstractPreviewDockableTabFactory {
    public static final String ID = "preview";

    public Label videoTimestampLabel;

    public PreviewDockableTab(UiPlaybackPreferenceRepository playbackPreferenceRepository, AudioVisualizationComponentFactory audioVisualazationComponentFactory,
            UiProjectRepository uiProjectRepository,
            SingleFullImageViewController fullScreenRenderer, ScaleComboBoxFactory scaleComboBoxFactory,
            MessagingService messagingService, CanvasStates canvasStates, UiTimelineManagerFactory uiTimelineManagerFactory) {
        super(ID, playbackPreferenceRepository, uiProjectRepository, audioVisualazationComponentFactory,
                fullScreenRenderer, scaleComboBoxFactory, messagingService, canvasStates, uiTimelineManagerFactory);
    }

    @Override
    public DetachableTab createTabInternal() {
        return new DetachableTab("Preview", createPreviewRightVBox(), ID);
    }

}

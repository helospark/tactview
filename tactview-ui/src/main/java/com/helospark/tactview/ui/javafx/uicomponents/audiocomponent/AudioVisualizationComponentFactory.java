package com.helospark.tactview.ui.javafx.uicomponents.audiocomponent;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.CanvasStateHolder;
import com.helospark.tactview.ui.javafx.PlaybackFrameAccessor;
import com.helospark.tactview.ui.javafx.UiPlaybackPreferenceRepository;
import com.helospark.tactview.ui.javafx.uicomponents.util.AudioRmsCalculator;

@Component
public class AudioVisualizationComponentFactory {
    private PlaybackFrameAccessor playbackController;
    private AudioRmsCalculator audioRmsCalculator;
    private UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository;

    public AudioVisualizationComponentFactory(PlaybackFrameAccessor playbackController, AudioRmsCalculator audioRmsCalculator, UiPlaybackPreferenceRepository uiPlaybackPreferenceRepository) {
        this.playbackController = playbackController;
        this.audioRmsCalculator = audioRmsCalculator;
        this.uiPlaybackPreferenceRepository = uiPlaybackPreferenceRepository;
    }

    public AudioVisualizationComponent create(CanvasStateHolder canvasStateHolder) {
        return new AudioVisualizationComponent(playbackController, audioRmsCalculator, canvasStateHolder, uiPlaybackPreferenceRepository);
    }

}

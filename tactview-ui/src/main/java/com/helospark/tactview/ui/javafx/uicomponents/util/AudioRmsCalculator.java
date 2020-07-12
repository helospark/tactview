package com.helospark.tactview.ui.javafx.uicomponents.util;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AudioFrameResult;

@Component
public class AudioRmsCalculator {

    public double calculateRms(AudioFrameResult frame, int channelNumber) {
        if (frame.getNumberSamples() == 0) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < frame.getNumberSamples(); ++i) {
            double sample = frame.getRescaledSample(channelNumber, 1, frame.getSamplePerSecond(), i);
            result += (sample * sample);
        }
        double rms = Math.sqrt((double) result / (frame.getNumberSamples()));
        return rms;
    }

}

package com.helospark.tactview.core.util;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AudioFrameResult;

@Component
public class AudioRmsCalculator {

    public double calculateRms(AudioFrameResult frame, int channelNumber) {
        if (frame.getNumberSamples() == 0) {
            return 0;
        }
        double result = 0;
        for (int i = 0; i < frame.getNumberSamples(); ++i) {
            double sample = frame.getNormalizedSampleAt(channelNumber, i);
            result += (sample * sample);
        }
        return Math.sqrt(result / (frame.getNumberSamples()));
    }

}

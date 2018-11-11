package com.helospark.tactview.ui.javafx.audio;

import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.AudioFrameResult;

@Configuration
public class JavaByteArrayConverter {

    public byte[] convert(AudioFrameResult audioFrameResult, int bits, int samples, int channels) {
        double length = audioFrameResult.getLength().getSeconds().doubleValue();
        int numberOfSamplesPerChannel = (int) (bits / 8 * samples * length);
        int numberOfChannels = channels;
        int originalNumberOfChannels = audioFrameResult.getChannels().size();
        int fullLength = numberOfChannels * numberOfSamplesPerChannel;
        byte[] result = new byte[fullLength];

        for (int sample = 0; sample < numberOfSamplesPerChannel; ++sample) {
            for (int channel = 0; channel < numberOfChannels; ++channel) {
                if (originalNumberOfChannels < originalNumberOfChannels) {
                    int rescaledSample = audioFrameResult.getRescaledSample(channel, bits, samples, sample);
                    result[sample * numberOfChannels + channel] = (byte) rescaledSample;
                } else {
                    result[sample * numberOfChannels + channel] = 0;
                }
            }
        }

        return result;
    }

}

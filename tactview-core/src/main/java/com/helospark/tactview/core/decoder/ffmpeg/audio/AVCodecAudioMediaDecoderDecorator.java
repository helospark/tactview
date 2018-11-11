package com.helospark.tactview.core.decoder.ffmpeg.audio;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.timeline.TimelineLength;

@Component
public class AVCodecAudioMediaDecoderDecorator implements AudioMediaDecoder {
    private AVCodecBasedAudioMediaDecoderImplementation implementation;
    private MediaCache mediaCache;

    public AVCodecAudioMediaDecoderDecorator(AVCodecBasedAudioMediaDecoderImplementation implementation) {
        this.implementation = implementation;
    }

    @Override
    public MediaDataResponse readFrames(AudioMediaDataRequest request) {
        AVCodecAudioRequest nativeRequest = new AVCodecAudioRequest();

        implementation.readAudio(null);

        return null;
    }

    @Override
    public AudioMediaMetadata readMetadata(File file) {
        AVCodecAudioMetadataResponse readMetadata = implementation.readMetadata(file.getAbsolutePath());
        return AudioMediaMetadata.builder()
                .withChannels(readMetadata.channels)
                .withSampleRate(readMetadata.sampleRate)
                .withBytesPerSample(readMetadata.bytesPerSample)
                .withLength(TimelineLength.ofMicroseconds(readMetadata.lengthInMicroseconds))
                .build();
    }

}

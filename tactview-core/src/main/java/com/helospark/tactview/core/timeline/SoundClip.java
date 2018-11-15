package com.helospark.tactview.core.timeline;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;

public class SoundClip extends AudibleTimelineClip {
    private AudioMediaDecoder mediaDecoder;
    private AudioMediaSource backingSource;
    private TimelinePosition startPosition;

    public SoundClip(AudioMediaMetadata mediaMetadata, AudioMediaDecoder mediaDecoder,
            AudioMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(new TimelineInterval(startPosition, length), mediaMetadata);
        this.mediaDecoder = mediaDecoder;
        this.backingSource = backingSource;
        this.startPosition = startPosition;
    }

    @Override
    public AudioFrameResult requestAudioFrame(AudioRequest audioRequest) {
        AudioMediaDataRequest request = AudioMediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withMetadata(mediaMetadata)
                .withStart(audioRequest.getPosition().from(interval.getStartPosition()))
                .withExpectedBytesPerSample(mediaMetadata.getBytesPerSample())
                .withExpectedSampleRate(mediaMetadata.getSampleRate()) // this could be scaled if processing is too slow
                .withExpectedChannels(mediaMetadata.getChannels())
                .withLength(audioRequest.getLength())
                .build();

        List<ByteBuffer> data = backingSource.decoder.readFrames(request).getFrames();

        AudioFrameResult result = new AudioFrameResult(data, mediaMetadata.getSampleRate(), mediaMetadata.getBytesPerSample());

        return result;
    }

    @Override
    public AudioMediaDecoder getMediaDecoder() {
        return mediaDecoder;
    }

    public AudioMediaSource getBackingSource() {
        return backingSource;
    }

    @Override
    protected TimelineClip cloneClip() {
        return new SoundClip(mediaMetadata, mediaDecoder, backingSource, interval.getStartPosition(), interval.getLength());
    }

}

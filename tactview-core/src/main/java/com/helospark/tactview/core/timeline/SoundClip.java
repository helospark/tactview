package com.helospark.tactview.core.timeline;

import java.io.File;
import java.nio.ByteBuffer;

import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;

public class SoundClip extends AudibleTimelineClip {
    private AudioMediaMetadata mediaMetadata;
    private AudioMediaDecoder mediaDecoder;
    private AudioMediaSource backingSource;
    private TimelinePosition startPosition;

    public SoundClip(AudioMediaMetadata mediaMetadata, AudioMediaDecoder mediaDecoder,
            AudioMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(new TimelineInterval(startPosition, length));
        this.mediaMetadata = mediaMetadata;
        this.mediaDecoder = mediaDecoder;
        this.backingSource = backingSource;
        this.startPosition = TimelinePosition.ofZero();
    }

    @Override
    public ByteBuffer requestAudioFrame(TimelinePosition position, int sampleRate, int channel) {
        AudioMediaDataRequest request = AudioMediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withMetadata(mediaMetadata)
                .withStart(position)
                .build();
        return backingSource.decoder.readFrames(request).getFrames().get(0);
    }

    @Override
    public AudioMediaDecoder getMediaMetadata() {
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

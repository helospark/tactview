package com.helospark.tactview.core.timeline;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.audio.AVCodecAudioMediaDecoderDecorator;
import com.helospark.tactview.core.util.StaticObjectMapper;

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

    public SoundClip(AudioMediaMetadata metadata, AVCodecAudioMediaDecoderDecorator mediaDecoder, AudioMediaSource videoSource, JsonNode savedClip) {
        super(metadata, savedClip);
        this.mediaDecoder = mediaDecoder;
        this.backingSource = videoSource;
        this.startPosition = StaticObjectMapper.toValue(savedClip, "startPosition", TimelinePosition.class);
    }

    @Override
    public AudioFrameResult requestAudioFrameInternal(AudioRequest audioRequest) {
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
    public TimelineClip cloneClip() {
        return new SoundClip(mediaMetadata, mediaDecoder, backingSource, interval.getStartPosition(), interval.getLength());
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        savedContent.put("startPosition", savedContent);
    }

}

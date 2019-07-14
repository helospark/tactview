package com.helospark.tactview.core.timeline;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.CloneRequestMetadata;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.audio.AVCodecAudioMediaDecoderDecorator;

public class SoundClip extends AudibleTimelineClip {
    private AudioMediaDecoder mediaDecoder;
    private AudioMediaSource backingSource;

    public SoundClip(AudioMediaMetadata mediaMetadata, AudioMediaDecoder mediaDecoder,
            AudioMediaSource backingSource, TimelinePosition startPosition, TimelineLength length) {
        super(new TimelineInterval(startPosition, length), mediaMetadata);
        this.mediaDecoder = mediaDecoder;
        this.backingSource = backingSource;
    }

    public SoundClip(SoundClip soundClip, CloneRequestMetadata cloneRequestMetadata) {
        super(soundClip, cloneRequestMetadata);
        this.mediaDecoder = soundClip.mediaDecoder;
        this.backingSource = soundClip.backingSource;
    }

    public SoundClip(AudioMediaMetadata metadata, AVCodecAudioMediaDecoderDecorator mediaDecoder, AudioMediaSource videoSource, JsonNode savedClip, LoadMetadata loadMetadata) {
        super(metadata, savedClip, loadMetadata);
        this.mediaDecoder = mediaDecoder;
        this.backingSource = videoSource;
    }

    @Override
    public AudioFrameResult requestAudioFrameInternal(AudioRequest audioRequest) {
        AudioMediaDataRequest request = AudioMediaDataRequest.builder()
                .withFile(new File(backingSource.backingFile))
                .withMetadata(mediaMetadata)
                .withStart(audioRequest.getPosition().from(interval.getStartPosition()).add(renderOffset))
                .withExpectedBytesPerSample(audioRequest.getBytesPerSample())
                .withExpectedSampleRate(audioRequest.getSampleRate())
                .withExpectedChannels(audioRequest.getNumberOfChannels())
                .withLength(audioRequest.getLength())
                .build();

        List<ByteBuffer> data = backingSource.decoder.readFrames(request).getFrames();

        AudioFrameResult result = new AudioFrameResult(data, audioRequest.getSampleRate(), audioRequest.getBytesPerSample());

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
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new SoundClip(this, cloneRequestMetadata);
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent) {
        savedContent.put("backingFile", backingSource.getBackingFile());
    }

}

package com.helospark.tactview.core.timeline;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.decoder.AudioMediaDataRequest;
import com.helospark.tactview.core.decoder.AudioMediaDecoder;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.audio.AVCodecAudioMediaDecoderDecorator;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;

public class SoundClip extends AudibleTimelineClip {
    private final AudioMediaDecoder mediaDecoder;
    private final AudioMediaSource backingSource;

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
                .withStart(audioRequest.getPosition())
                .withExpectedBytesPerSample(audioRequest.getBytesPerSample())
                .withExpectedSampleRate(audioRequest.getSampleRate())
                .withExpectedChannels(audioRequest.getNumberOfChannels())
                .withLength(audioRequest.getLength())
                .build();

        List<ByteBuffer> data = backingSource.decoder.readFrames(request).getFrames();

        AudioFrameResult result = new AudioFrameResult(data, audioRequest.getSampleRate(), audioRequest.getBytesPerSample());

        return result;
    }

    public AudioMediaSource getBackingSource() {
        return backingSource;
    }

    @Override
    public TimelineClip cloneClip(CloneRequestMetadata cloneRequestMetadata) {
        return new SoundClip(this, cloneRequestMetadata);
    }

    @Override
    protected void generateSavedContentInternal(Map<String, Object> savedContent, SaveMetadata saveMetadata) {
        if (saveMetadata.isPackageAllContent()) {
            String fullBackingFile = new File(backingSource.getBackingFile()).getName();
            String copiedFileName = "data/" + this.getId() + "/" + fullBackingFile;
            saveMetadata.getFilesToCopy().put(copiedFileName, backingSource.getBackingFile());
            savedContent.put("backingFile", SaveMetadata.LOCALLY_SAVED_SOURCE_PREFIX + copiedFileName);
        } else {
            savedContent.put("backingFile", backingSource.getBackingFile());
        }
    }

    @Override
    protected void initializeValueProvider() {
        super.initializeValueProvider();
    }

    @Override
    protected List<ValueProviderDescriptor> getDescriptorsInternal() {
        return super.getDescriptorsInternal();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    // this is really a cut instead of a resize, but on UI it shows the exact same way as resize
    // maximum size a clip can be is the size of the videoclip
    @Override
    public TimelineInterval getIntervalAfterRescaleTo(boolean left, TimelinePosition position) {
        return intervalAfterResizeAsCut(left, position, mediaMetadata.getLength(), false);
    }

    @Override
    public void resize(boolean left, TimelineInterval position) {
        resizeAsCut(left, position);
    }
}

package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.audio.AVCodecAudioMediaDecoderDecorator;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AudioMediaSource;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.SoundClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

@Component
@Order(value = Integer.MAX_VALUE)
public class AVCodecSoundClipFactory implements ClipFactory {
    private AVCodecAudioMediaDecoderDecorator mediaDecoder;

    public AVCodecSoundClipFactory(AVCodecAudioMediaDecoderDecorator mediaDecoder) {
        this.mediaDecoder = mediaDecoder;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile() && hasAudioStream(request);
    }

    private boolean hasAudioStream(AddClipRequest request) {
        return readMetadataFromFile(request.getFile()).isValid();
    }

    @Override
    public AudioMediaMetadata readMetadata(AddClipRequest request) {
        return readMetadataFromFile(request.getFile());
    }

    private AudioMediaMetadata readMetadataFromFile(File file) {
        return mediaDecoder.readMetadata(file);
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        File file = request.getFile();
        TimelinePosition position = request.getPosition();
        AudioMediaMetadata metadata = mediaDecoder.readMetadata(file);
        AudioMediaSource videoSource = new AudioMediaSource(file, mediaDecoder);
        SoundClip result = new SoundClip(metadata, mediaDecoder, videoSource, position, metadata.getLength());
        result.setCreatorFactoryId(this.getId());
        return result;
    }

    @Override
    public String getId() {
        return "soundClipFactory";
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        File file = loadMetadata.resolveFilePath(savedClip.get("backingFile").asText());
        AudioMediaMetadata metadata = mediaDecoder.readMetadata(file);
        AudioMediaSource videoSource = new AudioMediaSource(file, mediaDecoder);

        return new SoundClip(metadata, mediaDecoder, videoSource, savedClip, loadMetadata);
    }

}

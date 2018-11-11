package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.AudioMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.audio.AVCodecAudioMediaDecoderDecorator;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AudioMediaSource;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.SoundClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;

@Component
public class AVCodecSoundClipFactory implements ClipFactory {
    private AVCodecAudioMediaDecoderDecorator mediaDecoder;

    public AVCodecSoundClipFactory(AVCodecAudioMediaDecoderDecorator mediaDecoder) {
        this.mediaDecoder = mediaDecoder;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        // TODO: real
        return request.containsFile() && request.getFile().getAbsolutePath().contains("_sound");
    }

    @Override
    public AudioMediaMetadata readMetadata(AddClipRequest request) {
        return mediaDecoder.readMetadata(request.getFile());
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        File file = request.getFile();
        TimelinePosition position = request.getPosition();
        AudioMediaMetadata metadata = mediaDecoder.readMetadata(file);
        AudioMediaSource videoSource = new AudioMediaSource(file, mediaDecoder);
        SoundClip result = new SoundClip(metadata, mediaDecoder, videoSource, position, metadata.getLength());
        return result;
    }

}

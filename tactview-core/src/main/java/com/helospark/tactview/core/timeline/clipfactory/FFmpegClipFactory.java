package com.helospark.tactview.core.timeline.clipfactory;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.ffmpeg.FFmpegBasedMediaDecoderDecorator;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VideoClip;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.effect.rotate.RotateService;
import com.helospark.tactview.core.util.FileTypeProberUtil;

@Component
public class FFmpegClipFactory implements ClipFactory {
    private FFmpegBasedMediaDecoderDecorator mediaDecoder;
    private RotateService rotateService;

    public FFmpegClipFactory(FFmpegBasedMediaDecoderDecorator mediaDecoder, RotateService rotateService) {
        this.mediaDecoder = mediaDecoder;
        this.rotateService = rotateService;
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        File file = request.getFile();
        TimelinePosition position = request.getPosition();
        VideoMetadata metadata = readMetadataFromFile(file);
        VisualMediaSource videoSource = new VisualMediaSource(file, mediaDecoder);
        VideoClip result = new VideoClip(metadata, videoSource, position, metadata.getLength(), rotateService);
        result.setCreatorFactoryId(getId());
        return result;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.containsFile() &&
                !FileTypeProberUtil.isImageByContentType(request.getFile()) && // FFmpeg handles some image formats as well
                !FileTypeProberUtil.isAudioByContentType(request.getFile()) && // FFmpeg opens cover images in audio file, example mp3, but it should not create channel
                hasVideoStream(request.getFile());
    }

    private boolean hasVideoStream(File file) {
        return readMetadataFromFile(file).isValid();
    }

    @Override
    public VisualMediaMetadata readMetadata(AddClipRequest request) {
        return readMetadataFromFile(request.getFile());
    }

    public VideoMetadata readMetadataFromFile(File file) {
        return mediaDecoder.readMetadata(file);
    }

    @Override
    public String getId() {
        return "ffmpegClipFactory";
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata loadMetadata) {
        File file = loadMetadata.resolveFilePath(savedClip.get("backingFile").asText());

        VideoMetadata metadata = mediaDecoder.readMetadata(file);
        VisualMediaSource videoSource = new VisualMediaSource(file, mediaDecoder);

        return new VideoClip(metadata, videoSource, savedClip, loadMetadata, rotateService);
    }

}

package com.helospark.tactview.core.timeline.clipfactory;

import static com.helospark.tactview.core.timeline.AddClipRequestMetaDataKey.FPS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.decoder.MediaMetadata;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.imagesequence.ImageSequenceDecoderDecorator;
import com.helospark.tactview.core.decoder.opencv.ImageMetadataRequest;
import com.helospark.tactview.core.decoder.opencv.ImageMetadataResponse;
import com.helospark.tactview.core.decoder.opencv.ImageMediaLoader;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactory;
import com.helospark.tactview.core.timeline.ImageSequenceVideoClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualMediaSource;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileHolder;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;

@Component
public class ImageSequenceClipFactory implements ClipFactory {
    private FileNamePatternToFileResolverService fileNamePatternToFileResolverService;
    private ImageMediaLoader implementation;
    private ImageSequenceDecoderDecorator imageSequenceDecoder;

    public ImageSequenceClipFactory(FileNamePatternToFileResolverService fileNamePatternToFileResolverService, ImageMediaLoader implementation, ImageSequenceDecoderDecorator imageSequenceDecoder) {
        this.fileNamePatternToFileResolverService = fileNamePatternToFileResolverService;
        this.implementation = implementation;
        this.imageSequenceDecoder = imageSequenceDecoder;
    }

    @Override
    public boolean doesSupport(AddClipRequest request) {
        return request.getAddClipRequestMetadataKey().containsKey(FPS) &&
                fileNamePatternToFileResolverService.filenamePatternToFileResolver(request.getFilePath()).size() > 0;
    }

    @Override
    public MediaMetadata readMetadata(AddClipRequest request) {
        BigDecimal fps = (BigDecimal) request.getAddClipRequestMetadataKey().get(FPS);
        String backingFiles = request.getFilePath();

        return readMetadataFromFileAndFps(fps, backingFiles);
    }

    @Override
    public TimelineClip createClip(AddClipRequest request) {
        String filePath = request.getFilePath();
        TimelinePosition position = request.getPosition();
        VideoMetadata metadata = (VideoMetadata) readMetadata(request);
        VisualMediaSource videoSource = new VisualMediaSource(filePath, imageSequenceDecoder);
        ImageSequenceVideoClip result = new ImageSequenceVideoClip(metadata, videoSource, position, metadata.getLength());
        result.setCreatorFactoryId(getId());
        return result;
    }

    @Override
    public String getId() {
        return "imageSequenceClipFactory";
    }

    @Override
    public TimelineClip restoreClip(JsonNode savedClip, LoadMetadata metadata) {
        String file = savedClip.get("backingFile").asText();
        BigDecimal fps = new BigDecimal(savedClip.get("fps").asDouble());

        VideoMetadata mediaMetadata = (VideoMetadata) readMetadataFromFileAndFps(fps, file);

        VisualMediaSource videoSource = new VisualMediaSource(file, imageSequenceDecoder);

        return new ImageSequenceVideoClip(mediaMetadata, videoSource, savedClip, metadata);
    }

    private MediaMetadata readMetadataFromFileAndFps(BigDecimal fps, String backingFiles) {
        List<FileHolder> files = fileNamePatternToFileResolverService.filenamePatternToFileResolver(backingFiles);
        FileHolder firstImage = files.get(0);
        FileHolder lastImage = files.get(files.size() - 1);

        BigDecimal length = BigDecimal.valueOf(lastImage.getFrameIndex()).divide(fps, 10, RoundingMode.HALF_UP);

        ImageMetadataRequest imageRequest = new ImageMetadataRequest();
        imageRequest.path = firstImage.getFile().getAbsolutePath();

        ImageMetadataResponse imageMetadata = implementation.readMetadata(imageRequest);

        VideoMetadata metadata = VideoMetadata.builder()
                .withWidth(imageMetadata.width)
                .withHeight(imageMetadata.height)
                .withFps(fps.doubleValue())
                .withLength(new TimelineLength(length))
                .build();

        return metadata;
    }

}

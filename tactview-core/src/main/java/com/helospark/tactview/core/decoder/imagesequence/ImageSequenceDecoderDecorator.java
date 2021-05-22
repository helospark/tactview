package com.helospark.tactview.core.decoder.imagesequence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaDataFrame;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.decoder.opencv.ImageMediaLoader;
import com.helospark.tactview.core.decoder.opencv.ImageRequest;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileHolder;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;

@Component
public class ImageSequenceDecoderDecorator implements VisualMediaDecoder {
    private ImageMediaLoader implementation;
    private MediaCache mediaCache;
    private MemoryManager memoryManager;
    private FileNamePatternToFileResolverService fileNamePatternService;

    public ImageSequenceDecoderDecorator(ImageMediaLoader implementation, MediaCache mediaCache, MemoryManager memoryManager, FileNamePatternToFileResolverService fileNamePatternService) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
        this.memoryManager = memoryManager;
        this.fileNamePatternService = fileNamePatternService;
    }

    @Override
    public MediaDataResponse readFrames(VideoMediaDataRequest request) {
        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        BigDecimal frameTime = BigDecimal.ONE.divide(new BigDecimal(metadata.getFps()), 10, RoundingMode.HALF_UP);
        int startFrame = request.getStart().getSeconds().divide(frameTime, 0, RoundingMode.HALF_DOWN).intValue();

        Optional<MediaDataFrame> foundElement = mediaCache.findInCacheAndClone(createCacheKey(request), request.getStart().getSeconds());

        if (foundElement.isPresent()) {
            return new MediaDataResponse(List.of(foundElement.get().frame));
        } else {
            return loadAndCacheFrame(request, startFrame, frameTime);
        }
    }

    private MediaDataResponse loadAndCacheFrame(VideoMediaDataRequest request, int frame, BigDecimal frameTime) {
        List<FileHolder> files = fileNamePatternService.filenamePatternToFileResolver(request.getFilePath());
        BigDecimal frameStart = BigDecimal.valueOf(frame).multiply(frameTime).setScale(3, RoundingMode.HALF_UP);
        BigDecimal frameEnd = frameStart.add(frameTime);

        int actualFrameSequenceNumber = frame + files.get(0).getFrameIndex(); // ex. frame might start from 1

        Optional<FileHolder> foundFrame = findFrame(files, actualFrameSequenceNumber);

        if (foundFrame.isPresent()) {
            ImageRequest imageRequest = new ImageRequest();
            imageRequest.width = request.getWidth();
            imageRequest.height = request.getHeight();
            imageRequest.path = foundFrame.get().getFile().getAbsolutePath();
            imageRequest.data = memoryManager.requestBuffer(imageRequest.width * imageRequest.height * 4);
            ByteBuffer data = readImageFromFile(imageRequest);

            mediaCache.cacheMedia(createCacheKey(request), new MediaHashValue(List.of(new MediaDataFrame(data, frameStart)), frameEnd, frameStart));

            return new MediaDataResponse(data);
        } else {
            ByteBuffer emptyBuffer = memoryManager.requestBuffer(request.getWidth() * request.getHeight() * 4);
            return new MediaDataResponse(emptyBuffer);
        }
    }

    private ByteBuffer readImageFromFile(ImageRequest imageRequest) {
        readImage(imageRequest);
        return imageRequest.data;
    }

    private void readImage(ImageRequest imageRequest) {
        implementation.readImage(imageRequest);
    }

    private Optional<FileHolder> findFrame(List<FileHolder> files, int frameToSearchFor) {
        int startIndex = 0;
        int endIndex = files.size() - 1;

        while (startIndex <= endIndex) {
            int index = (startIndex + endIndex) / 2;
            FileHolder holder = files.get(index);
            int foundIndex = holder.getFrameIndex();
            if (foundIndex == frameToSearchFor) {
                return Optional.of(holder);
            } else if (foundIndex > frameToSearchFor) {
                endIndex = index - 1;
            } else {
                startIndex = index + 1;
            }
        }

        return Optional.empty();
    }

    private String createCacheKey(VideoMediaDataRequest request) {
        return request.getFilePath() + "_" + request.getWidth() + "_" + request.getHeight();
    }

}

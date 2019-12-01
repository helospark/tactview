package com.helospark.tactview.core.decoder.imagesequence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VideoMetadata;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.decoder.opencv.ImageMediaLoader;
import com.helospark.tactview.core.decoder.opencv.ImageRequest;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileHolder;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;

public class ImageSequenceDecoderDecorator implements VisualMediaDecoder {
    private ImageMediaLoader imageMediaLoader;
    private MediaCache mediaCache;
    private MemoryManager memoryManager;
    private FileNamePatternToFileResolverService fileNamePatternService;

    public ImageSequenceDecoderDecorator(ImageMediaLoader imageMediaLoader, MediaCache mediaCache, MemoryManager memoryManager, FileNamePatternToFileResolverService fileNamePatternService) {
        this.imageMediaLoader = imageMediaLoader;
        this.mediaCache = mediaCache;
        this.memoryManager = memoryManager;
        this.fileNamePatternService = fileNamePatternService;
    }

    @Override
    public MediaDataResponse readFrames(VideoMediaDataRequest request) {
        VideoMetadata metadata = (VideoMetadata) request.getMetadata();
        int startFrame = request.getStart().getSeconds().multiply(new BigDecimal(metadata.getFps())).setScale(2, RoundingMode.HALF_UP).intValue();

        Optional<MediaHashValue> foundElement = mediaCache.findInCache(createCacheKey(request), startFrame);

        if (foundElement.isPresent()) {
            return copyOf(foundElement.get());
        } else {
            return loadAndCacheFrame(request, startFrame);
        }
    }

    private MediaDataResponse loadAndCacheFrame(VideoMediaDataRequest request, int frame) {
        List<FileHolder> files = fileNamePatternService.filenamePatternToFileResolver(request.getFilePath());

        int actualFrameSequenceNumber = frame + files.get(0).getFrameIndex(); // ex. frame might start from 1

        Optional<FileHolder> foundFrame = findFrame(files, actualFrameSequenceNumber);

        if (foundFrame.isPresent()) {
            ImageRequest imageRequest = new ImageRequest();
            imageRequest.width = request.getWidth();
            imageRequest.height = request.getHeight();
            imageRequest.path = foundFrame.get().getFile().getAbsolutePath();
            imageRequest.data = memoryManager.requestBuffer(imageRequest.width * imageRequest.height * 4);
            ByteBuffer data = readImageFromFile(imageRequest);

            mediaCache.cacheMedia(createCacheKey(request), new MediaHashValue(frame, frame + 1, List.of(data)));

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
        imageMediaLoader.readImage(imageRequest);
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

    private MediaDataResponse copyOf(MediaHashValue mediaHashValue) {
        ByteBuffer frame = mediaHashValue.frames.get(0);
        ByteBuffer result = memoryManager.requestBuffer(frame.capacity());
        for (int i = 0; i < frame.capacity(); ++i) {
            result.put(i, frame.get(i));
        }
        return new MediaDataResponse(List.of(result));
    }

    private String createCacheKey(VideoMediaDataRequest request) {
        return request.getFilePath() + "_" + request.getWidth() + "_" + request.getHeight();
    }

}

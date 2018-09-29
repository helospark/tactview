package com.helospark.tactview.core.decoder.opencv;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.MediaDataRequest;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.MediaDecoder;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashKey;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class OpenCvImageDecorderDecorator implements MediaDecoder {
    private OpenCvMediaDecoder implementation;
    private MediaCache mediaCache;

    public OpenCvImageDecorderDecorator(OpenCvMediaDecoder implementation, MediaCache mediaCache) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
    }

    @Override
    @Cacheable
    public ImageMetadata readMetadata(File file) {
        ImageMetadataRequest request = new ImageMetadataRequest();
        request.path = file.getAbsolutePath();
        ImageMetadataResonse result = implementation.readMetadata(request);

        return ImageMetadata.builder()
                .withWidth(result.width)
                .withHeight(result.height)
                .withLength(TimelineLength.ofMillis(10000))
                .build();
    }

    @Override
    public MediaDataResponse readFrames(MediaDataRequest request) {
        MediaHashKey cacheKey = new MediaHashKey(request.getFile().getAbsolutePath(), request.getWidth(), request.getHeight());
        Optional<MediaHashValue> result = mediaCache.findInCache(cacheKey, 0);

        ByteBuffer image;
        if (result.isPresent()) {
            image = result.get().frames.get(0);
        } else {
            ImageRequest imageRequest = new ImageRequest();

            imageRequest.data = ByteBuffer.allocateDirect(request.getWidth() * request.getHeight() * 4);
            imageRequest.width = request.getWidth();
            imageRequest.height = request.getHeight();
            imageRequest.path = request.getFile().getAbsolutePath();

            implementation.readImage(imageRequest);
            image = imageRequest.data;

            mediaCache.cacheMedia(cacheKey, new MediaHashValue(0, Collections.singletonList(image)));
        }

        List<ByteBuffer> frames = new ArrayList<>();
        for (int i = 0; i < request.getNumberOfFrames(); ++i) {
            frames.add(image); // todo: clone?
        }

        return new MediaDataResponse(frames);
    }

}

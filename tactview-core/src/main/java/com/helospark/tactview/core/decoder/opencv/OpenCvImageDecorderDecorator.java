package com.helospark.tactview.core.decoder.opencv;

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.ImageMetadata;
import com.helospark.tactview.core.decoder.MediaDataResponse;
import com.helospark.tactview.core.decoder.VideoMediaDataRequest;
import com.helospark.tactview.core.decoder.VisualMediaDecoder;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaDataFrame;
import com.helospark.tactview.core.decoder.framecache.MediaCache.MediaHashValue;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class OpenCvImageDecorderDecorator implements VisualMediaDecoder {
    public TimelineLength imageLength = TimelineLength.ofMillis(10000);
    private ImageMediaLoader implementation;
    private MediaCache mediaCache;

    public OpenCvImageDecorderDecorator(ImageMediaLoader implementation, MediaCache mediaCache) {
        this.implementation = implementation;
        this.mediaCache = mediaCache;
    }

    @PreferenceValue(name = "Default image clip length (ms)", defaultValue = "10000", group = "Clip")
    public void setImageClipLength(Integer lengthInMs) {
        imageLength = TimelineLength.ofMillis(lengthInMs);
    }

    @Cacheable
    public ImageMetadata readMetadata(File file) {
        ImageMetadataRequest request = new ImageMetadataRequest();
        request.path = file.getAbsolutePath();
        ImageMetadataResponse result = implementation.readMetadata(request);

        return ImageMetadata.builder()
                .withWidth(result.width)
                .withHeight(result.height)
                .withLength(imageLength)
                .build();
    }

    @Override
    public MediaDataResponse readFrames(VideoMediaDataRequest request) {
        String cacheKey = request.getFile().getAbsolutePath() + " " + request.getWidth() + " " + request.getHeight();
        Optional<MediaDataFrame> result = mediaCache.findInCacheAndClone(cacheKey, BigDecimal.ZERO);

        ByteBuffer image;
        if (result.isPresent()) {
            image = result.get().frame;
        } else {
            ImageRequest imageRequest = new ImageRequest();

            imageRequest.width = request.getWidth();
            imageRequest.height = request.getHeight();
            imageRequest.path = request.getFile().getAbsolutePath();

            implementation.readImage(imageRequest);
            image = imageRequest.data;

            mediaCache.cacheMedia(cacheKey, new MediaHashValue(Collections.singletonList(new MediaDataFrame(image, BigDecimal.ZERO)), BigDecimal.ONE, BigDecimal.ZERO));
        }

        return new MediaDataResponse(image);
    }

    public TimelineLength getImageLength() {
        return imageLength;
    }

}

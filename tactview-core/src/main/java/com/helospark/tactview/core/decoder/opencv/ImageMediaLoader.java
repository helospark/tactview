package com.helospark.tactview.core.decoder.opencv;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;

@Component
public class ImageMediaLoader {
    private BufferedImageToClipFrameResultConverter converter;
    private ScaleService scaleService;

    public ImageMediaLoader(BufferedImageToClipFrameResultConverter converter, ScaleService scaleService) {
        this.converter = converter;
        this.scaleService = scaleService;
    }

    public ImageMetadataResponse readMetadata(ImageMetadataRequest request) {
        try {
            BufferedImage image = ImageIO.read(new File(request.path));

            if (image == null) {
                throw new RuntimeException("Image metadata cannot be loaded");
            }

            ImageMetadataResponse metadataResponse = new ImageMetadataResponse();

            metadataResponse.width = image.getWidth();
            metadataResponse.height = image.getHeight();

            return metadataResponse;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void readImage(ImageRequest request) {
        try {
            BufferedImage image = ImageIO.read(new File(request.path));
            ReadOnlyClipImage convertedImage = converter.convert(image);

            ScaleRequest scaleRequest = ScaleRequest.builder()
                    .withImage(convertedImage)
                    .withNewWidth(request.width)
                    .withNewHeight(request.height)
                    .build();

            ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);

            GlobalMemoryManagerAccessor.memoryManager.returnBuffer(convertedImage.getBuffer());

            request.data = scaledImage.getBuffer();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

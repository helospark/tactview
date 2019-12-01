package com.helospark.tactview.core.render.proxy;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.decoder.opencv.ImageMediaLoader;
import com.helospark.tactview.core.decoder.opencv.ImageRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleRequest;
import com.helospark.tactview.core.timeline.effect.scale.service.ScaleService;
import com.helospark.tactview.core.timeline.image.ClipImage;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

@Component
public class Lz4ImageMediaLoader implements ImageMediaLoader {
    private LZ4FastDecompressor decompressor;

    private MemoryManager memoryManager;
    private ProxyMetadataHandler proxyMetadataHandler;
    private ScaleService scaleService;

    public Lz4ImageMediaLoader(MemoryManager memoryManager, ProxyMetadataHandler proxyMetadataHandler, ScaleService scaleService) {
        this.memoryManager = memoryManager;
        this.proxyMetadataHandler = proxyMetadataHandler;
        this.scaleService = scaleService;

        LZ4Factory factory = LZ4Factory.fastestInstance();
        decompressor = factory.fastDecompressor();
    }

    @Override
    public void readImage(ImageRequest request) {
        try {
            ProxyCacheMetadata metadata = proxyMetadataHandler.getMetadata(new File(request.path).getParentFile());
            byte[] file = Files.readAllBytes(new File(request.path).toPath());

            ByteBuffer unscaledResult = memoryManager.requestBuffer(metadata.getWidth() * metadata.getHeight() * 4);

            decompressor.decompress(ByteBuffer.wrap(file), unscaledResult);

            if (request.width == metadata.width && request.height == metadata.height) {
                request.data = unscaledResult;
            } else {
                ScaleRequest scaleRequest = ScaleRequest.builder()
                        .withImage(new ClipImage(unscaledResult, metadata.width, metadata.height))
                        .withNewWidth(request.width)
                        .withNewHeight(request.height)
                        .build();

                ClipImage scaledImage = scaleService.createScaledImage(scaleRequest);

                GlobalMemoryManagerAccessor.memoryManager.returnBuffer(unscaledResult);

                request.data = scaledImage.getBuffer();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

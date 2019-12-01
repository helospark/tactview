package com.helospark.tactview.core.decoder.imagesequence;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.decoder.opencv.JavaBasedImageMediaLoader;
import com.helospark.tactview.core.render.proxy.Lz4ImageMediaLoader;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;

@Configuration
public class ImageSequenceDecoderDecoratorConfiguration {

    @Bean
    public ImageSequenceDecoderDecorator regularImageSequenceDecoderDecorator(JavaBasedImageMediaLoader implementation, MediaCache mediaCache, MemoryManager memoryManager,
            FileNamePatternToFileResolverService fileNamePatternService) {
        return new ImageSequenceDecoderDecorator(implementation, mediaCache, memoryManager, fileNamePatternService);

    }

    @Bean
    public ImageSequenceDecoderDecorator lz4ImageSequenceDecoderDecorator(Lz4ImageMediaLoader implementation, MediaCache mediaCache, MemoryManager memoryManager,
            FileNamePatternToFileResolverService fileNamePatternService) {
        return new ImageSequenceDecoderDecorator(implementation, mediaCache, memoryManager, fileNamePatternService);
    }

}

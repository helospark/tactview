package com.helospark.tactview.core.it;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.helospark.lightdi.LightDiContext;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.it.util.IntegrationTestUtil;
import com.helospark.tactview.core.timeline.framemerge.AlphaBlitServiceImpl;
import com.helospark.tactview.core.timeline.framemerge.nativelibrary.AlphaBlendRequest;
import com.helospark.tactview.core.timeline.framemerge.nativelibrary.NativeAlphaBlendService;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.util.memoryoperations.MemoryOperations;

public class PerformanceTest {
    private LightDiContext lightDi;
    private MemoryManager memoryManager;
    private MemoryOperations memoryOperations;
    private NativeAlphaBlendService nativeAlphaBlendService;
    private AlphaBlitServiceImpl alphaBlitServiceImpl;

    @BeforeEach
    public void init() {
        lightDi = IntegrationTestUtil.startContext();
        memoryManager = lightDi.getBean(MemoryManager.class);
        memoryOperations = lightDi.getBean(MemoryOperations.class);
        nativeAlphaBlendService = lightDi.getBean(NativeAlphaBlendService.class);
        alphaBlitServiceImpl = lightDi.getBean(AlphaBlitServiceImpl.class);
    }

    @AfterEach
    public void destroy() {
        lightDi.close();
    }

    // This is very fast and no need to do any optimization.
    @Test
    @Disabled
    public void testClearMemoryThroughput() {
        int sizePerBuffer = 3840 * 2160 * 4;
        int iteration = 7000;

        ByteBuffer memory = memoryManager.requestBuffer(sizePerBuffer);
        long time = System.currentTimeMillis();
        for (int i = 0; i < iteration; ++i) {
            memoryOperations.clearBuffer(memory);
        }
        double timeSecond = (System.currentTimeMillis() - time) / 1000.0;
        double sizeInMb = sizePerBuffer / 1024.0 / 1024.0 * iteration;
        System.out.println("Took " + timeSecond + " " + (sizeInMb / timeSecond) + " MB/s");
        memoryManager.returnBuffer(memory);
    }

    // Both these (Java vs native) are slow and should be optimized further.
    // In 4k, native implementation does about 9.5 FPS, Java impl does 6.5 FPS.
    // Native implementation currently is single threaded, possibly multithreading can increase the perf.
    // Also testing machine is 2c4t, maybe on a CPU with more cores Java could be faster than singlethreaded native impl.
    @Test
    @Disabled
    public void testAlphaBlendThroughput() {
        int iteration = 300;

        int width = 3840;
        int height = 2160;

        ClipImage firstImage = ClipImage.fromSize(width, height);
        ClipImage secondImage = ClipImage.fromSize(width, height);
        Random random = new Random();

        for (int y = 0; y < firstImage.getHeight(); ++y) {
            for (int x = 0; x < firstImage.getWidth(); x += 4) {
                firstImage.setRed(random.nextInt(255), x, y);
                firstImage.setGreen(random.nextInt(255), x, y);
                firstImage.setBlue(random.nextInt(255), x, y);
                firstImage.setAlpha(255, x, y);

                secondImage.setRed(random.nextInt(255), x, y);
                secondImage.setGreen(random.nextInt(255), x, y);
                secondImage.setBlue(random.nextInt(255), x, y);
                secondImage.setAlpha(255, x, y);
            }
        }

        long time = System.currentTimeMillis();
        for (int i = 0; i < iteration; ++i) {
            // Option1:
            // alphaBlitServiceImpl.javaAlphaBlending(firstImage, secondImage, width, height, new NormalBlendModeStrategy(), 1.0);

            // Option2:
            AlphaBlendRequest alphaBlendRequest = new AlphaBlendRequest();
            alphaBlendRequest.alpha = 1.0;
            alphaBlendRequest.foreground = firstImage.getBuffer();
            alphaBlendRequest.backgroundAndResult = secondImage.getBuffer();
            alphaBlendRequest.width = width;
            alphaBlendRequest.height = height;
            nativeAlphaBlendService.normalAlphablend(alphaBlendRequest);
        }

        double timeSecond = (System.currentTimeMillis() - time) / 1000.0;
        double sizeInMb = (width * height * 4) / 1024.0 / 1024.0 * iteration;
        System.out.println("Took " + timeSecond + " " + (sizeInMb / timeSecond) + " MB/s | " + (iteration / timeSecond) + " fps");

        memoryManager.returnBuffer(firstImage.getBuffer());
        memoryManager.returnBuffer(secondImage.getBuffer());
    }

}

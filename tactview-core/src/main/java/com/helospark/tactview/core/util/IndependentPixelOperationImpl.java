package com.helospark.tactview.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

@Component
public class IndependentPixelOperationImpl implements IndependentPixelOperation {
    private ExecutorService workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private int numberOfThreads = Runtime.getRuntime().availableProcessors();

    @Override
    public ClipImage createNewImageWithAppliedTransformation(ReadOnlyClipImage currentFrame, SimplePixelTransformer pixelTransformer) {
        return createNewImageWithAppliedTransformation(currentFrame, List.of(), pixelTransformer);
    }

    @Override
    public ClipImage createNewImageWithAppliedTransformation(ReadOnlyClipImage currentFrame, List<ThreadLocalProvider<?>> threadLocalProviders, SimplePixelTransformer pixelTransformer) {
        ClipImage resultFrame = ClipImage.sameSizeAs(currentFrame);

        int taskSize = resultFrame.getHeight() / numberOfThreads;
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; ++i) {
            int currentIndex = i;
            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                int startIndex = currentIndex * taskSize;
                int endIndex = (currentIndex == numberOfThreads - 1 ? resultFrame.getHeight() : (currentIndex + 1) * taskSize);

                int[] pixelComponents = new int[4];
                int[] resultPixelComponents = new int[4];

                Map<ThreadLocalProvider<?>, Object> threadLocals = threadLocalProviders.stream()
                        .collect(Collectors.toMap(a -> a, a -> a.get()));

                SimplePixelTransformerRequest request = SimplePixelTransformerRequest.builder()
                        .withx(0)
                        .withy(0)
                        .withInput(pixelComponents)
                        .withOutput(resultPixelComponents)
                        .withThreadLocals(threadLocals)
                        .build();
                for (int y = startIndex; y < endIndex; y++) {
                    for (int x = 0; x < currentFrame.getWidth(); x++) {
                        currentFrame.getPixelComponents(pixelComponents, x, y);

                        request.x = x;
                        request.y = y;
                        request.output = resultPixelComponents;
                        request.input = pixelComponents;

                        pixelTransformer.transform(request);

                        resultFrame.setPixel(request.output, x, y);
                    }
                }
            }, workerExecutor);
            futures.add(future);
        }
        CompletableFuture.allOf(toArray(futures)).join(); // block until finished
        return resultFrame;
    }

    @Override
    public void executePixelTransformation(int width, int height, BiConsumer<Integer, Integer> consumer) {
        int taskSize = height / numberOfThreads;
        List<CompletableFuture<?>> futures = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; ++i) {
            int currentIndex = i;
            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                int startIndex = currentIndex * taskSize;
                int endIndex = (currentIndex == numberOfThreads - 1 ? height : (currentIndex + 1) * taskSize);
                for (int y = startIndex; y < endIndex; y++) {
                    for (int x = 0; x < width; x++) {
                        consumer.accept(x, y);
                    }
                }
            }, workerExecutor);
            futures.add(future);
        }
        CompletableFuture.allOf(toArray(futures)).join(); // block until finished
    }

    private CompletableFuture<?>[] toArray(List<CompletableFuture<?>> futures) {
        return futures.toArray(new CompletableFuture[futures.size()]);
    }

    @Override
    public void executePixelTransformation(int width, int height, List<ThreadLocalProvider<?>> threadLocalProviders, Consumer<PixelUpdateRequest> consumer) {
        int taskSize = height / numberOfThreads;
        List<CompletableFuture<?>> futures = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; ++i) {
            int currentIndex = i;
            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                int startIndex = currentIndex * taskSize;
                int endIndex = (currentIndex == numberOfThreads - 1 ? height : (currentIndex + 1) * taskSize);

                Map<ThreadLocalProvider<?>, Object> threadLocals = threadLocalProviders.stream()
                        .collect(Collectors.toMap(a -> a, a -> a.get()));

                PixelUpdateRequest request = new PixelUpdateRequest(0, 0, threadLocals);
                for (int y = startIndex; y < endIndex; y++) {
                    for (int x = 0; x < width; x++) {
                        request.x = x;
                        request.y = y;

                        consumer.accept(request);
                    }
                }
            }, workerExecutor);
            futures.add(future);
        }
        CompletableFuture.allOf(toArray(futures)).join(); // block until finished
    }

}

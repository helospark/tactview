package com.helospark.tactview.ui.javafx.render;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.util.messaging.MessageListener;
import com.helospark.tactview.ui.javafx.UiMessagingService;

public class ProgressAdvancer {
    private int numberOfJobs = 1;
    private AtomicInteger currentProgress = new AtomicInteger(0);

    private UiMessagingService messagingService;
    private String id;

    private long startTime = 0;

    public ProgressAdvancer(UiMessagingService messagingService, String id) {
        this.messagingService = messagingService;
        this.id = id;
    }

    public void updateProgress(Consumer<ProgressUpdateInformation> progressConsumer, Runnable doneRunnable) {
        MessageListener<ProgressInitializeMessage> initializedListener = message -> {
            if (id.equals(message.getId())) {
                this.numberOfJobs = message.getAllJobs();
                startTime = System.currentTimeMillis();
            }
        };
        MessageListener<ProgressAdvancedMessage> advancedListener = message -> {
            if (id.equals(message.getId())) {
                currentProgress.getAndAdd(message.getNumberOfJobsDone());
                double percent = (double) currentProgress.get() / numberOfJobs;

                long allTimeTaken = (System.currentTimeMillis() - startTime);
                long expectedRemainingTime;
                if (percent > 0.0) {
                    expectedRemainingTime = (long) ((1.0 / percent) * allTimeTaken);
                } else {
                    expectedRemainingTime = 0;
                }
                double jobsPerSecond = currentProgress.get() / (allTimeTaken / 1000.0);
                progressConsumer.accept(new ProgressUpdateInformation(percent, expectedRemainingTime, jobsPerSecond, allTimeTaken));
            }
        };
        MessageListener<ProgressDoneMessage> doneListener = message -> {
            if (id.equals(message.getId())) {
                long allTimeTaken = (System.currentTimeMillis() - startTime);
                double jobsPerSecond = currentProgress.get() / (allTimeTaken / 1000.0);
                progressConsumer.accept(new ProgressUpdateInformation(1.0, 0L, jobsPerSecond, allTimeTaken));
                doneRunnable.run();
                messagingService.removeListener(ProgressInitializeMessage.class, initializedListener);
                messagingService.removeListener(ProgressAdvancedMessage.class, advancedListener);
                messagingService.removeListener(ProgressDoneMessage.class, this);
            }
        };
        messagingService.register(ProgressInitializeMessage.class, initializedListener);
        messagingService.register(ProgressAdvancedMessage.class, advancedListener);
        messagingService.register(ProgressDoneMessage.class, doneListener);
    }

    public static class ProgressUpdateInformation {
        public double percent;
        public long expectedMilliseconds;
        public long runningTime;
        public double jobsPerSecond;

        public ProgressUpdateInformation(double percent, long expectedMilliseconds, double jobsPerSecond, long runningTime) {
            this.percent = percent;
            this.expectedMilliseconds = expectedMilliseconds;
            this.jobsPerSecond = jobsPerSecond;
            this.runningTime = runningTime;
        }

    }

}

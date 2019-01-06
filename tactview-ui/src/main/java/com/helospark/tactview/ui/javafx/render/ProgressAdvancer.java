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

    public ProgressAdvancer(UiMessagingService messagingService, String id) {
        this.messagingService = messagingService;
        this.id = id;
    }

    public void updateProgress(Consumer<Double> progressConsumer, Runnable doneRunnable) {
        MessageListener<ProgressInitializeMessage> initializedListener = message -> {
            if (id.equals(message.getId())) {
                this.numberOfJobs = message.getAllJobs();
            }
        };
        MessageListener<ProgressAdvancedMessage> advancedListener = message -> {
            if (id.equals(message.getId())) {
                currentProgress.getAndAdd(message.getNumberOfJobsDone());
                progressConsumer.accept((double) currentProgress.get() / numberOfJobs);
            }
        };
        MessageListener<ProgressDoneMessage> doneListener = message -> {
            if (id.equals(message.getId())) {
                progressConsumer.accept(1.0);
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

}

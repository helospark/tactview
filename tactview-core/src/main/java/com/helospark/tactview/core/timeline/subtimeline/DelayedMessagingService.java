package com.helospark.tactview.core.timeline.subtimeline;

import java.util.ArrayList;
import java.util.List;

import com.helospark.tactview.core.util.messaging.MessageListener;
import com.helospark.tactview.core.util.messaging.MessagingService;

// TODO: until I decide how to handle this
public class DelayedMessagingService implements MessagingService {
    private MessagingService messagingService;
    private List<Object> delayedMessages = new ArrayList<>();
    private boolean isDelayed = true;

    public DelayedMessagingService(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override
    public <T> void register(Class<T> messageType, MessageListener<T> listener) {
        messagingService.register(messageType, listener);
    }

    @Override
    public <T> void sendMessage(T message) {
        if (isDelayed) {
            delayedMessages.add(message);
        } else {
            messagingService.sendMessage(message);
        }
    }

    @Override
    public <T> void sendAsyncMessage(T message) {
        if (isDelayed) {
            delayedMessages.add(message);
        } else {
            messagingService.sendAsyncMessage(message);
        }
    }

    @Override
    public void removeListener(Class<?> clazz, Object progressAdvancer) {
        messagingService.removeListener(clazz, progressAdvancer);
    }

    public void stopDelay() {
        this.isDelayed = false;
        //        for (var message : delayedMessages) {
        //            sendMessage(message);
        //        }
    }

}

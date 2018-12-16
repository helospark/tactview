package com.helospark.tactview.ui.javafx;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.messaging.MessageListener;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.application.Platform;

@Component
public class UiMessagingService {
    private MessagingService delegate;

    public UiMessagingService(MessagingService delegate) {
        this.delegate = delegate;
    }

    public <T> void register(Class<T> messageType, MessageListener<T> listener) {
        delegate.register(messageType, e -> {
            if (Platform.isFxApplicationThread()) {
                System.out.println("On application event " + e);
                listener.onMessage(e);
            } else {
                System.out.println("NOT on application event " + e);
                Platform.runLater(() -> listener.onMessage(e));
            }
        });
    }

    public <T> void sendMessage(T message) {
        delegate.sendMessage(message);
    }

    public <T> void sendAsyncMessage(T message) {
        delegate.sendAsyncMessage(message);
    }

}

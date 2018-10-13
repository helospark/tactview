package com.helospark.tactview.core.util.messaging;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;

@Component
public class MessagingService {
    @Slf4j
    private Logger logger;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Map<Class<?>, Queue<MessageListener<?>>> messageListeners = new ConcurrentHashMap<>();

    public <T> void register(Class<T> messageType, MessageListener<T> listener) {
        messageListeners.compute(messageType, (a, value) -> {
            Queue<MessageListener<?>> queueToAddValueTo;
            if (value == null) {
                queueToAddValueTo = new ConcurrentLinkedDeque<>();
            } else {
                queueToAddValueTo = value;
            }
            queueToAddValueTo.offer(listener);
            return queueToAddValueTo;
        });
    }

    public <T> void sendMessage(T message) {
        logger.debug("Sending message {}", message);
        messageListeners.entrySet()
                .stream()
                .filter(clazz -> clazz.getKey().isAssignableFrom(message.getClass()))
                .flatMap(a -> a.getValue().stream())
                .map(a -> (MessageListener<T>) a)
                .forEach(listener -> listener.onMessage(message));
    }

    public <T> void sendAsyncMessage(T message) {
        executorService.execute(() -> {
            try {
                sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

package com.helospark.tactview.core.util.messaging;

public interface MessagingService {

    <T> void register(Class<T> messageType, MessageListener<T> listener);

    <T> void sendMessage(T message);

    <T> void sendAsyncMessage(T message);

    void removeListener(Class<?> clazz, Object progressAdvancer);

}
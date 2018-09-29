package com.helospark.tactview.core.util.messaging;

@FunctionalInterface
public interface MessageListener<T> {

    void onMessage(T message);

}

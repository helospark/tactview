package com.helospark.tactview.core.timeline.subtimeline;

import com.helospark.tactview.core.util.messaging.MessageListener;
import com.helospark.tactview.core.util.messaging.MessagingService;

// TODO: until I decide how to handle this
public class DummyMessagingService implements MessagingService {

    @Override
    public <T> void register(Class<T> messageType, MessageListener<T> listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void sendMessage(T message) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void sendAsyncMessage(T message) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeListener(Class<?> clazz, Object progressAdvancer) {
        // TODO Auto-generated method stub

    }

}

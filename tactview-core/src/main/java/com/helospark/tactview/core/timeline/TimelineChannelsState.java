package com.helospark.tactview.core.timeline;

import java.util.concurrent.CopyOnWriteArrayList;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;

@Component
public class TimelineChannelsState {
    Object fullLock = new Object();

    CopyOnWriteArrayList<TimelineChannel> channels = new CopyOnWriteArrayList<>();

    public TimelineChannelsState cloneAll(CloneRequestMetadata cloneRequestMetadata) {
        TimelineChannelsState result = new TimelineChannelsState();

        channels.stream()
                .map(channel -> channel.cloneChannel(cloneRequestMetadata))
                .forEach(channel -> result.channels.add(channel));

        return result;
    }
}

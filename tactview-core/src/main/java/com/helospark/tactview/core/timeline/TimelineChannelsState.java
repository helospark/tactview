package com.helospark.tactview.core.timeline;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;

@Component
public class TimelineChannelsState {
    Object fullLock = new Object();

    CopyOnWriteArrayList<TimelineChannel> channels = new CopyOnWriteArrayList<>();

    public TimelineChannelsState deepClone(CloneRequestMetadata metadata) {
        List<TimelineChannel> channelsClone = channels.stream()
                .map(a -> a.cloneChannel(metadata))
                .collect(Collectors.toList());

        TimelineChannelsState result = new TimelineChannelsState();
        result.channels = new CopyOnWriteArrayList<>(channelsClone);

        return result;
    }

    public CopyOnWriteArrayList<TimelineChannel> getChannels() {
        return new CopyOnWriteArrayList<>(channels);
    }

    public void setChannels(List<TimelineChannel> channels2) {
        this.channels = new CopyOnWriteArrayList<>(channels2);
    }
}

package com.helospark.tactview.core.timeline;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.helospark.lightdi.annotation.Component;

@Component
public class TimelineManager {
    private List<StatelessVideoEffect> globalEffects;
    private ConcurrentHashMap<Integer, TimelineChannel> channels = new ConcurrentHashMap<>();

    private List<ClipFactory> clipFactoryChain;

    public boolean canAddClipAt(int channelNumber, TimelinePosition position, TimelineLength length) {
        if (channelNumber < 0) {
            throw new IllegalArgumentException("Channel must be greater than 0");
        }
        if (channels.containsKey(channelNumber)) {
            return true;
        }
        TimelineChannel channel = channels.get(channelNumber);
        return channel.canAddResourceAt(position, length);
    }

    public void onResourceAdded(int channelNumber, TimelinePosition position, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(filePath + " does not exists");
        }
        TimelineClip clip = clipFactoryChain.stream()
                .filter(a -> a.doesSupport(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No clip factory found for " + file))
                .createClip(file);
        TimelineChannel channelToAddResourceTo = channels.computeIfAbsent(channelNumber, key -> new TimelineChannel());
        if (channelToAddResourceTo.canAddResourceAt(null, null))
            ;

    }

}

package com.helospark.tactview.core.timeline;

import java.util.concurrent.CopyOnWriteArrayList;

import com.helospark.lightdi.annotation.Component;

@Component
public class TimelineChannelsState {
    Object fullLock = new Object();

    CopyOnWriteArrayList<TimelineChannel> channels = new CopyOnWriteArrayList<>();
}

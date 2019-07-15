package com.helospark.tactview.core.timeline;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.AffectedModifiedIntervalAware;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class GlobalDirtyClipManager {
    private DirtyIntervalList dirtyIntervalList = new DirtyIntervalList();
    private MessagingService messagingService;
    @Slf4j
    private Logger logger;

    public GlobalDirtyClipManager(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(AffectedModifiedIntervalAware.class, message -> {
            message.getAffectedIntervals()
                    .stream()
                    .forEach(a -> {
                        dirtyIntervalList.dirtyInterval(a);
                        logger.debug("Interval {} marked as dirty", a);
                    });
        });
    }

    public long positionLastModified(TimelinePosition position) {
        return dirtyIntervalList.positionLastModified(position);
    }
}

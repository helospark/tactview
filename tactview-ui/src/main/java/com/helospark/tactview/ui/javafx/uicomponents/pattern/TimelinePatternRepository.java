package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.NonIntersectingIntervalList;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.scene.image.Image;

@Component
public class TimelinePatternRepository {
    private static final BigDecimal DELTA = BigDecimal.valueOf(0.0001);
    private static final int MAX_ALLOWED_ENTRIES_PER_CLIP = 10;
    private Map<String, NonIntersectingIntervalList<PatternIntervalAware>> repository = new ConcurrentHashMap<>();
    private MessagingService messagingService;

    public TimelinePatternRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void addAllAndRemoveOldEntriesNotVisible(String clipId, List<Pair<Image, TimelineInterval>> generatedPatterns, double zoom, TimelineInterval visibleInterval) {
        NonIntersectingIntervalList<PatternIntervalAware> list = repository.get(clipId);

        if (generatedPatterns.isEmpty()) {
            return;
        }

        if (list == null) {
            list = new NonIntersectingIntervalList<>();
        }

        for (var pair : generatedPatterns) {
            List<PatternIntervalAware> intersectingElements = list.computeIntersectingIntervals(pair.getRight());
            if (!intersectingElements.isEmpty()) {
                list.removeAll(intersectingElements);
            }
            list.addInterval(new PatternIntervalAware(pair.getLeft(), pair.getRight(), zoom));
        }

        if (list.size() > MAX_ALLOWED_ENTRIES_PER_CLIP) {
            for (int i = 0; i < list.size() && list.size() > MAX_ALLOWED_ENTRIES_PER_CLIP; ++i) {
                if (!list.get(i).interval.intersects(visibleInterval)) {
                    list.removeIndex(i);
                    --i;
                }
            }
        }

        repository.put(clipId, list);
        messagingService.sendAsyncMessage(new TimelinePatternChangedMessage(clipId, TimelinePatternChangedMessage.ChangeType.ADDED));
    }

    public List<PatternIntervalAware> getPatternForClipId(String clipId, TimelineInterval visibleInterval) {
        NonIntersectingIntervalList<PatternIntervalAware> result = repository.get(clipId);
        if (result == null) {
            return Collections.emptyList();
        } else {
            return result.computeIntersectingIntervals(visibleInterval);
        }
    }

    public boolean hasFullyOverlappingClipWithSimilarZoomLevel(String clipId, TimelineInterval interval, double zoom) {
        NonIntersectingIntervalList<PatternIntervalAware> list = repository.get(clipId);

        if (list == null) {
            return false;
        }

        // double precision
        interval = interval.butWithStartPosition(interval.getStartPosition().add(DELTA))
                .butWithEndPosition(interval.getEndPosition().subtract(DELTA));

        List<PatternIntervalAware> intersectingClips = list.computeIntersectingIntervals(interval);

        for (var intersection : intersectingClips) {
            if (intersection.getInterval().getStartPosition().isLessThanOrEqualTo(interval.getStartPosition())
                    && intersection.getInterval().getEndPosition().isGreaterOrEqualToThan(interval.getEndPosition())) {
                if (Math.abs(zoom - intersection.zoom) < 0.1) {
                    return true;
                }
            }
        }
        return false;

    }

    public void clearAll() {
        repository.clear();
        messagingService.sendAsyncMessage(new TimelinePatternChangedMessage("", TimelinePatternChangedMessage.ChangeType.REMOVED)); // TODO: custom message type
    }

}

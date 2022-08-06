package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.RegenerateAllImagePatternsMessage;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.scene.image.Image;

@Component
public class ClipPatternDrawerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClipPatternDrawerListener.class);
    private Map<String, Boolean> blacklistedClips = new ConcurrentHashMap<>();
    private Queue<String> clipRedrawRequestQueue = new ConcurrentLinkedDeque<>();

    private MessagingService messagingService;
    private TimelineImagePatternService timelineImagePatternService;
    private AudioImagePatternService audioImagePatternService;
    private TimelineState timelineState;
    private TimelinePatternRepository timelinePatternRepository;
    private TimelineManagerAccessor timelineAccessor;
    private ScheduledExecutorService scheduledExecutorService;
    @Slf4j
    private Logger logger;

    private boolean patternDrawingEnabled = true;

    public ClipPatternDrawerListener(MessagingService messagingService, TimelineImagePatternService timelineImagePatternService,
            TimelineState timelineState, AudioImagePatternService audioImagePatternService, TimelineManagerAccessor timelineManager,
            TimelinePatternRepository timelinePatternRepository, TimelineManagerAccessor timelineAccessor,
            @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService) {
        this.messagingService = messagingService;
        this.timelineImagePatternService = timelineImagePatternService;
        this.timelineState = timelineState;
        this.audioImagePatternService = audioImagePatternService;
        this.timelinePatternRepository = timelinePatternRepository;
        this.timelineAccessor = timelineAccessor;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @PreferenceValue(name = "Render pattern on clips", defaultValue = "true", group = "Performance")
    public void setImageClipLength(boolean patternDrawingEnabled) {
        this.patternDrawingEnabled = patternDrawingEnabled;
    }

    @PostConstruct
    public void init() {
        startConsumingThread();

        messagingService.register(RegenerateAllImagePatternsMessage.class, message -> {
            timelinePatternRepository.clearAll();
        });
        messagingService.register(KeyframeSuccesfullyAddedMessage.class, message -> {
            Optional<TimelineClip> clip = timelineAccessor.findClipById(message.getContainingElementId());
            if (clip.isPresent()) {
                clipRedrawRequestQueue.offer(message.getContainingElementId());
            }
        });
    }

    private void startConsumingThread() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                Set<String> requestedClipIds = new HashSet<>(clipRedrawRequestQueue);
                while (clipRedrawRequestQueue.size() > 0) {
                    String newElement = clipRedrawRequestQueue.poll();
                    if (newElement != null) {
                        requestedClipIds.add(newElement);
                    }
                }

                for (var channel : timelineAccessor.getChannels()) {
                    for (var clip : channel.getAllClips()) {
                        if (blacklistedClips.containsKey(clip.getId())) {
                            continue;
                        }
                        TimelineLength clipLength = clip.getInterval().getLength();

                        double zoom = timelineState.getZoom();
                        int normalizedClipTime = getBatchSizeFor(zoom);

                        TimelineInterval visibleInterval = TimelineInterval.fromDoubles(timelineState.getTranslateDouble(),
                                timelineState.getTranslateDouble() + timelineState.getTimelineLengthDouble());
                        visibleInterval = visibleInterval.butAddOffset(clip.getInterval().getStartPosition().negate());
                        List<Pair<Image, TimelineInterval>> generatedPatterns = new ArrayList<>();

                        double clipStartPosition = clip.getGlobalInterval().getStartPosition().getSeconds().doubleValue();
                        double clipEndPosition = clip.getGlobalInterval().getLength().getSeconds().doubleValue();
                        double visibleStartPosition = timelineState.getTranslateDouble() - clipStartPosition;

                        double originalStartValue = roundDownToNearest(visibleStartPosition, normalizedClipTime);

                        if (originalStartValue < 0.0) {
                            originalStartValue = 0.0;
                        }

                        double startValue = originalStartValue;
                        while (startValue >= 0.0) {

                            double intervalEnd = startValue + normalizedClipTime;
                            if (intervalEnd > clipEndPosition) {
                                intervalEnd = clipEndPosition;
                            }
                            if (intervalEnd < visibleInterval.getStartPosition().getSeconds().doubleValue()) {
                                break;
                            }

                            double intervalStart = startValue;
                            if (intervalStart < 0) {
                                intervalStart = 0.0;
                            }

                            TimelineInterval intervalInClipSpace = TimelineInterval.fromDoubles(intervalStart, intervalEnd);
                            if (clipRequiresRedraw(requestedClipIds, clip, clipLength, zoom, visibleInterval, intervalInClipSpace)) {
                                Image pattern = updatePattern(clip, intervalInClipSpace, zoom);
                                if (pattern != null) {
                                    generatedPatterns.add(Pair.of(pattern, intervalInClipSpace));
                                }
                            }
                            startValue -= normalizedClipTime;
                        }
                        startValue = originalStartValue + normalizedClipTime;
                        while (startValue < clipEndPosition && startValue < visibleInterval.getEndPosition().getSeconds().doubleValue()) {
                            double intervalEnd = startValue + normalizedClipTime;
                            if (intervalEnd >= clipEndPosition) {
                                intervalEnd = clipEndPosition;
                            }
                            if (intervalEnd - startValue <= 0.0001) {
                                break;
                            }
                            TimelineInterval intervalInGlobalSpace = TimelineInterval.fromDoubles(startValue, intervalEnd);
                            if (clipRequiresRedraw(requestedClipIds, clip, clipLength, zoom, visibleInterval, intervalInGlobalSpace)) {
                                Image pattern = updatePattern(clip, intervalInGlobalSpace, zoom);
                                if (pattern != null) {
                                    generatedPatterns.add(Pair.of(pattern, intervalInGlobalSpace));
                                }
                            }
                            startValue += normalizedClipTime;
                        }
                        timelinePatternRepository.addAllAndRemoveOldEntriesNotVisible(clip.getId(), generatedPatterns, zoom, visibleInterval, clipLength);
                    }
                }

            } catch (Exception e) {
                logger.warn("Image pattern update failed", e);
            }
        }, 10, 1000, TimeUnit.MILLISECONDS);
    }

    private boolean clipRequiresRedraw(Set<String> requestedClipIds, TimelineClip clip, TimelineLength clipLength, double zoom, TimelineInterval visibleInterval,
            TimelineInterval intervalInGlobalSpace) {
        return intervalInGlobalSpace.intersects(visibleInterval)
                && (requestedClipIds.contains(clip.getId())
                        || !timelinePatternRepository.hasFullyOverlappingClipWithSimilarZoomLevel(clip.getId(), intervalInGlobalSpace, zoom, clipLength));
    }

    private double roundDownToNearest(double value, double multiple) {
        return Math.floor(value / multiple) * multiple;
    }

    private int getBatchSizeFor(double zoom) {
        if (zoom < 0.10) {
            return 300;
        } else if (zoom < 0.50) {
            return 200;
        } else if (zoom < 2) {
            return 150;
        } else if (zoom < 1) {
            return 60;
        } else if (zoom < 4) {
            return 30;
        } else if (zoom < 8) {
            return 10;
        }
        return 5;
    }

    private Image updatePattern(TimelineClip clipToUpdate, TimelineInterval interval, double zoom) {
        try {
            int pixelWidth = (int) timelineState.secondsToPixelsWithZoom(interval.getLength());

            if (pixelWidth < 1) {
                return null;
            }
            Image result = updatePatternDelegate(clipToUpdate, interval, zoom, pixelWidth);
            if (result == null) {
                blacklistedClips.put(clipToUpdate.getId(), true);
            }
            return result;
        } catch (Exception e) {
            blacklistedClips.put(clipToUpdate.getId(), true);
            e.printStackTrace();
        }
        return null;
    }

    private Image updatePatternDelegate(TimelineClip clipToUpdate, TimelineInterval interval, double zoom, int pixelWidth) {
        if (patternDrawingEnabled) {
            LOGGER.debug("Generating pattern for clip={} with the local interval={} and zoom={}", clipToUpdate.getId(), interval, zoom);

            double visibleStartPosition = interval.getStartPosition().getSeconds().doubleValue();
            double visibleEndPosition = interval.getEndPosition().getSeconds().doubleValue();

            Image image = null;
            if (clipToUpdate instanceof VisualTimelineClip) {
                VisualTimelineClip videoClip = (VisualTimelineClip) clipToUpdate;
                image = timelineImagePatternService.createTimelinePattern(videoClip, pixelWidth, visibleStartPosition, visibleEndPosition);
            } else if (clipToUpdate instanceof AudibleTimelineClip) {
                AudibleTimelineClip audibleTimelineClip = (AudibleTimelineClip) clipToUpdate;
                image = audioImagePatternService.createAudioImagePattern(audibleTimelineClip, pixelWidth, AudioImagePatternService.DEFAULT_HEIGHT, visibleStartPosition, visibleEndPosition);
            }

            return image;
        }
        return null;
    }

}

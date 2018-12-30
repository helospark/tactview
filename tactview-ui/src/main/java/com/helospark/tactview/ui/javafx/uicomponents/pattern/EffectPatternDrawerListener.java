package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectResizedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeEnabledWasChangedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyRemovedMessage;
import com.helospark.tactview.core.util.ThreadSleep;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.uicomponents.TimelineState;

import javafx.application.Platform;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

@Component
public class EffectPatternDrawerListener {
    private Map<String, EffectPatternUpdateDomain> effectToUpdate = new ConcurrentHashMap<>();
    private Set<EffectPatternUpdateRequest> updateRequests = new ConcurrentSkipListSet<>();
    private volatile boolean running = true;

    private MessagingService messagingService;
    private TimelineEffectPatternService timelineEffectPatternService;
    private TimelineState timelineState;
    @Slf4j
    private Logger logger;

    public EffectPatternDrawerListener(MessagingService messagingService,
            TimelineState timelineState, TimelineEffectPatternService timelineEffectPatternService) {
        this.messagingService = messagingService;
        this.timelineEffectPatternService = timelineEffectPatternService;
        this.timelineState = timelineState;
    }

    @PostConstruct
    public void init() {
        startConsumingThread();

        messagingService.register(EffectAddedMessage.class, message -> {
            effectToUpdate.put(message.getEffectId(), new EffectPatternUpdateDomain(message.getEffect(), timelineState.getZoom()));
            updateRequests.add(new EffectPatternUpdateRequest(message.getEffectId()));
        });
        messagingService.register(EffectRemovedMessage.class, message -> {
            effectToUpdate.remove(message.getEffectId());
        });
        messagingService.register(EffectResizedMessage.class, message -> {
            updateRequests.add(new EffectPatternUpdateRequest(message.getEffectId()));
        });
        messagingService.register(KeyframeSuccesfullyAddedMessage.class, message -> {
            if (effectToUpdate.containsKey(message.getContainingElementId())) {
                updateRequests.add(new EffectPatternUpdateRequest(message.getContainingElementId()));
            }
        });
        messagingService.register(KeyframeSuccesfullyRemovedMessage.class, message -> {
            if (effectToUpdate.containsKey(message.getContainingElementId())) {
                updateRequests.add(new EffectPatternUpdateRequest(message.getContainingElementId()));
            }
        });
        messagingService.register(KeyframeEnabledWasChangedMessage.class, message -> {
            if (effectToUpdate.containsKey(message.getContainerId())) {
                updateRequests.add(new EffectPatternUpdateRequest(message.getContainerId()));
            }
        });
    }

    @PreDestroy
    public void destroy() {
        this.running = false;
    }

    private void startConsumingThread() {
        new Thread(() -> {
            while (running) {
                try {
                    ThreadSleep.sleep(1000);
                    Set<EffectPatternUpdateRequest> clonedRequests = new HashSet<EffectPatternUpdateRequest>(updateRequests);
                    updateRequests.clear();
                    for (var request : clonedRequests) {
                        if (effectToUpdate.containsKey(request.effectId)) {
                            logger.info("Updating timeline image pattern for {}", request.effectId);
                            updatePattern(request);
                        }
                    }
                    double currentZoomLevel = timelineState.getZoom();
                    for (var entry : effectToUpdate.entrySet()) {
                        if (Math.abs(entry.getValue().createdImageAtZoomLevel - currentZoomLevel) > 0.2) {
                            updateRequests.add(new EffectPatternUpdateRequest(entry.getKey()));
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Image pattern update failed", e);
                }
            }
        }).start();
    }

    private void updatePattern(EffectPatternUpdateRequest request) {
        Rectangle rectangle = (Rectangle) timelineState.findEffectById(request.effectId).orElseThrow();
        EffectPatternUpdateDomain clipsToUpdateDomain = effectToUpdate.get(request.effectId);
        double zoom = timelineState.getZoom();

        int width = (int) (rectangle.getWidth() * zoom);
        StatelessEffect clipToUpdate = clipsToUpdateDomain.effect;

        Paint image = new ImagePattern(timelineEffectPatternService.createImagePatternFor(clipToUpdate, width, zoom));

        Platform.runLater(() -> rectangle.setFill(image));
        effectToUpdate.put(request.effectId, clipsToUpdateDomain.butWithZoomLevel(zoom));
    }

    static class EffectPatternUpdateDomain {
        private StatelessEffect effect;
        private double createdImageAtZoomLevel;

        public EffectPatternUpdateDomain(StatelessEffect effect, double createImageAtZoomLevel) {
            this.effect = effect;
            this.createdImageAtZoomLevel = createImageAtZoomLevel;
        }

        public EffectPatternUpdateDomain butWithZoomLevel(double newZoomLevel) {
            return new EffectPatternUpdateDomain(effect, newZoomLevel);
        }

    }

    static class EffectPatternUpdateRequest implements Comparable<EffectPatternUpdateRequest> {
        private String effectId;

        public EffectPatternUpdateRequest(String effectId) {
            this.effectId = effectId;
        }

        @Override
        public int compareTo(EffectPatternUpdateRequest o) {
            return effectId.compareTo(o.effectId);
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof EffectPatternUpdateRequest)) {
                return false;
            }
            EffectPatternUpdateRequest castOther = (EffectPatternUpdateRequest) other;
            return Objects.equals(effectId, castOther.effectId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(effectId);
        }

    }
}

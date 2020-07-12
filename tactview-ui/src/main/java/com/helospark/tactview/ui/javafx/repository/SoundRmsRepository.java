package com.helospark.tactview.ui.javafx.repository;

import java.util.concurrent.ConcurrentHashMap;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class SoundRmsRepository {
    private static final double DEFAULT_RMS_TO_USE = 140.0;
    private ConcurrentHashMap<String, Double> clipToRmsMap = new ConcurrentHashMap<>();

    public SoundRmsRepository(MessagingService messagingService) {
        messagingService.register(ClipRemovedMessage.class, message -> {
            clipToRmsMap.remove(message.getElementId());
        });
    }

    public void setRmsForClip(String clipId, double rms) {
        clipToRmsMap.put(clipId, rms);
    }

    public double getMaxRms() {
        double rms = clipToRmsMap.values()
                .stream()
                .mapToDouble(d -> d)
                .max()
                .orElse(DEFAULT_RMS_TO_USE);
        if (rms > 0.1) {
            return rms;
        } else {
            return DEFAULT_RMS_TO_USE;
        }
    }

}

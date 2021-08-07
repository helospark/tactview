package com.helospark.tactview.core.timeline.marker;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.marker.markers.ChapterMarker;
import com.helospark.tactview.core.timeline.marker.markers.InpointMarker;
import com.helospark.tactview.core.timeline.marker.markers.Marker;
import com.helospark.tactview.core.timeline.marker.markers.OutpointMarker;
import com.helospark.tactview.core.timeline.message.NotificationMessage;
import com.helospark.tactview.core.util.StaticObjectMapper;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class MarkerRepository implements SaveLoadContributor, ResettableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerRepository.class);
    private Map<TimelinePosition, Marker> markers = new TreeMap<>();

    private MessagingService messagingService;

    public MarkerRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void addChapter(TimelinePosition position, String title) {
        addMarker(position, new ChapterMarker(title));
    }

    public <T extends Marker> boolean addMarker(TimelinePosition position, T marker) {
        if (!isValidMarker(marker, position)) {
            return false;
        }
        ensureNoDuplicatedMarkersFromSingletonMarkers(marker);

        markers.put(position, marker);
        messagingService.sendMessage(new MarkersChangedMessage());
        return true;
    }

    private <T extends Marker> boolean isValidMarker(T generalMarker, TimelinePosition position) {
        if (generalMarker.getType().equals(MarkerType.INPOINT)) {
            Optional<TimelinePosition> outpointMarker = getMarkersOfType(OutpointMarker.class)
                    .keySet()
                    .stream()
                    .findFirst();
            if (outpointMarker.isPresent() && outpointMarker.get().isLessThan(position)) {
                messagingService.sendMessage(new NotificationMessage("Inpoint cannot be after outpoint", "Inpoint cannot be after outpoint", NotificationMessage.Level.WARNING));
                return false;
            }
        }
        if (generalMarker.getType().equals(MarkerType.OUTPOINT)) {
            Optional<TimelinePosition> inpointMarker = getMarkersOfType(InpointMarker.class)
                    .keySet()
                    .stream()
                    .findFirst();
            if (inpointMarker.isPresent() && inpointMarker.get().isGreaterThan(position)) {
                messagingService.sendMessage(new NotificationMessage("Outpoint cannot be before inpoint", "Outpoint cannot be after inpoint", NotificationMessage.Level.WARNING));
                return false;
            }
        }
        return true;
    }

    private <T extends Marker> void ensureNoDuplicatedMarkersFromSingletonMarkers(T generalMarker) {
        if (generalMarker.getType().equals(MarkerType.INPOINT)) {
            getMarkersOfType(InpointMarker.class)
                    .keySet()
                    .stream()
                    .forEach(a -> removeMarkerAt(a));
        }
        if (generalMarker.getType().equals(MarkerType.OUTPOINT)) {
            getMarkersOfType(OutpointMarker.class)
                    .keySet()
                    .stream()
                    .forEach(a -> removeMarkerAt(a));
        }
    }

    public void removeMarkerAt(TimelinePosition position) {
        markers.remove(position);
        messagingService.sendMessage(new MarkersChangedMessage());
    }

    public Map<TimelinePosition, Marker> getMarkers() {
        return new TreeMap<>(markers);
    }

    public <T extends Marker> Map<TimelinePosition, T> getMarkersOfType(Class<T> type) {
        return markers.entrySet()
                .stream()
                .filter(a -> a.getValue().getClass().isAssignableFrom(type))
                .collect(Collectors.toMap(a -> a.getKey(), a -> (T) a.getValue(), (a, b) -> a, TreeMap::new));
    }

    public void removeAllMarkers() {
        markers.clear();
        messagingService.sendMessage(new MarkersChangedMessage());
    }

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata) {
        generatedContent.put("markers", this.markers);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata metadata) {
        try {
            JsonNode clipsNode = tree.get("markers");
            if (clipsNode != null) {
                ObjectReader reader = StaticObjectMapper.objectMapper.readerFor(new TypeReference<Map<TimelinePosition, Marker>>() {
                });
                this.markers = reader.readValue(clipsNode);
                if (this.markers == null) {
                    this.markers = new TreeMap<>();
                }
                messagingService.sendMessage(new MarkersChangedMessage());
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to load chapters information", e);
        }
    }

    @Override
    public void resetDefaults() {
        removeAllMarkers();
    }

}

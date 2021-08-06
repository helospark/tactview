package com.helospark.tactview.core.timeline.marker;

import java.util.Map;
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
import com.helospark.tactview.core.timeline.marker.markers.Marker;
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
        markers.put(position, new ChapterMarker(title));
        messagingService.sendMessage(new MarkersChangedMessage());
    }

    public <T extends Marker> void addMarker(TimelinePosition position, T generalMarker) {
        this.markers.put(position, generalMarker);
        messagingService.sendMessage(new MarkersChangedMessage());
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
                .filter(a -> type.getClass().isAssignableFrom(a.getValue().getClass()))
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

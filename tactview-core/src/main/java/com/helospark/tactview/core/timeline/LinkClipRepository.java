package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingServiceImpl;

@Component
public class LinkClipRepository implements SaveLoadContributor {
    private Map<String, List<String>> linkedClips = new ConcurrentHashMap<>();
    private MessagingServiceImpl messagingService;
    private ObjectMapper objectMapper;

    public LinkClipRepository(MessagingServiceImpl messagingService, @Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
        this.messagingService = messagingService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipRemovedMessage.class, message -> {
            removeClip(message.getElementId());
        });
    }

    public void linkClip(String clipId, String otherClipId) {
        if (clipId.equals(otherClipId)) {
            return;
        }
        linkedClips.compute(clipId, (previousKey, previousValue) -> {
            List<String> clips;
            if (previousValue != null) {
                clips = previousValue;
            } else {
                clips = new ArrayList<>();
            }
            clips.add(otherClipId);
            return clips;
        });
    }

    public List<String> getLinkedClips(String clipId) {
        return linkedClips.getOrDefault(clipId, Collections.emptyList());
    }

    public void linkClips(String clipId, List<TimelineClip> clips) {
        clips.stream()
                .forEach(clip -> linkClip(clipId, clip.getId()));
    }

    public void linkClipIds(String clipId, List<String> clips) {
        clips.stream()
                .forEach(clip -> linkClip(clipId, clip));
    }

    public void removeClip(String elementId) {
        linkedClips.remove(elementId);
        for (var entry : linkedClips.entrySet()) {
            entry.getValue().remove(elementId);
        }
    }

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent) {
        generatedContent.put("linkedClips", linkedClips);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata metadata) {
        try {
            JsonNode clipsNode = tree.get("linkedClips");
            if (clipsNode != null) {
                ObjectReader reader = objectMapper.readerFor(new TypeReference<Map<String, List<String>>>() {
                });
                Map<String, List<String>> value = reader.readValue(clipsNode);

                linkedClips = new ConcurrentHashMap<>(value);
            } else {
                linkedClips = new ConcurrentHashMap<>();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void linkClips(List<String> linkedClipIds) {
        for (var clipId : linkedClipIds) {
            linkClipIds(clipId, linkedClipIds);
        }
    }

    public void unlinkClips(List<String> linkedClipIds) {
        for (var clipId : linkedClipIds) {
            linkClipIds(clipId, linkedClipIds);
        }
    }

    public void unlinkClipIds(List<String> linkedClipIds) {
        for (var clipId : linkedClipIds) {
            unlinkClipIds(clipId, linkedClipIds);
        }
    }

    private void unlinkClipIds(String clipId, List<String> linkedClipIds) {
        List<String> entries = linkedClips.get(clipId);
        if (entries == null) {
            return;
        }
        entries.removeAll(linkedClipIds);
    }

}

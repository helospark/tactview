package com.helospark.tactview.core.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class LinkClipRepository implements SaveLoadContributor {
    private Map<String, List<String>> linkedClips = new ConcurrentHashMap<>();
    private MessagingService messagingService;
    private ObjectMapper objectMapper;

    public LinkClipRepository(MessagingService messagingService, @Qualifier("simpleObjectMapper") ObjectMapper objectMapper) {
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
    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata) {
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

    public Map<String, List<String>> getLinkedClips(List<String> clipIds) {
        Map<String, List<String>> result = new HashMap<>();
        for (var clip : clipIds) {
            result.put(clip, getLinkedClips(clip));
        }
        return result;
    }

    public Map<String, List<String>> mapLinksWithChangedClipsIds(Map<String, String> oldToNewClipIds, Map<String, List<String>> originalLinks) {
        Map<String, List<String>> result = new HashMap<>();
        for (var originalLink : originalLinks.entrySet()) {
            List<String> elements = new ArrayList<>();
            String newLink = oldToNewClipIds.get(originalLink.getKey());
            if (newLink != null) {
                for (var link : originalLink.getValue()) {
                    if (oldToNewClipIds.containsKey(link)) {
                        elements.add(oldToNewClipIds.get(link));
                    }
                    result.put(newLink, elements);
                }
            }
        }
        return result;
    }

    public void linkClips(Map<String, List<String>> newLinks) {
        for (var entry : newLinks.entrySet()) {
            for (var element : entry.getValue()) {
                linkClip(entry.getKey(), element);
            }
        }
    }

    public List<List<String>> unlinkAllConnectedClips(List<String> linkedClipIds) {
        List<List<String>> originallyLinked = new ArrayList<>();
        for (var entry : linkedClipIds) {
            List<String> linkedClip = new ArrayList<>(getLinkedClips(entry));
            for (var a : linkedClip) {
                List<String> unlinked = List.of(entry, a);
                unlinkClipIds(unlinked);
                originallyLinked.add(unlinked);
            }
        }
        return originallyLinked;
    }

}

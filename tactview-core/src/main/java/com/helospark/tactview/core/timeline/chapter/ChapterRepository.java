package com.helospark.tactview.core.timeline.chapter;

import java.util.Map;
import java.util.TreeMap;

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
import com.helospark.tactview.core.util.StaticObjectMapper;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class ChapterRepository implements SaveLoadContributor, ResettableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChapterRepository.class);
    private Map<TimelinePosition, String> chapters = new TreeMap<>();

    private MessagingService messagingService;

    public ChapterRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void addChapter(TimelinePosition position, String title) {
        chapters.put(position, title);
        messagingService.sendMessage(new ChaptersChangedMessage());
    }

    public void removeChapterAt(TimelinePosition position) {
        chapters.remove(position);
        messagingService.sendMessage(new ChaptersChangedMessage());
    }

    public Map<TimelinePosition, String> getChapters() {
        return new TreeMap<>(chapters);
    }

    public void removeAllChapters() {
        chapters.clear();
        messagingService.sendMessage(new ChaptersChangedMessage());
    }

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata) {
        generatedContent.put("chapters", this.chapters);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata metadata) {
        try {
            JsonNode clipsNode = tree.get("chapters");
            if (clipsNode != null) {
                ObjectReader reader = StaticObjectMapper.objectMapper.readerFor(new TypeReference<Map<TimelinePosition, String>>() {
                });
                this.chapters = reader.readValue(clipsNode);
                if (this.chapters == null) {
                    this.chapters = new TreeMap<>();
                }
                messagingService.sendMessage(new ChaptersChangedMessage());
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to load chapters information", e);
        }
    }

    @Override
    public void resetDefaults() {
        removeAllChapters();
    }
}

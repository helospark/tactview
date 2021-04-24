package com.helospark.tactview.ui.javafx.uicomponents.pattern;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

import javafx.scene.image.Image;

@Component
public class TimelinePatternRepository {
    private Map<String, Image> repository = new ConcurrentHashMap<>();
    private MessagingService messagingService;

    public TimelinePatternRepository(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ClipRemovedMessage.class, message -> {
            Image removedElement = repository.remove(message.getElementId());
            if (removedElement != null) {
                messagingService.sendAsyncMessage(new TimelinePatternChangedMessage(message.getElementId(), TimelinePatternChangedMessage.ChangeType.REMOVED));
            }
        });
    }

    public void savePatternForClip(String clipId, Image image) {
        repository.put(clipId, image);
        messagingService.sendAsyncMessage(new TimelinePatternChangedMessage(clipId, TimelinePatternChangedMessage.ChangeType.ADDED));
    }

    public Optional<Image> getPatternForClipId(String clipId) {
        return Optional.ofNullable(repository.get(clipId));
    }

}

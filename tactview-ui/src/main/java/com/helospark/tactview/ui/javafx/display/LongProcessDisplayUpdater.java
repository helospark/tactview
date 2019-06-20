package com.helospark.tactview.ui.javafx.display;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.longprocess.LongProcessDescriptor;
import com.helospark.tactview.core.timeline.longprocess.LongProcessRequestor;
import com.helospark.tactview.core.timeline.message.progress.ProgressAdvancedMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressDoneMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressInitializeMessage;
import com.helospark.tactview.core.timeline.message.progress.ProgressType;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.DisplayUpdateRequestMessage;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedRequest;

import javafx.application.Platform;
import javafx.scene.paint.Color;

@Component
public class LongProcessDisplayUpdater implements DisplayUpdatedListener {
    private LongProcessRequestor longProcessRequestor;
    private MessagingService messagingService;
    private NameToIdRepository nameToIdRepository;

    private Map<String, LongProgressProcessDomain> currentLongProcesses = new ConcurrentHashMap<>();

    public LongProcessDisplayUpdater(LongProcessRequestor longProcessRequestor, MessagingService messagingService, NameToIdRepository nameToIdRepository) {
        this.longProcessRequestor = longProcessRequestor;
        this.messagingService = messagingService;
        this.nameToIdRepository = nameToIdRepository;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ProgressInitializeMessage.class, message -> {
            if (message.getType().equals(ProgressType.LONG_PROCESS)) {
                currentLongProcesses.put(message.getId(), new LongProgressProcessDomain(message.getAllJobs()));
                messagingService.sendAsyncMessage(new DisplayUpdateRequestMessage(false));
            }
        });
        messagingService.register(ProgressAdvancedMessage.class, message -> {
            LongProgressProcessDomain domain = currentLongProcesses.get(message.getId());
            if (domain != null) {
                domain.finishedJobs += message.getNumberOfJobsDone();
                int percent = (int) ((double) domain.finishedJobs / domain.allJobs * 100.0);
                int deltaPercent = percent - domain.lastDisplayUpdatePercent;
                if (deltaPercent >= 1) {
                    messagingService.sendAsyncMessage(new DisplayUpdateRequestMessage(false));
                    domain.lastDisplayUpdatePercent = percent;
                }
            }
        });
        messagingService.register(ProgressDoneMessage.class, message -> {
            currentLongProcesses.remove(message.getId());
            messagingService.sendAsyncMessage(new DisplayUpdateRequestMessage(true));
        });
    }

    @Override
    public void displayUpdated(DisplayUpdatedRequest request) {
        Platform.runLater(() -> {
            int y = 40;
            request.graphics.setStroke(Color.RED);
            if (currentLongProcesses.size() > 0) {
                request.graphics.strokeText("Long process update in progress", 10, 20);
            }
            for (var entry : currentLongProcesses.entrySet()) {
                var domain = entry.getValue();
                int percent = (int) ((double) domain.finishedJobs / domain.allJobs * 100.0);
                LongProcessDescriptor longProcessEntry = longProcessRequestor.getRunningJobs().get(entry.getKey());
                if (longProcessEntry != null) {
                    String elementName = Optional.ofNullable(nameToIdRepository.getNameForId(longProcessEntry.effectId.orElse(longProcessEntry.clipId))).orElse("???");
                    request.graphics.strokeText(elementName + " " + percent + "%", 10, y);
                }
                y += 20;
            }
        });
    }

}

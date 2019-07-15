package com.helospark.tactview.ui.javafx.display;

import java.util.ArrayList;
import java.util.List;
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
import com.helospark.tactview.core.util.messaging.MessagingServiceImpl;
import com.helospark.tactview.ui.javafx.DisplayUpdateRequestMessage;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedListener;
import com.helospark.tactview.ui.javafx.uicomponents.display.DisplayUpdatedRequest;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

@Component
public class LongProcessDisplayUpdater implements DisplayUpdatedListener {
    private static final int BORDER = 20;
    private LongProcessRequestor longProcessRequestor;
    private MessagingServiceImpl messagingService;
    private NameToIdRepository nameToIdRepository;

    private Map<String, LongProgressProcessDomain> currentLongProcesses = new ConcurrentHashMap<>();

    public LongProcessDisplayUpdater(LongProcessRequestor longProcessRequestor, MessagingServiceImpl messagingService, NameToIdRepository nameToIdRepository) {
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
        request.graphics.setStroke(Color.RED);
        List<String> texts = new ArrayList<>();
        if (currentLongProcesses.size() > 0) {
            texts.add("Long process update in progress");
        }
        for (var entry : currentLongProcesses.entrySet()) {
            var domain = entry.getValue();
            int percent = (int) ((double) domain.finishedJobs / domain.allJobs * 100.0);
            LongProcessDescriptor longProcessEntry = longProcessRequestor.getRunningJobs().get(entry.getKey());
            if (longProcessEntry != null) {
                String elementName = Optional.ofNullable(nameToIdRepository.getNameForId(longProcessEntry.effectId.orElse(longProcessEntry.clipId))).orElse("???");
                texts.add(elementName + " " + percent + "%");
            }
        }
        if (texts.size() > 0) {
            Platform.runLater(() -> {
                double sumHeights = texts.stream()
                        .mapToDouble(s -> computeBounds(s).getHeight())
                        .sum();

                double startYPosition = (request.canvas.getHeight() - sumHeights) / 2;

                request.graphics.setFill(new Color(0.1, 0.1, 0.1, 0.5));
                request.graphics.fillRect(0, startYPosition - BORDER, request.canvas.getWidth(), sumHeights + 2 * BORDER);

                request.graphics.setStroke(Color.WHITE);
                double position = startYPosition + computeBounds(texts.get(0)).getHeight();
                for (String text : texts) {
                    Bounds bounds = computeBounds(text);
                    double startXPosition = (request.canvas.getWidth() - bounds.getWidth()) / 2.0;
                    request.graphics.strokeText(text, startXPosition, position);
                    position += bounds.getHeight();
                }
            });
        }
    }

    private Bounds computeBounds(String string) {
        Text text = new Text(string);
        return text.getLayoutBounds();
    }

}

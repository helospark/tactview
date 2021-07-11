package com.helospark.tactview.ui.javafx.clip.chain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.ClipResizedCommand;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;

@Component
@Order(91)
public class ResizeClipContextMenuChainItem implements ClipContextMenuChainItem {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private AlertDialogFactory alertFactory;
    @Slf4j
    private Logger logger;

    public ResizeClipContextMenuChainItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, AlertDialogFactory alertFactory) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.alertFactory = alertFactory;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        MenuItem resizeToClipsMenuItem = new MenuItem("Resize to...");
        resizeToClipsMenuItem.setOnAction(e -> {
            String currentLength = request.getPrimaryClip()
                    .getInterval()
                    .getLength()
                    .getSeconds()
                    .setScale(4, RoundingMode.HALF_UP)
                    .toString();
            TextInputDialog dialog = new TextInputDialog(currentLength);

            dialog.setTitle("New length");
            dialog.setHeaderText("Enter length in seconds:");
            dialog.setContentText("Length:");

            Optional<String> result = dialog.showAndWait();

            try {
                result.ifPresent(value -> {
                    BigDecimal newLength = new BigDecimal(value);
                    if (newLength.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException("Length has to be positive");
                    }
                    TimelineClip clipToResize = request.getPrimaryClip();
                    TimelinePosition newRightPosition = clipToResize.getInterval().getStartPosition().add(newLength);

                    ClipResizedCommand command = ClipResizedCommand.builder()
                            .withClipIds(timelineManager.findLinkedClipsWithSameInterval(clipToResize.getId()))
                            .withLeft(false)
                            .withMoreResizeExpected(false)
                            .withOriginalPosition(clipToResize.getInterval().getEndPosition())
                            .withPosition(newRightPosition)
                            .withRevertable(true)
                            .withTimelineManager(timelineManager)
                            .withUseSpecialPoints(false)
                            .build();

                    commandInterpreter.sendWithResult(command);
                });
            } catch (Exception ex) {
                alertFactory.showExceptionDialog("Cannot change length", ex);

                logger.warn("Unable to change clip length", ex);
            }
        });

        return resizeToClipsMenuItem;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return request.getPrimaryClip().isResizable();
    }

}

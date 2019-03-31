package com.helospark.tactview.ui.javafx.clip.chain;

import java.math.BigDecimal;
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

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;

@Component
@Order(91)
public class ResizeClipContextMenuChainItem implements ClipContextMenuChainItem {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    @Slf4j
    private Logger logger;

    public ResizeClipContextMenuChainItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        MenuItem resizeToClipsMenuItem = new MenuItem("Resize to...");
        resizeToClipsMenuItem.setOnAction(e -> {
            String currentLength = request.getPrimaryClip()
                    .getInterval()
                    .getLength()
                    .getSeconds()
                    .setScale(4)
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
                            .withClipId(clipToResize.getId())
                            .withLeft(false)
                            .withMoreResizeExpected(false)
                            .withOriginalInterval(clipToResize.getInterval())
                            .withPosition(newRightPosition)
                            .withRevertable(true)
                            .withTimelineManager(timelineManager)
                            .withUseSpecialPoints(false)
                            .build();

                    commandInterpreter.sendWithResult(command);
                });
            } catch (Exception ex) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Cannot change length");
                alert.setContentText(ex.getMessage());
                alert.setHeaderText(null);
                alert.showAndWait();

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

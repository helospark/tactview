package com.helospark.tactview.ui.javafx.clip.chain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.ClipMovedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipToLeftCommand;
import com.helospark.tactview.ui.javafx.commands.impl.ClipToRightCommand;
import com.helospark.tactview.ui.javafx.commands.impl.service.MoveByUnitService;
import com.helospark.tactview.ui.javafx.commands.impl.service.MoveByUnitService.Direction;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;

@Component
@Order(92)
public class MoveToContextMenuChainItem implements ClipContextMenuChainItem {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManagerAccessor;
    private AlertDialogFactory alertDialogFactory;
    private StylesheetAdderService stylesheetAdderService;
    private MoveByUnitService moveByUnitService;

    public MoveToContextMenuChainItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManagerAccessor, AlertDialogFactory alertDialogFactory,
            StylesheetAdderService stylesheetAdderService, MoveByUnitService moveByUnitService) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.alertDialogFactory = alertDialogFactory;
        this.stylesheetAdderService = stylesheetAdderService;
        this.moveByUnitService = moveByUnitService;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        Menu menu = new Menu("Move");

        menu.getItems().add(createMoveToPositionMenuItem(request));
        menu.getItems().add(createMoveToLeftMenuItem(request));
        menu.getItems().add(createMoveToRightMenuItem(request));
        menu.getItems().add(createMoveRelativeMenuItem(request));
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(createMoveLeftOneFrameMenuItem(request, "left one frame", Direction.LEFT));
        menu.getItems().add(createMoveLeftOneFrameMenuItem(request, "right one frame", Direction.RIGHT));
        menu.getItems().add(createMoveLeftOneFrameMenuItem(request, "up one channel", Direction.UP));
        menu.getItems().add(createMoveLeftOneFrameMenuItem(request, "down one channel", Direction.DOWN));

        return menu;
    }

    private MenuItem createMoveLeftOneFrameMenuItem(ClipContextMenuChainItemRequest request, String title, Direction direction) {
        MenuItem menuItem = new MenuItem(title);
        menuItem.setOnAction(e -> moveByUnitService.moveByOneUnit(direction));
        return menuItem;
    }

    private MenuItem createMoveToPositionMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToPositionMenuItem = new MenuItem("to position");

        moveToPositionMenuItem.setOnAction(e -> {
            String currentPosition = request.getPrimaryClip()
                    .getInterval()
                    .getStartPosition()
                    .getSeconds()
                    .setScale(4)
                    .toString();
            TextInputDialog dialog = new TextInputDialog(currentPosition);

            dialog.setTitle("New position");
            dialog.setHeaderText("Enter new position in seconds:");
            dialog.setContentText("Position:");
            stylesheetAdderService.addStyleSheets(dialog.getDialogPane(), "stylesheet.css");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String originalChannelId = timelineManagerAccessor.findChannelForClipId(request.getPrimaryClip().getId()).get().getId();

                ClipMovedCommand clipMovedCommand = ClipMovedCommand.builder()
                        .withAdditionalClipIds(request.getAllClips().stream().map(a -> a.getId()).collect(Collectors.toList()))
                        .withClipId(request.getPrimaryClip().getId())
                        .withEnableJumpingToSpecialPosition(false)
                        .withIsRevertable(true)
                        .withMoreMoveExpected(false)
                        .withNewPosition(new TimelinePosition(new BigDecimal(result.get())))
                        .withNewChannelId(originalChannelId)
                        .withOriginalChannelId(originalChannelId)
                        .withPreviousPosition(request.getPrimaryClip().getInterval().getStartPosition())
                        .withTimelineManager(timelineManagerAccessor)
                        .build();

                ClipMovedCommand commandResult = commandInterpreter.sendWithResult(clipMovedCommand).join();

                if (!commandResult.wasOperationSuccessful()) {
                    Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.ERROR, "Cannot move clip at that position", "Cannot move clip at that position");
                    alert.showAndWait();
                }
            }
        });

        return moveToPositionMenuItem;
    }

    private MenuItem createMoveToLeftMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToLeftMenuItem = new MenuItem("to furthest left");

        moveToLeftMenuItem.setOnAction(e -> {
            ClipToLeftCommand clipMovedCommand = ClipToLeftCommand.builder()
                    .withAdditionalClipIds(request.getAllClips().stream().map(a -> a.getId()).collect(Collectors.toList()))
                    .withClipId(request.getPrimaryClip().getId())
                    .withTimelineManager(timelineManagerAccessor)
                    .build();

            ClipToLeftCommand commandResult = commandInterpreter.sendWithResult(clipMovedCommand).join();

            if (!commandResult.wasOperationSuccessful()) {
                Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.ERROR, "Cannot move clip to left", "Cannot move clip to left");
                alert.showAndWait();
            }
        });

        return moveToLeftMenuItem;
    }

    private MenuItem createMoveToRightMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToLeftMenuItem = new MenuItem("to furthest right");

        moveToLeftMenuItem.setOnAction(e -> {
            ClipToRightCommand clipMovedCommand = ClipToRightCommand.builder()
                    .withAdditionalClipIds(request.getAllClips().stream().map(a -> a.getId()).collect(Collectors.toList()))
                    .withClipId(request.getPrimaryClip().getId())
                    .withTimelineManager(timelineManagerAccessor)
                    .build();

            ClipToRightCommand commandResult = commandInterpreter.sendWithResult(clipMovedCommand).join();

            if (!commandResult.wasOperationSuccessful()) {
                Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.ERROR, "Cannot move clip to right", "Cannot move clip to right");
                alert.showAndWait();
            }
        });

        return moveToLeftMenuItem;
    }

    private MenuItem createMoveRelativeMenuItem(ClipContextMenuChainItemRequest request) {
        MenuItem moveToPositionMenuItem = new MenuItem("relative");

        moveToPositionMenuItem.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("0.0");

            dialog.setTitle("Relative move");
            dialog.setHeaderText("Enter relative move in seconds:");
            dialog.setContentText("Relative move:");
            stylesheetAdderService.addStyleSheets(dialog.getDialogPane(), "stylesheet.css");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String originalChannelId = timelineManagerAccessor.findChannelForClipId(request.getPrimaryClip().getId()).get().getId();

                ClipMovedCommand clipMovedCommand = ClipMovedCommand.builder()
                        .withAdditionalClipIds(request.getAllClips().stream().map(a -> a.getId()).collect(Collectors.toList()))
                        .withClipId(request.getPrimaryClip().getId())
                        .withEnableJumpingToSpecialPosition(false)
                        .withIsRevertable(true)
                        .withMoreMoveExpected(false)
                        .withNewPosition(request.getPrimaryClip().getInterval().getStartPosition().add(new BigDecimal(result.get())))
                        .withNewChannelId(originalChannelId)
                        .withOriginalChannelId(originalChannelId)
                        .withPreviousPosition(request.getPrimaryClip().getInterval().getStartPosition())
                        .withTimelineManager(timelineManagerAccessor)
                        .build();

                ClipMovedCommand commandResult = commandInterpreter.sendWithResult(clipMovedCommand).join();

                if (!commandResult.wasOperationSuccessful()) {
                    Alert alert = alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.ERROR, "Cannot move clip at that position", "Cannot move clip at that position");
                    alert.showAndWait();
                }
            }
        });

        return moveToPositionMenuItem;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return true;
    }

}

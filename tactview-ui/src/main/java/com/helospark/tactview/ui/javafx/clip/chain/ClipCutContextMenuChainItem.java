package com.helospark.tactview.ui.javafx.clip.chain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;

@Component
@Order(92)
public class ClipCutContextMenuChainItem implements ClipContextMenuChainItem {
    private ClipCutService clipCutService;
    private StylesheetAdderService stylesheetAdderService;
    private AlertDialogFactory alertDialogFactory;

    public ClipCutContextMenuChainItem(ClipCutService clipCutService, StylesheetAdderService stylesheetAdderService, AlertDialogFactory alertDialogFactory) {
        this.clipCutService = clipCutService;
        this.stylesheetAdderService = stylesheetAdderService;
        this.alertDialogFactory = alertDialogFactory;
    }

    @Override
    public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
        Menu menu = new Menu("Cut");

        menu.getItems().add(cutCurrentClip(request));
        menu.getItems().add(cutWithUnlinkCurrentClip(request));
        menu.getItems().add(cutAt(request));

        return menu;
    }

    private MenuItem cutAt(ClipContextMenuChainItemRequest request) {
        MenuItem copyClip = new MenuItem("Cut this clip at...");
        copyClip.setOnAction(e -> {
            TimelineClip clip = request.getPrimaryClip();

            TimelineInterval globalInterval = clip.getUnmodifiedInterval();
            TimelineInterval localInterval = globalInterval.butMoveStartPostionTo(TimelinePosition.ofZero());
            String halfPosition = localInterval.getStartPosition().add(localInterval.getEndPosition()).getSeconds().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP).toString();

            TextInputDialog dialog = new TextInputDialog(halfPosition);

            dialog.setTitle("Cut clip at");
            dialog.setHeaderText(null);
            dialog.setContentText("Cut at:");
            stylesheetAdderService.addStyleSheets(dialog.getDialogPane(), "stylesheet.css");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                try {
                    BigDecimal value = new BigDecimal(result.get());
                    clipCutService.cutClipAtPosition(request.getPrimaryClip().getId(), false, new TimelinePosition(value).add(globalInterval.getStartPosition()));
                } catch (NumberFormatException ex) {
                    alertDialogFactory.createSimpleAlertWithTitleAndContent(AlertType.ERROR, "Invalid position", "Invalid position " + result.get());
                }
            }

        });
        return copyClip;
    }

    public MenuItem cutCurrentClip(ClipContextMenuChainItemRequest request) {
        MenuItem copyClip = new MenuItem("Cut this clip & linked");
        copyClip.setOnAction(e -> clipCutService.cutClip(request.getPrimaryClip().getId(), true));
        return copyClip;
    }

    public MenuItem cutWithUnlinkCurrentClip(ClipContextMenuChainItemRequest request) {
        MenuItem copyClip = new MenuItem("Cut only this clip");
        copyClip.setOnAction(e -> clipCutService.cutClip(request.getPrimaryClip().getId(), false));
        return copyClip;
    }

    @Override
    public boolean supports(ClipContextMenuChainItemRequest request) {
        return true;
    }

}

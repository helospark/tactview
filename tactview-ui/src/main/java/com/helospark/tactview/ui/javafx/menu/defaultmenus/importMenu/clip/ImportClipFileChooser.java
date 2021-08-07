package com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.clip;

import java.io.File;
import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

import javafx.stage.FileChooser;

@Component
public class ImportClipFileChooser {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreterService;
    private AlertDialogFactory alertDialogFactory;

    public ImportClipFileChooser(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreterService, AlertDialogFactory alertDialogFactory) {
        this.timelineManager = timelineManager;
        this.commandInterpreterService = commandInterpreterService;
        this.alertDialogFactory = alertDialogFactory;
    }

    public void importClip() {
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(JavaFXUiMain.STAGE);

        if (file != null) {
            TimelinePosition maxPosition = timelineManager.findEndPosition();

            AddClipsCommand clipCommand = AddClipsCommand.builder()
                    .withChannelId(timelineManager.getAllChannelIds().get(0))
                    .withPosition(maxPosition)
                    .withFilePaths(List.of(file.getAbsolutePath()))
                    .withTimelineManager(timelineManager)
                    .build();

            commandInterpreterService.sendWithResult(clipCommand)
                    .exceptionally(e -> {
                        alertDialogFactory.showExceptionDialog("Unable to add image sequence", e);

                        return null;
                    });
            // reveal added clip
        }
    }

}

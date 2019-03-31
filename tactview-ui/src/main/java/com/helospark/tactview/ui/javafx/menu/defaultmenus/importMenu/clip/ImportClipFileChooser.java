package com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.clip;

import java.io.File;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;

@Component
public class ImportClipFileChooser {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreterService;

    public ImportClipFileChooser(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreterService) {
        this.timelineManager = timelineManager;
        this.commandInterpreterService = commandInterpreterService;
    }

    public void importClip() {
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(JavaFXUiMain.STAGE);

        if (file != null) {
            TimelinePosition maxPosition = timelineManager.findEndPosition();

            AddClipRequest clipRequest = AddClipRequest.builder()
                    .withChannelId(timelineManager.getAllChannelIds().get(0))
                    .withPosition(maxPosition)
                    .withFilePath(file.getAbsolutePath())
                    .build();

            commandInterpreterService.sendWithResult(new AddClipsCommand(clipRequest, timelineManager))
                    .exceptionally(e -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Unable to add image sequence");
                        alert.setHeaderText(null);
                        alert.setContentText(e.getMessage());

                        alert.showAndWait();

                        return null;
                    });
            // reveal added clip
        }
    }

}

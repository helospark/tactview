package com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.imagesequence;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AddClipRequestMetaDataKey;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.clipfactory.sequence.FileNamePatternToFileResolverService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageSequenceChooserDialog {
    private Stage stage;

    public ImageSequenceChooserDialog(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreterService, ProjectRepository projectRepository) {
        BorderPane borderPane = new BorderPane();

        Scene dialog = new Scene(borderPane);
        stage = new Stage();

        GridPane gridPane = new GridPane();

        gridPane.add(new Label("Images"), 0, 0);
        Button button = new Button("Choose first image");
        gridPane.add(button, 1, 0);

        gridPane.add(new Label("Path"), 0, 1);
        TextField pathField = new TextField("");
        gridPane.add(pathField, 1, 1);

        gridPane.add(new Label("Pattern"), 0, 2);
        TextField patternField = new TextField("");
        gridPane.add(patternField, 1, 2);

        gridPane.add(new Label("Fps"), 0, 3);
        TextField fpsField = new TextField(projectRepository.getFps().toString());
        gridPane.add(fpsField, 1, 3);

        button.setOnMouseClicked(e -> {
            File file = new FileChooser().showOpenDialog(stage);
            if (file != null) {
                String fileName = file.getName();
                String path = file.getParentFile().getAbsolutePath();

                Pattern reg = Pattern.compile("(.*?)(\\d+?)(\\..*)");
                Matcher numberMatcher = reg.matcher(fileName);

                if (numberMatcher.matches()) {
                    patternField.setText(numberMatcher.replaceAll("$1(\\\\d+)$3"));
                } else {
                    patternField.setText(fileName);
                }
                pathField.setText(path);
            }
        });

        // buttons
        HBox buttonBar = new HBox();

        Button cancelButton = new Button("Close");
        cancelButton.setOnMouseClicked(e1 -> stage.close());

        Button okButton = new Button("OK");
        okButton.setOnMouseClicked(e2 -> {
            TimelinePosition maxPosition = timelineManager.findEndPosition();

            Map<AddClipRequestMetaDataKey, Object> metadata = new HashMap<>();
            metadata.put(AddClipRequestMetaDataKey.FPS, new BigDecimal(fpsField.getText()));

            String filePath = pathField.getText() + FileNamePatternToFileResolverService.PATH_FILENAME_SEPARATOR + patternField.getText();

            AddClipRequest clipRequest = AddClipRequest.builder()
                    .withChannelId(timelineManager.getAllChannelIds().get(0))
                    .withPosition(maxPosition)
                    .withAddClipRequestMetadataKey(metadata)
                    .withFilePath(filePath)
                    .build();

            commandInterpreterService.sendWithResult(new AddClipsCommand(clipRequest, timelineManager))
                    .exceptionally(e -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Unable to add image sequence");
                        alert.setHeaderText(null);
                        alert.setContentText(e.getMessage());

                        alert.showAndWait();

                        return null;
                    }).thenAccept(a -> {
                        if (a != null) {
                            stage.close();
                        }
                    });
        });

        Region emptyRegion = new Region();
        HBox.setHgrow(emptyRegion, Priority.ALWAYS);

        buttonBar.getChildren().add(cancelButton);
        buttonBar.getChildren().add(emptyRegion);
        buttonBar.getChildren().add(okButton);

        borderPane.setCenter(gridPane);
        borderPane.setBottom(buttonBar);

        stage.setTitle("Import image sequence");
        stage.setScene(dialog);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }
}

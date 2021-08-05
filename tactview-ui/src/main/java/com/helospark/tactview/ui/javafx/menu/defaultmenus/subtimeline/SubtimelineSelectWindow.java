package com.helospark.tactview.ui.javafx.menu.defaultmenus.subtimeline;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.subtimeline.ExposedDescriptorDescriptor;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SubtimelineSelectWindow {
    private TimelineManagerAccessor timelineManagerAccessor;
    private NameToIdRepository nameToIdRepository;
    private StylesheetAdderService stylesheetAdderService;

    Map<String, ExposedDescriptorDescriptor> descriptorDescriptors = new HashMap<>();
    boolean isSuccessful;

    public SubtimelineSelectWindow(TimelineManagerAccessor timelineManagerAccessor, NameToIdRepository nameToIdRepository, StylesheetAdderService stylesheetAdderService) {
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.nameToIdRepository = nameToIdRepository;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public Set<ExposedDescriptorDescriptor> open() {
        Stage stage = new Stage();
        BorderPane mainBorderPane = new BorderPane();
        mainBorderPane.getStyleClass().add("dialog-root");
        VBox vbox = new VBox();
        vbox.getStyleClass().add("subtimeline-descriptor-properties-box");

        for (var channel : timelineManagerAccessor.getChannels()) {
            for (var clip : channel.getAllClips()) {
                GridPane gridPane = new GridPane();
                gridPane.getStyleClass().add("subtimeline-descriptor-properties-grid");

                BorderPane borderPane = new BorderPane();
                borderPane.setTop(new Label(nameToIdRepository.getNameForId(clip.getId()) + " properties"));
                borderPane.setCenter(gridPane);

                int index = 0;

                for (var descriptor : clip.getDescriptors()) {
                    String descriptorId = descriptor.getKeyframeableEffect().getId();
                    ExposedDescriptorDescriptor descritporDescriptor = ExposedDescriptorDescriptor.builder()
                            .withId(descriptorId)
                            .withName(descriptor.getName())
                            .withGroup(descriptor.getGroup().orElse(null))
                            .build();

                    CheckBox checkBox = new CheckBox("");
                    checkBox.setSelected(false);
                    checkBox.selectedProperty().addListener((value, oldVal, newVal) -> {
                        if (newVal) {
                            descriptorDescriptors.put(descriptorId, descritporDescriptor);
                        } else {
                            descriptorDescriptors.remove(descriptorId);
                        }
                    });
                    Label label = new Label(descriptor.getName());

                    gridPane.add(checkBox, 0, index);
                    gridPane.add(label, 1, index);
                    ++index;
                }
                vbox.getChildren().add(borderPane);
            }
        }

        ScrollPane scrollPane = new ScrollPane(vbox);
        mainBorderPane.setCenter(scrollPane);
        mainBorderPane.setBottom(createButtonBar(stage));

        Scene scene = new Scene(mainBorderPane);
        stage.setWidth(700);
        stage.setHeight(500);
        stage.setScene(scene);
        stage.setTitle("Select which properties are exposed");
        stylesheetAdderService.setDefaultStyleSheetForDialog(stage, mainBorderPane);

        stage.showAndWait();

        return descriptorDescriptors.values().stream().collect(Collectors.toSet());
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    private ButtonBar createButtonBar(Stage stage) {
        Button closeButton = new Button("Cancel");
        closeButton.setOnAction(e -> {
            stage.close();
        });

        Button saveAndRestartButton = new Button("Create");
        saveAndRestartButton.setOnAction(e -> {
            isSuccessful = true;
            stage.close();
        });

        ButtonBar result = new ButtonBar();

        ButtonBar.setButtonData(closeButton, ButtonData.CANCEL_CLOSE);
        ButtonBar.setButtonData(saveAndRestartButton, ButtonData.APPLY);

        result.getButtons().addAll(closeButton, saveAndRestartButton);
        return result;
    }

}

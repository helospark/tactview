package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.save.LoadRequest;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@Component
public class UiLoadHandler {
    private SaveAndLoadHandler saveAndLoadHandler;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private File autosaveRootDirectory;

    public UiLoadHandler(SaveAndLoadHandler saveAndLoadHandler, CurrentProjectSavedFileRepository currentProjectSavedFileRepository, @Value("${autosave.directory}") File autosaveRootDirectory) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.autosaveRootDirectory = autosaveRootDirectory;
    }

    public void load() {
        openFileChooser(Optional.empty());

    }

    public void loadAutosaved() {
        openFileChooser(Optional.of(autosaveRootDirectory));
    }

    private void openFileChooser(Optional<File> initialDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new ExtensionFilter("TactView save file", "tvs"));
        fileChooser.setTitle("Open Project");
        initialDirectory.ifPresent(a -> fileChooser.setInitialDirectory(a));
        File file = fileChooser.showOpenDialog(JavaFXUiMain.STAGE);

        if (file != null) {
            try {
                saveAndLoadHandler.load(new LoadRequest(file.getAbsolutePath()));
                currentProjectSavedFileRepository.setCurrentSavedFile(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Unable to load project");
                alert.setHeaderText(e.getMessage());
                alert.setContentText(e.getMessage() + ", see logs for details");
                alert.show();
            }
        }
    }

}

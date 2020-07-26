package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.save.LoadRequest;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@Component
public class UiLoadHandler {
    private SaveAndLoadHandler saveAndLoadHandler;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private File autosaveRootDirectory;
    private AlertDialogFactory alertDialogFactory;

    private UiSaveHandler uiSaveHandler;

    public UiLoadHandler(SaveAndLoadHandler saveAndLoadHandler, CurrentProjectSavedFileRepository currentProjectSavedFileRepository, @Value("${autosave.directory}") File autosaveRootDirectory,
            UiSaveHandler uiSaveHandler, AlertDialogFactory alertDialogFactory) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.autosaveRootDirectory = autosaveRootDirectory;
        this.uiSaveHandler = uiSaveHandler;
        this.alertDialogFactory = alertDialogFactory;
    }

    public void load() {
        openFileChooser(Optional.ofNullable(uiSaveHandler.lastOpenedDirectoryName).map(File::new));

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
                uiSaveHandler.lastOpenedDirectoryName = file.getParentFile().getAbsolutePath();
                saveAndLoadHandler.load(new LoadRequest(file.getAbsolutePath()));
                currentProjectSavedFileRepository.setCurrentSavedFile(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = alertDialogFactory.createErrorAlertWithStackTrace("Unable to load project", e);
                alert.show();
            }
        }
    }

}

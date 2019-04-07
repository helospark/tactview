package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.core.save.SaveRequest;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

@Component
public class UiSaveHandler {
    private SaveAndLoadHandler saveAndLoadHandler;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private DirtyRepository dirtyRepository;
    @Slf4j
    private Logger logger;

    public UiSaveHandler(SaveAndLoadHandler saveAndLoadHandler, CurrentProjectSavedFileRepository currentProjectSavedFileRepository, DirtyRepository dirtyRepository) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.dirtyRepository = dirtyRepository;
    }

    public boolean save() {
        try {
            return executeSave();
        } catch (Exception e) {
            logger.error("Unable to save", e);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Unable to save project");
            alert.setHeaderText(e.getMessage());
            alert.setContentText(e.getMessage() + ", see logs for details");
            alert.show();
            return false;
        }
    }

    private boolean executeSave() {
        Optional<String> currentSavedFile = currentProjectSavedFileRepository.getCurrentSavedFile();
        if (currentSavedFile.isPresent()) {
            dirtyRepository.setDirty(false);
            saveAndLoadHandler.save(new SaveRequest(currentSavedFile.get()));
            return true;
        } else {
            Optional<String> fileName = queryUserAboutFileName();
            if (fileName.isPresent()) {
                dirtyRepository.setDirty(false);
                saveAndLoadHandler.save(new SaveRequest(fileName.get()));
                currentProjectSavedFileRepository.setCurrentSavedFile(fileName.get());
                return true;
            }
        }
        return false;
    }

    public void saveAs() {
        Optional<String> fileName = queryUserAboutFileName();
        if (fileName.isPresent()) {
            saveAndLoadHandler.save(new SaveRequest(fileName.get()));
        }
    }

    private Optional<String> queryUserAboutFileName() {
        while (true) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Project");
            File file = fileChooser.showSaveDialog(JavaFXUiMain.STAGE);

            if (file == null) {
                return Optional.empty();
            }

            if (file.exists()) {
                Alert alert = new Alert(AlertType.CONFIRMATION, "File " + file.getAbsolutePath() + " already exists. Override?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    return Optional.ofNullable(file.getAbsolutePath());
                } else if (alert.getResult() == ButtonType.CANCEL) {
                    return Optional.empty();
                } // else in case of 'NO' try again
            } else {
                return Optional.ofNullable(file.getAbsolutePath());
            }
        }

    }

}

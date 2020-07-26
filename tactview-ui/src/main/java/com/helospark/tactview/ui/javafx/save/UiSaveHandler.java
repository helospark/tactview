package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.persistentstate.PersistentState;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.core.save.SaveRequest;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

@Component
public class UiSaveHandler {
    private SaveAndLoadHandler saveAndLoadHandler;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private DirtyRepository dirtyRepository;
    private StylesheetAdderService stylesheetAdderService;
    private AlertDialogFactory alertDialogFactory;
    @Slf4j
    private Logger logger;
    @PersistentState
    String lastOpenedDirectoryName;

    public UiSaveHandler(SaveAndLoadHandler saveAndLoadHandler, CurrentProjectSavedFileRepository currentProjectSavedFileRepository, DirtyRepository dirtyRepository,
            StylesheetAdderService stylesheetAdderService, AlertDialogFactory alertDialogFactory) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.dirtyRepository = dirtyRepository;
        this.stylesheetAdderService = stylesheetAdderService;
        this.alertDialogFactory = alertDialogFactory;
    }

    public boolean save() {
        try {
            return executeSave();
        } catch (Exception e) {
            Alert alert = alertDialogFactory.createErrorAlertWithStackTrace("Unable to save project", e);
            logger.error("Unable to save", e);
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
                lastOpenedDirectoryName = new File(fileName.get()).getParentFile().getAbsolutePath();
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
            lastOpenedDirectoryName = new File(fileName.get()).getParentFile().getAbsolutePath();
            saveAndLoadHandler.save(new SaveRequest(fileName.get()));
        }
    }

    private Optional<String> queryUserAboutFileName() {
        while (true) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Project");
            if (lastOpenedDirectoryName != null) {
                fileChooser.setInitialDirectory(new File(lastOpenedDirectoryName));
            }
            File file = fileChooser.showSaveDialog(JavaFXUiMain.STAGE);

            if (file == null) {
                return Optional.empty();
            }

            String extension = file.getName().endsWith(".tvs") ? "" : ".tvs";
            File fileWithSavedExtension = new File(file.getAbsolutePath() + extension); // TODO: why is this needed in core and ui?

            if (!extension.isEmpty() && fileWithSavedExtension.exists()) { // If user set extension JavaFX already asked this question
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setContentText("File " + fileWithSavedExtension.getAbsolutePath() + " already exists. Override?");
                alert.setHeaderText(null);
                alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.CANCEL, ButtonType.YES);
                stylesheetAdderService.addStyleSheets(alert.getDialogPane(), "stylesheet.css");
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

    public String getLastOpenedDirectoryName() {
        return lastOpenedDirectoryName;
    }

    public void setLastOpenedDirectoryName(String lastOpenedDirectoryName) {
        this.lastOpenedDirectoryName = lastOpenedDirectoryName;
    }

}

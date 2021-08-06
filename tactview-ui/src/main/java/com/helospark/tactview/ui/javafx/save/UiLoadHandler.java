package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.save.AbstractSaveHandler;
import com.helospark.tactview.core.save.LoadRequest;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.core.save.TemplateSaveAndLoadHandler;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.ProjectInitializer;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

@Component
public class UiLoadHandler {
    private static final ExtensionFilter SAVEFILE_EXTENSION_FILTER = new ExtensionFilter("Tactview save file", "tvs");
    private SaveAndLoadHandler saveAndLoadHandler;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private File autosaveRootDirectory;
    private AlertDialogFactory alertDialogFactory;
    private RecentlyAccessedRepository recentlyAccessedRepository;
    private TemplateSaveAndLoadHandler templateSaveAndLoadHandler;
    private ProjectInitializer projectInitializer;

    private UiSaveHandler uiSaveHandler;

    public UiLoadHandler(SaveAndLoadHandler saveAndLoadHandler, CurrentProjectSavedFileRepository currentProjectSavedFileRepository, @Value("${autosave.directory}") File autosaveRootDirectory,
            UiSaveHandler uiSaveHandler, AlertDialogFactory alertDialogFactory, RecentlyAccessedRepository recentlyAccessedRepository, TemplateSaveAndLoadHandler templateSaveAndLoadHandler,
            ProjectInitializer projectInitializer) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.autosaveRootDirectory = autosaveRootDirectory;
        this.uiSaveHandler = uiSaveHandler;
        this.alertDialogFactory = alertDialogFactory;
        this.recentlyAccessedRepository = recentlyAccessedRepository;
        this.templateSaveAndLoadHandler = templateSaveAndLoadHandler;
        this.projectInitializer = projectInitializer;
    }

    public void load() {
        openFileChooser(Optional.ofNullable(uiSaveHandler.lastOpenedDirectoryName).map(File::new), SAVEFILE_EXTENSION_FILTER, saveAndLoadHandler);
    }

    public void loadFile(File file) {
        projectInitializer.clearState();
        saveAndLoadHandler.load(new LoadRequest(file.getAbsolutePath()));
        recentlyAccessedRepository.addNewRecentlySavedElement(file);
        currentProjectSavedFileRepository.setCurrentSavedFile(file.getAbsolutePath());
    }

    public void loadAutosaved() {
        openFileChooser(Optional.of(autosaveRootDirectory), SAVEFILE_EXTENSION_FILTER, saveAndLoadHandler);
    }

    private void openFileChooser(Optional<File> initialDirectory, ExtensionFilter extensionFilter, AbstractSaveHandler loadHandler) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(extensionFilter);
        fileChooser.setTitle("Open Project");
        initialDirectory.ifPresent(a -> fileChooser.setInitialDirectory(a));
        File file = fileChooser.showOpenDialog(JavaFXUiMain.STAGE);

        if (file != null) {
            try {
                uiSaveHandler.lastOpenedDirectoryName = file.getParentFile().getAbsolutePath();
                projectInitializer.clearState();
                loadHandler.load(new LoadRequest(file.getAbsolutePath()));
                recentlyAccessedRepository.addNewRecentlySavedElement(file);
                currentProjectSavedFileRepository.setCurrentSavedFile(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                alertDialogFactory.showExceptionDialog("Unable to load project", e);
            }
        }
    }

    public void loadTemplate() {
        openFileChooser(Optional.ofNullable(uiSaveHandler.lastOpenedDirectoryName).map(File::new), new ExtensionFilter("Tactview template", "tvt"), templateSaveAndLoadHandler);
    }

}

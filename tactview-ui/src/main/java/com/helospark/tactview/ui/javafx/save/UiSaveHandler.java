package com.helospark.tactview.ui.javafx.save;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.persistentstate.PersistentState;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.core.save.SaveRequest;
import com.helospark.tactview.core.save.SaveTemplateRequest;
import com.helospark.tactview.core.save.TemplateSaveAndLoadHandler;
import com.helospark.tactview.core.timeline.subtimeline.ExposedDescriptorDescriptor;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.subtimeline.SubtimelineSelectWindowOpener;
import com.helospark.tactview.ui.javafx.save.QuerySaveFilenameService.QuerySaveFileNameRequest;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

@Component
public class UiSaveHandler {
    private SaveAndLoadHandler saveAndLoadHandler;
    private TemplateSaveAndLoadHandler templateSaveHandler;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private DirtyRepository dirtyRepository;
    private AlertDialogFactory alertDialogFactory;
    private RecentlyAccessedRepository recentlyAccessedRepository;
    private QuerySaveFilenameService querySaveFilenameService;
    private SubtimelineSelectWindowOpener subtimelineWindowOpener;
    @Slf4j
    private Logger logger;
    @PersistentState
    String lastOpenedDirectoryName;

    public UiSaveHandler(SaveAndLoadHandler saveAndLoadHandler, CurrentProjectSavedFileRepository currentProjectSavedFileRepository, DirtyRepository dirtyRepository,
            QuerySaveFilenameService querySaveFilenameService, AlertDialogFactory alertDialogFactory, RecentlyAccessedRepository recentlyAccessedRepository,
            TemplateSaveAndLoadHandler templateSaveHandler, SubtimelineSelectWindowOpener subtimelineWindowOpener) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.dirtyRepository = dirtyRepository;
        this.querySaveFilenameService = querySaveFilenameService;
        this.alertDialogFactory = alertDialogFactory;
        this.recentlyAccessedRepository = recentlyAccessedRepository;
        this.templateSaveHandler = templateSaveHandler;
        this.subtimelineWindowOpener = subtimelineWindowOpener;
    }

    public boolean save() {
        try {
            return executeSave();
        } catch (Exception e) {
            alertDialogFactory.showExceptionDialog("Unable to save project", e);
            logger.error("Unable to save", e);
            return false;
        }
    }

    private boolean executeSave() {
        Optional<String> currentSavedFile = currentProjectSavedFileRepository.getCurrentSavedFile();
        if (currentSavedFile.isPresent()) {
            dirtyRepository.setDirty(false);
            String filePath = currentSavedFile.get();
            saveAndLoadHandler.save(createSaveRequest(filePath));
            recentlyAccessedRepository.addNewRecentlySavedElement(new File(filePath));
            return true;
        } else {
            QuerySaveFileNameRequest request = QuerySaveFileNameRequest.builder()
                    .withInitialDirectory(lastOpenedDirectoryName)
                    .withTitle("Save Project")
                    .build();
            Optional<String> fileName = querySaveFilenameService.queryUserAboutFileName(request);
            if (fileName.isPresent()) {
                String pathName = fileName.get();
                if (!pathName.endsWith(".tvs")) {
                    pathName += ".tvs";
                }
                lastOpenedDirectoryName = new File(pathName).getParentFile().getAbsolutePath();
                dirtyRepository.setDirty(false);
                saveAndLoadHandler.save(createSaveRequest(pathName));

                recentlyAccessedRepository.addNewRecentlySavedElement(new File(pathName));
                currentProjectSavedFileRepository.setCurrentSavedFile(pathName);
                return true;
            }
        }
        return false;
    }

    protected SaveRequest createSaveRequest(String filePath) {
        return SaveRequest.builder()
                .withFileName(filePath)
                .withPackageAllContent(false)
                .build();
    }

    public void saveAs() {
        QuerySaveFileNameRequest request = QuerySaveFileNameRequest.builder()
                .withInitialDirectory(lastOpenedDirectoryName)
                .withTitle("Save Project")
                .build();
        Optional<String> fileName = querySaveFilenameService.queryUserAboutFileName(request);
        if (fileName.isPresent()) {
            String resultFilePath = fileName.get();
            if (!resultFilePath.endsWith(".tvs")) {
                resultFilePath += ".tvs";
            }
            File resultFile = new File(resultFilePath);
            lastOpenedDirectoryName = resultFile.getParentFile().getAbsolutePath();
            saveAndLoadHandler.save(createSaveRequest(resultFilePath));
            dirtyRepository.setDirty(false);
            recentlyAccessedRepository.addNewRecentlySavedElement(resultFile);
        }
    }

    public void saveAsTemplate() {
        QuerySaveFileNameRequest request = QuerySaveFileNameRequest.builder()
                .withInitialDirectory(lastOpenedDirectoryName)
                .withTitle("Save Project as template")
                .build();
        Optional<String> fileName = querySaveFilenameService.queryUserAboutFileName(request);
        if (fileName.isPresent()) {
            Optional<Set<ExposedDescriptorDescriptor>> result = subtimelineWindowOpener.openWindow();
            if (result.isPresent()) {
                String resultFilePath = fileName.get();
                if (!resultFilePath.endsWith(".tvt")) {
                    resultFilePath += ".tvt";
                }
                File resultFile = new File(resultFilePath);
                lastOpenedDirectoryName = resultFile.getParentFile().getAbsolutePath();
                templateSaveHandler.save(createSaveTemplateRequest(resultFilePath, result.get()));
                dirtyRepository.setDirty(false);
            }
        }
    }

    protected SaveTemplateRequest createSaveTemplateRequest(String filePath, Set<ExposedDescriptorDescriptor> exposedDescriptors) {
        return SaveTemplateRequest.builder()
                .withFileName(filePath)
                .withPackageAllContent(false)
                .withExposedDescriptors(exposedDescriptors)
                .build();
    }

    public String getLastOpenedDirectoryName() {
        return lastOpenedDirectoryName;
    }

    public void setLastOpenedDirectoryName(String lastOpenedDirectoryName) {
        this.lastOpenedDirectoryName = lastOpenedDirectoryName;
    }

}

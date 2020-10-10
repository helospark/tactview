package com.helospark.tactview.ui.javafx.init;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.init.PostInitializationArgsCallback;
import com.helospark.tactview.core.save.LoadRequest;
import com.helospark.tactview.core.save.SaveAndLoadHandler;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.ProjectInitializer;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddClipsCommand;
import com.helospark.tactview.ui.javafx.save.CurrentProjectSavedFileRepository;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

@Component
public class TactviewInitializationCallback implements PostInitializationArgsCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(TactviewInitializationCallback.class);
    private SaveAndLoadHandler saveAndLoadHandler;
    private AlertDialogFactory alertDialogFactory;
    private CurrentProjectSavedFileRepository currentProjectSavedFileRepository;
    private ProjectInitializer projectInitializer;
    private UiCommandInterpreterService commandInterpreterService;
    private TimelineManagerAccessor timelineManagerAccessor;

    public TactviewInitializationCallback(SaveAndLoadHandler saveAndLoadHandler, AlertDialogFactory alertDialogFactory, CurrentProjectSavedFileRepository currentProjectSavedFileRepository,
            ProjectInitializer projectInitializer, TimelineManagerAccessor timelineManagerAccessor, UiCommandInterpreterService commandInterpreterService) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.alertDialogFactory = alertDialogFactory;
        this.currentProjectSavedFileRepository = currentProjectSavedFileRepository;
        this.projectInitializer = projectInitializer;
        this.timelineManagerAccessor = timelineManagerAccessor;
        this.commandInterpreterService = commandInterpreterService;
    }

    @Override
    public void call(String[] args) {
        boolean initialized = false;
        try {
            Optional<String> optionalFileName = Arrays.stream(args)
                    .filter(a -> !a.startsWith("-"))
                    .findFirst();
            if (optionalFileName.isPresent()) {

                File file = new File(optionalFileName.get());

                LOGGER.debug("Application launched with " + Arrays.toString(args));

                if (file.exists()) {
                    if (file.getName().endsWith(".tvs")) {
                        LOGGER.info("Loading save file " + file.getAbsolutePath());
                        saveAndLoadHandler.load(new LoadRequest(file.getAbsolutePath()));
                        currentProjectSavedFileRepository.setCurrentSavedFile(file.getAbsolutePath());
                        initialized = true;
                    } else {
                        LOGGER.info("Loading media file " + file.getAbsolutePath());
                        projectInitializer.clearAndInitialize();
                        String firstChannelId = timelineManagerAccessor.getChannels().get(0).getId();
                        AddClipRequest addClipRequest = AddClipRequest.builder()
                                .withChannelId(firstChannelId)
                                .withPosition(TimelinePosition.ofZero())
                                .withFilePath(file.getAbsolutePath())
                                .build();

                        commandInterpreterService.synchronousSend(new AddClipsCommand(addClipRequest, timelineManagerAccessor));

                        initialized = true;
                    }
                }
            }
        } catch (Exception e) {
            alertDialogFactory.createErrorAlertWithStackTrace("Initialization failed", e);
        }

        if (!initialized) {
            projectInitializer.clearAndInitialize();
        }
    }

}

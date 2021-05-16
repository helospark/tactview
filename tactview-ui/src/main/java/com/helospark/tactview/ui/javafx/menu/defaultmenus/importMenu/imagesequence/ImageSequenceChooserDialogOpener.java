package com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.imagesequence;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

@Component
public class ImageSequenceChooserDialogOpener {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreterService;
    private ProjectRepository projectRepository;
    private AlertDialogFactory alertDialogFactory;
    private StylesheetAdderService stylesheetAdderService;

    public ImageSequenceChooserDialogOpener(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreterService, ProjectRepository projectRepository,
            AlertDialogFactory alertDialogFactory, StylesheetAdderService stylesheetAdderService) {
        this.timelineManager = timelineManager;
        this.commandInterpreterService = commandInterpreterService;
        this.projectRepository = projectRepository;
        this.alertDialogFactory = alertDialogFactory;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    public void importImageSequence() {
        ImageSequenceChooserDialog dialog = new ImageSequenceChooserDialog(timelineManager, commandInterpreterService, projectRepository, alertDialogFactory, stylesheetAdderService);
        dialog.show();
    }

}

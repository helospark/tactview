package com.helospark.tactview.ui.javafx.menu.defaultmenus.importMenu.imagesequence;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;

@Component
public class ImageSequenceChooserDialogOpener {
    private TimelineManager timelineManager;
    private UiCommandInterpreterService commandInterpreterService;
    private ProjectRepository projectRepository;

    public ImageSequenceChooserDialogOpener(TimelineManager timelineManager, UiCommandInterpreterService commandInterpreterService, ProjectRepository projectRepository) {
        this.timelineManager = timelineManager;
        this.commandInterpreterService = commandInterpreterService;
        this.projectRepository = projectRepository;
    }

    public void importImageSequence() {
        ImageSequenceChooserDialog dialog = new ImageSequenceChooserDialog(timelineManager, commandInterpreterService, projectRepository);
        dialog.show();
    }

}

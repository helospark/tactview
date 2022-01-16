package com.helospark.tactview.ui.javafx;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import java.math.BigDecimal;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.uicomponents.DefaultCanvasTranslateSetter;

@Component
public class ProjectInitializer {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private DirtyRepository dirtyRepository;
    private UiTimelineManager uiTimelineManager;
    private LightDiContext diContext;
    private DefaultCanvasTranslateSetter defaultCanvasTranslateSetter;
    private UiProjectRepository uiProjectRepository;

    public ProjectInitializer(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, DirtyRepository dirtyRepository, UiTimelineManager uiTimelineManager,
            LightDiContext diContext, DefaultCanvasTranslateSetter defaultCanvasTranslateSetter, UiProjectRepository uiProjectRepository) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.dirtyRepository = dirtyRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.diContext = diContext;
        this.defaultCanvasTranslateSetter = defaultCanvasTranslateSetter;
        this.uiProjectRepository = uiProjectRepository;
    }

    public void clearAndInitialize() {
        clearState();

        for (int i = 0; i < 8; ++i) {
            commandInterpreter.synchronousSend(new CreateChannelCommand(timelineManager, LAST_INDEX));
        }
        dirtyRepository.setDirty(false);
    }

    public void clearState() {
        diContext.getListOfBeans(ResettableBean.class)
                .stream()
                .forEach(bean -> bean.resetDefaults());

        timelineManager.getChannels()
                .stream()
                .forEach(channel -> timelineManager.removeChannel(channel.getId()));

        uiTimelineManager.jumpAbsolute(BigDecimal.ZERO);
    }

}

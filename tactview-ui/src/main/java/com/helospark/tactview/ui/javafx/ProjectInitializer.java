package com.helospark.tactview.ui.javafx;

import static com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand.LAST_INDEX;

import java.math.BigDecimal;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.commands.impl.CreateChannelCommand;

@Component
public class ProjectInitializer {
    private UiCommandInterpreterService commandInterpreter;
    private TimelineManagerAccessor timelineManager;
    private DirtyRepository dirtyRepository;
    private UiTimelineManager uiTimelineManager;
    private LightDiContext diContext;

    public ProjectInitializer(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, DirtyRepository dirtyRepository, UiTimelineManager uiTimelineManager,
            LightDiContext diContext) {
        this.commandInterpreter = commandInterpreter;
        this.timelineManager = timelineManager;
        this.dirtyRepository = dirtyRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.diContext = diContext;
    }

    public void clearAndInitialize() {
        timelineManager.getChannels()
                .stream()
                .forEach(channel -> timelineManager.removeChannel(channel.getId()));

        for (int i = 0; i < 8; ++i) {
            commandInterpreter.sendWithResult(new CreateChannelCommand(timelineManager, LAST_INDEX)).join();
        }

        dirtyRepository.setDirty(false);

        diContext.getListOfBeans(ResettableBean.class)
                .stream()
                .forEach(bean -> bean.resetDefaults());

        uiTimelineManager.jumpAbsolute(BigDecimal.ZERO);

    }

}

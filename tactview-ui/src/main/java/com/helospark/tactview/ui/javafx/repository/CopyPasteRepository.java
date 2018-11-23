package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingClipsCommand;

@Component
public class CopyPasteRepository {
    private TimelineManager timelineManager;
    private UiCommandInterpreterService commandInterpreter;

    private Object clipboardContent;
    private TimelineChannel timelineChannel;
    private CopyableType copyableType;

    public CopyPasteRepository(TimelineManager timelineManager, UiCommandInterpreterService commandInterpreter) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
    }

    public void copyClip(String clipId) {
        timelineChannel = timelineManager.findChannelForClipId(clipId).orElse(null);
        clipboardContent = timelineManager.findClipById(clipId)
                .map(clip -> clip.cloneClip()) // clone here, so changes will not affect the pasted clip
                .orElse(null);
        copyableType = CopyableType.CLIP;
    }

    public void pasteWithoutChannel() {
        if (clipboardContent == null) {
            return;
        }
        if (copyableType == CopyableType.CLIP) {
            AddExistingClipRequest request = AddExistingClipRequest.builder()
                    .withChannel(timelineChannel)
                    .withClipToAdd(((TimelineClip) clipboardContent).cloneClip()) // multiple ctrl+v
                    .build();
            AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineManager);

            commandInterpreter.sendWithResult(addClipCommand);
        } else {
            throw new RuntimeException("Later");
        }
    }

    static enum CopyableType {
        CLIP,
        EFFECT
    }
}

package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.AddExistingEffectRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingEffectCommand;
import com.helospark.tactview.ui.javafx.repository.copypaste.ClipCopyPasteDomain;
import com.helospark.tactview.ui.javafx.repository.copypaste.EffectCopyPasteDomain;

@Component
public class CopyPasteRepository {
    private TimelineManager timelineManager;
    private UiCommandInterpreterService commandInterpreter;

    private Object clipboardContent;

    public CopyPasteRepository(TimelineManager timelineManager, UiCommandInterpreterService commandInterpreter) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
    }

    public void copyClip(String clipId) {
        TimelineChannel timelineChannel = timelineManager.findChannelForClipId(clipId).orElse(null);
        TimelineClip timelineClip = timelineManager.findClipById(clipId)
                .map(clip -> clip.cloneClip()) // clone here, so changes will not affect the pasted clip
                .orElse(null);
        if (timelineChannel != null && timelineClip != null) {
            clipboardContent = new ClipCopyPasteDomain(timelineClip, timelineChannel);
        }
    }

    public void copyEffect(String selectedEffect) {
        TimelineClip clip = timelineManager.findClipForEffect(selectedEffect).orElse(null);
        StatelessEffect effect = clip.getEffect(selectedEffect).orElse(null);

        if (clip != null && effect != null) {
            clipboardContent = new EffectCopyPasteDomain(clip, effect);
        }
    }

    public void pasteWithoutAdditionalInfo() {
        if (clipboardContent == null) {
            return;
        }
        tryPasteClip();
    }

    public void pasteOnExistingEffect(String clipId) {
        if (clipboardContent == null) {
            return;
        }
        if (!tryPasteClip()) {
            tryPasteEffect(clipId);
        }
    }

    private void tryPasteEffect(String clipId) {
        if (clipboardContent instanceof EffectCopyPasteDomain) {
            TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow();
            AddExistingEffectRequest request = AddExistingEffectRequest.builder()
                    .withClipToAdd(clip)
                    .withEffect(((EffectCopyPasteDomain) clipboardContent).effect.cloneEffect())
                    .build();
            AddExistingEffectCommand addExistingEffectCommand = new AddExistingEffectCommand(request, timelineManager);

            commandInterpreter.sendWithResult(addExistingEffectCommand);
        }
    }

    private boolean tryPasteClip() {
        if (clipboardContent instanceof ClipCopyPasteDomain) {
            AddExistingClipRequest request = AddExistingClipRequest.builder()
                    .withChannel(((ClipCopyPasteDomain) clipboardContent).timelineChannel)
                    .withClipToAdd(((ClipCopyPasteDomain) clipboardContent).clipboardContent.cloneClip()) // multiple ctrl+v
                    .build();
            AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineManager);

            commandInterpreter.sendWithResult(addClipCommand);
            return true;
        }
        return false;
    }

    static enum CopyableType {
        CLIP,
        EFFECT
    }
}

package com.helospark.tactview.ui.javafx.repository;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.AddExistingEffectRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingEffectCommand;
import com.helospark.tactview.ui.javafx.repository.copypaste.ClipCopyPasteDomain;
import com.helospark.tactview.ui.javafx.repository.copypaste.EffectCopyPasteDomain;

@Component
public class CopyPasteRepository {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreter;

    private Object clipboardContent;

    public CopyPasteRepository(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
    }

    public void copyClip(String clipId) {
        TimelineChannel timelineChannel = timelineManager.findChannelForClipId(clipId).orElse(null);
        TimelineClip timelineClip = timelineManager.findClipById(clipId)
                .map(clip -> clip.cloneClip(CloneRequestMetadata.ofDefault())) // clone here, so changes will not affect the pasted clip
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
        pasteWithoutInfo();
    }

    public void pasteOnExistingClip(String clipId) {
        if (clipboardContent == null) {
            return;
        }
        pasteWithSelectedClip(clipId);
    }

    private void pasteWithSelectedClip(String clipId) {
        if (clipboardContent instanceof ClipCopyPasteDomain) {
            pasteClip();
        } else if (clipboardContent instanceof EffectCopyPasteDomain) {
            TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow();
            pasteEffectToClip(clip);
        }
    }

    private void pasteWithoutInfo() {
        if (clipboardContent instanceof ClipCopyPasteDomain) {
            pasteClip();
        } else if (clipboardContent instanceof EffectCopyPasteDomain) {
            TimelineClip clip = timelineManager.findClipById(((EffectCopyPasteDomain) clipboardContent).clipboardContent.getId()).orElseThrow();
            pasteEffectToClip(clip);
        }
    }

    private void pasteEffectToClip(TimelineClip clip) {
        AddExistingEffectRequest request = AddExistingEffectRequest.builder()
                .withClipToAdd(clip)
                .withEffect(((EffectCopyPasteDomain) clipboardContent).effect.cloneEffect(CloneRequestMetadata.ofDefault()))
                .build();
        AddExistingEffectCommand addExistingEffectCommand = new AddExistingEffectCommand(request, timelineManager);

        commandInterpreter.sendWithResult(addExistingEffectCommand);
    }

    private void pasteClip() {
        AddExistingClipRequest request = AddExistingClipRequest.builder()
                .withChannel(((ClipCopyPasteDomain) clipboardContent).timelineChannel)
                .withClipToAdd(((ClipCopyPasteDomain) clipboardContent).clipboardContent.cloneClip(CloneRequestMetadata.ofDefault())) // multiple ctrl+v
                .build();
        AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineManager);

        commandInterpreter.sendWithResult(addClipCommand);
    }

}

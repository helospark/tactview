package com.helospark.tactview.ui.javafx.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.AddExistingEffectRequest;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineChannel;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingClipsCommand;
import com.helospark.tactview.ui.javafx.commands.impl.AddExistingEffectCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.repository.copypaste.ClipCopyPasteDomain;
import com.helospark.tactview.ui.javafx.repository.copypaste.EffectCopyPasteDomain;
import com.helospark.tactview.ui.javafx.repository.copypaste.EffectCopyPasteDomain.EffectCopyPasteDomainElement;

@Component
public class CopyPasteRepository {
    private TimelineManagerAccessor timelineManager;
    private UiCommandInterpreterService commandInterpreter;

    private Object clipboardContent;

    public CopyPasteRepository(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
    }

    public void copyClip(List<String> clipIds) {
        if (clipIds.size() == 0) {
            return;
        }
        List<TimelineClip> clips = timelineManager.resolveClipIdsToClips(clipIds);
        TimelinePosition minimumPosition = timelineManager.findMinimumPosition(clips).get();

        TimelinePosition relativeEndPosition = clips.stream()
                .map(a -> a.getInterval().getEndPosition().subtract(minimumPosition))
                .sorted((a, b) -> b.compareTo(a))
                .findFirst()
                .get();

        List<ClipCopyPasteDomain.CopiedClipData> copiedData = new ArrayList<>();
        for (var clipId : clipIds) {
            TimelineChannel timelineChannel = timelineManager.findChannelForClipId(clipId).orElse(null);
            TimelineClip timelineClip = timelineManager.findClipById(clipId)
                    .map(clip -> clip.cloneClip(CloneRequestMetadata.ofDefault())) // clone here, so changes will not affect the pasted clip
                    .orElse(null);
            if (timelineChannel != null && timelineClip != null) {
                TimelinePosition relativePosition = timelineClip.getInterval().getStartPosition().subtract(minimumPosition);
                copiedData.add(new ClipCopyPasteDomain.CopiedClipData(timelineClip, timelineChannel, relativePosition));
            }
        }
        clipboardContent = new ClipCopyPasteDomain(copiedData, relativeEndPosition);
    }

    public void copyEffect(String selectedEffect) {
        TimelineClip clip = timelineManager.findClipForEffect(selectedEffect).orElse(null);
        StatelessEffect effect = clip.getEffect(selectedEffect).orElse(null);

        if (clip != null && effect != null) {
            clipboardContent = new EffectCopyPasteDomain(clip, effect);
        }
    }

    public void copyEffect(List<String> selectedEffects) {
        if (selectedEffects.size() == 0) {
            return;
        }
        List<EffectCopyPasteDomainElement> elements = selectedEffects.stream()
                .map(a -> {
                    TimelineClip clip = timelineManager.findClipForEffect(a).orElse(null);
                    StatelessEffect effect = clip.getEffect(a).orElse(null);

                    return new EffectCopyPasteDomainElement(clip, effect);
                })
                .collect(Collectors.toList());
        clipboardContent = new EffectCopyPasteDomain(elements);
    }

    public void pasteWithoutAdditionalInfo() {
        if (clipboardContent == null) {
            return;
        }
        pasteWithoutInfo();
    }

    public void pasteOnExistingClips(List<String> selectedClipIds) {
        if (clipboardContent == null) {
            return;
        }
        pasteWithSelectedClips(selectedClipIds);
    }

    private void pasteWithSelectedClips(List<String> selectedClipIds) {
        if (clipboardContent instanceof ClipCopyPasteDomain) {
            pasteClip();
        } else if (clipboardContent instanceof EffectCopyPasteDomain) {

            List<AddExistingEffectCommand> commands = selectedClipIds.stream()
                    .map(clipId -> {
                        TimelineClip clip = timelineManager.findClipById(clipId).orElseThrow();
                        return createExsitingEffectPasteCommand(clip);
                    }).collect(Collectors.toList());

            CompositeCommand command = new CompositeCommand(commands);
            commandInterpreter.sendWithResult(command);
        }
    }

    private void pasteWithoutInfo() {
        if (clipboardContent instanceof ClipCopyPasteDomain) {
            pasteClip();
        } else if (clipboardContent instanceof EffectCopyPasteDomain) {
            TimelineClip clip = timelineManager.findClipById(((EffectCopyPasteDomain) clipboardContent).getElements().get(0).clip.getId()).orElseThrow();
            pasteEffectsToClip(clip);
        }
    }

    private void pasteEffectsToClip(TimelineClip clip) {
        AddExistingEffectCommand addExistingEffectCommand = createExsitingEffectPasteCommand(clip);

        commandInterpreter.sendWithResult(addExistingEffectCommand);
    }

    private AddExistingEffectCommand createExsitingEffectPasteCommand(TimelineClip clip) {
        AddExistingEffectRequest request = AddExistingEffectRequest.builder()
                .withClipToAdd(clip)
                .withEffects(getAllEffectClone())
                .build();
        AddExistingEffectCommand addExistingEffectCommand = new AddExistingEffectCommand(request, timelineManager);
        return addExistingEffectCommand;
    }

    private List<StatelessEffect> getAllEffectClone() {
        return ((EffectCopyPasteDomain) clipboardContent).getElements()
                .stream()
                .map(e -> e.effect.cloneEffect(CloneRequestMetadata.ofDefault()))
                .collect(Collectors.toList());
    }

    private void pasteClip() {
        TimelinePosition positionToInsertClipsTo = timelineManager.findEndPosition();
        ClipCopyPasteDomain clipCopyPasteDomain = (ClipCopyPasteDomain) clipboardContent;
        List<AddExistingClipsCommand> commands = new ArrayList<>();
        for (var element : clipCopyPasteDomain.copiedData) {
            AddExistingClipRequest request = AddExistingClipRequest.builder()
                    .withChannel(element.timelineChannel)
                    .withClipToAdd(element.clipboardContent.cloneClip(CloneRequestMetadata.ofDefault())) // multiple ctrl+v
                    .withPosition(Optional.of(positionToInsertClipsTo.add(element.relativeOffset)))
                    .build();
            AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineManager);
            commands.add(addClipCommand);
        }
        commandInterpreter.sendWithResult(new CompositeCommand(commands));
    }

    public boolean isEffectOnClipboard() {
        return clipboardContent != null && clipboardContent instanceof EffectCopyPasteDomain;
    }
}

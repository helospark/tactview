package com.helospark.tactview.ui.javafx.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.timeline.AddExistingClipRequest;
import com.helospark.tactview.core.timeline.AddExistingEffectRequest;
import com.helospark.tactview.core.timeline.ClipChannelPair;
import com.helospark.tactview.core.timeline.LinkClipRepository;
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
    private LinkClipRepository linkClipRepository;

    private Object clipboardContent;

    public CopyPasteRepository(TimelineManagerAccessor timelineManager, UiCommandInterpreterService commandInterpreter, LinkClipRepository linkClipRepository) {
        this.timelineManager = timelineManager;
        this.commandInterpreter = commandInterpreter;
        this.linkClipRepository = linkClipRepository;
    }

    public void copyClip(List<String> clipIds) {
        if (clipIds.size() == 0) {
            return;
        }
        Map<String, List<String>> links = linkClipRepository.getLinkedClips(clipIds);
        Map<String, String> oldTonewClipMap = new HashMap<>();
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
            oldTonewClipMap.put(clipId, timelineClip.getId());
            if (timelineChannel != null && timelineClip != null) {
                TimelinePosition relativePosition = timelineClip.getInterval().getStartPosition().subtract(minimumPosition);
                copiedData.add(new ClipCopyPasteDomain.CopiedClipData(timelineClip, timelineChannel, relativePosition));
            }
        }
        clipboardContent = new ClipCopyPasteDomain(copiedData, relativeEndPosition, linkClipRepository.mapLinksWithChangedClipsIds(oldTonewClipMap, links));
    }

    public void copyRawClips(List<TimelineClip> clips, Map<String, List<String>> links) {
        if (clips.size() == 0) {
            return;
        }
        Map<String, String> oldTonewClipMap = new HashMap<>();
        TimelinePosition minimumPosition = clips.stream().map(clip -> clip.getInterval().getStartPosition()).sorted().findFirst().get();

        TimelinePosition relativeEndPosition = clips.stream()
                .map(a -> a.getInterval().getEndPosition().subtract(minimumPosition))
                .sorted((a, b) -> b.compareTo(a))
                .findFirst()
                .get();

        List<ClipCopyPasteDomain.CopiedClipData> copiedData = new ArrayList<>();
        int i = 0;
        for (var clip : clips) {
            if (i >= timelineManager.getChannels().size()) {
                i = 0;
            }
            TimelineChannel timelineChannel = timelineManager.getChannels().get(i);
            TimelineClip clonedClip = clip.cloneClip(CloneRequestMetadata.ofDefault());
            oldTonewClipMap.put(clip.getId(), clonedClip.getId());
            if (timelineChannel != null && clonedClip != null) {
                TimelinePosition relativePosition = clonedClip.getInterval().getStartPosition().subtract(minimumPosition);
                copiedData.add(new ClipCopyPasteDomain.CopiedClipData(clonedClip, timelineChannel, relativePosition));
            }
            ++i;
        }
        Map<String, List<String>> mappedLinks = linkClipRepository.mapLinksWithChangedClipsIds(oldTonewClipMap, links);
        clipboardContent = new ClipCopyPasteDomain(copiedData, relativeEndPosition, mappedLinks);
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

    public boolean hasClipInClipboard() {
        return clipboardContent != null && clipboardContent instanceof ClipCopyPasteDomain;
    }

    public ClipCopyPasteDomain getClipDomain() {
        return (ClipCopyPasteDomain) clipboardContent;
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
        Map<String, String> oldToNewClipMap = new HashMap<>();
        for (var element : clipCopyPasteDomain.copiedData) {
            TimelineClip clonedClip = element.clipboardContent.cloneClip(CloneRequestMetadata.ofDefault());
            AddExistingClipRequest request = AddExistingClipRequest.builder()
                    .withChannel(element.timelineChannel)
                    .withClipToAdd(clonedClip) // multiple ctrl+v
                    .withPosition(Optional.of(positionToInsertClipsTo.add(element.relativeOffset)))
                    .build();
            oldToNewClipMap.put(element.clipboardContent.getId(), clonedClip.getId());
            AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineManager);
            commands.add(addClipCommand);
        }
        Map<String, List<String>> newLinks = linkClipRepository.mapLinksWithChangedClipsIds(oldToNewClipMap, clipCopyPasteDomain.links);
        linkClipRepository.linkClips(newLinks);
        commandInterpreter.sendWithResult(new CompositeCommand(commands));
    }

    public boolean isEffectOnClipboard() {
        return clipboardContent != null && clipboardContent instanceof EffectCopyPasteDomain;
    }

    public void pasteClipToPosition(String channelId, TimelinePosition actualPosition) {
        TimelinePosition positionToInsertClipsTo = actualPosition;
        TimelineChannel channel = timelineManager.findChannelWithId(channelId).get();
        ClipCopyPasteDomain clipCopyPasteDomain = (ClipCopyPasteDomain) clipboardContent;

        List<TimelineClip> clips = clipCopyPasteDomain.copiedData
                .stream()
                .map(a -> a.clipboardContent)
                .map(a -> a.cloneClip(CloneRequestMetadata.ofDefault()))
                .collect(Collectors.toList());

        Map<String, String> oldToNewClipMap = new HashMap<>();
        for (int i = 0; i < clips.size(); ++i) {
            oldToNewClipMap.put(clipCopyPasteDomain.copiedData.get(i).clipboardContent.getId(), clips.get(i).getId());
        }

        List<ClipChannelPair> additionalClips = new ArrayList<>();
        for (int i = 1; i < clips.size(); ++i) {
            additionalClips.add(new ClipChannelPair(clips.get(i), null));
        }

        AddExistingClipRequest request = AddExistingClipRequest.builder()
                .withChannel(channel)
                .withClipToAdd(clips.get(0))
                .withPosition(Optional.of(positionToInsertClipsTo))
                .withAdditionalClipsToAdd(additionalClips)
                .build();
        AddExistingClipsCommand addClipCommand = new AddExistingClipsCommand(request, timelineManager);

        Map<String, List<String>> newLinks = linkClipRepository.mapLinksWithChangedClipsIds(oldToNewClipMap, clipCopyPasteDomain.links);
        linkClipRepository.linkClips(newLinks);

        commandInterpreter.sendWithResult(addClipCommand);
    }
}

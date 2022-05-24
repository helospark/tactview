package com.helospark.tactview.ui.javafx.clip.chain;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.EffectFactoryChain;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddScaleCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveEffectCommand;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.SelectedNodeRepository;
import com.helospark.tactview.ui.javafx.repository.timelineeditmode.TimelineEditMode;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardClipContextMenuChainItemConfiguration {

    @Bean
    @Order(100)
    public ClipContextMenuChainItem copyMenuItem(CopyPasteRepository copyPasteRepository, TimelineManagerAccessor timelineManagerAccessor) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem copyClip = new MenuItem("Copy");
            List<String> clips = timelineManagerAccessor.findLinkedClipsWithSameInterval(request.getPrimaryClip().getId());
            copyClip.setOnAction(e -> copyPasteRepository.copyClip(clips));
            return copyClip;
        });
    }

    @Bean
    @Order(101)
    public ClipContextMenuChainItem pasteMenuItem(CopyPasteRepository copyPasteRepository) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem pasteEffectOnClipMenuItem = new MenuItem("Paste effect on clip");

            pasteEffectOnClipMenuItem.setOnAction(e -> {
                if (copyPasteRepository.isEffectOnClipboard()) {
                    copyPasteRepository.pasteOnExistingClips(List.of(request.getPrimaryClip().getId()));
                }
            });

            if (!copyPasteRepository.isEffectOnClipboard()) {
                pasteEffectOnClipMenuItem.setDisable(true);
            }

            return pasteEffectOnClipMenuItem;
        });
    }

    @Bean
    @Order(102)
    public ClipContextMenuChainItem deleteAllEffectsMenuItem(CopyPasteRepository copyPasteRepository, UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem deleteEffectsFromClipMenuItem = new MenuItem("Delete all effects");

            deleteEffectsFromClipMenuItem.setOnAction(e -> {
                List<RemoveEffectCommand> removeEffectsCommand = request.getPrimaryClip()
                        .getEffects()
                        .stream()
                        .map(effect -> new RemoveEffectCommand(timelineManager, effect.getId()))
                        .collect(Collectors.toList());

                commandInterpreter.sendWithResult(new CompositeCommand(removeEffectsCommand));
            });

            if (request.getPrimaryClip().getEffects().isEmpty()) {
                deleteEffectsFromClipMenuItem.setDisable(true);
            }

            return deleteEffectsFromClipMenuItem;
        });
    }

    @Bean
    @Order(110)
    public ClipContextMenuChainItem deleteMenuItem(RemoveClipService removeClipService) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem deleteClipMenuItem = new MenuItem("Delete");
            deleteClipMenuItem.setOnAction(e -> removeClipService.removeClip(request.getPrimaryClip().getId()));
            return deleteClipMenuItem;
        });
    }

    @Bean
    @Order(109)
    public ClipContextMenuChainItem rippleDeleteMenuItem(RemoveClipService removeClipService) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem deleteClipMenuItem = new MenuItem("Ripple delete");
            deleteClipMenuItem
                    .setOnAction(e -> removeClipService.rippleDeleteClips(request.getAllClips().stream().map(a -> a.getId()).collect(Collectors.toList()), TimelineEditMode.ALL_CHANNEL_RIPPLE));
            return deleteClipMenuItem;
        });
    }

    @Bean
    @Order(0)
    public ClipContextMenuChainItem scaleToFrameMenuItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, ProjectRepository projectRepository,
            @Qualifier("scaleEffect") EffectFactory scaleFactory, EffectFactoryChain effectFactoryChain, SelectedNodeRepository selectedNodeRepository) {
        return typeSupportingContextMenuItem(VisualTimelineClip.class, request -> {
            MenuItem scaleToImageMenuItem = new MenuItem("Scale to frame");
            scaleToImageMenuItem.setOnAction(e -> {
                List<AddScaleCommand> commands = request.getAllClips()
                        .stream()
                        .map(a -> a.getId())
                        .map(clipId -> AddScaleCommand.builder()
                                .withClipId(clipId)
                                .withProjectRepository(projectRepository)
                                .withScaleEffectFactory(scaleFactory)
                                .withTimelineManager(timelineManager)
                                .withEffectFactoryChain(effectFactoryChain)
                                .build())
                        .collect(Collectors.toList());
                if (commands.size() > 0) {
                    commandInterpreter.sendWithResult(new CompositeCommand(commands));
                }
            });
            return scaleToImageMenuItem;
        });
    }

    private ClipContextMenuChainItem alwaysSupportedContextMenuItem(Function<ClipContextMenuChainItemRequest, MenuItem> factory) {
        return new ClipContextMenuChainItem() {

            @Override
            public boolean supports(ClipContextMenuChainItemRequest request) {
                return true;
            }

            @Override
            @Order(0)
            public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
                return factory.apply(request);
            }
        };
    }

    private ClipContextMenuChainItem typeSupportingContextMenuItem(Class<? extends TimelineClip> supportedType, Function<ClipContextMenuChainItemRequest, MenuItem> factory) {
        return new ClipContextMenuChainItem() {

            @Override
            public boolean supports(ClipContextMenuChainItemRequest request) {
                return supportedType.isAssignableFrom(request.getPrimaryClip().getClass());
            }

            @Override
            public MenuItem createMenu(ClipContextMenuChainItemRequest request) {
                return factory.apply(request);
            }
        };
    }
}

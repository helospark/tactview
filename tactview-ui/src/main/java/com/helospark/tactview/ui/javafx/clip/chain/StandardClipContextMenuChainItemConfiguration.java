package com.helospark.tactview.ui.javafx.clip.chain;

import java.util.List;
import java.util.function.Function;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddScaleCommand;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.uicomponents.ClipCutService;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardClipContextMenuChainItemConfiguration {

    @Bean
    @Order(95)
    public ClipContextMenuChainItem cutCurrentClip(ClipCutService clipCutService) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem copyClip = new MenuItem("Cut clip");
            copyClip.setOnAction(e -> clipCutService.cutClip(request.getPrimaryClip().getId(), true));
            return copyClip;
        });
    }

    @Bean
    @Order(96)
    public ClipContextMenuChainItem cutWithUnlinkCurrentClip(ClipCutService clipCutService) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem copyClip = new MenuItem("Cut only this clip");
            copyClip.setOnAction(e -> clipCutService.cutClip(request.getPrimaryClip().getId(), false));
            return copyClip;
        });
    }

    @Bean
    @Order(100)
    public ClipContextMenuChainItem copyMenuItem(CopyPasteRepository copyPasteRepository) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem copyClip = new MenuItem("Copy");
            copyClip.setOnAction(e -> copyPasteRepository.copyClip(request.getPrimaryClip().getId()));
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
    @Order(110)
    public ClipContextMenuChainItem deleteMenuItem(RemoveClipService removeClipService) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem deleteClipMenuItem = new MenuItem("Delete");
            deleteClipMenuItem.setOnAction(e -> removeClipService.removeClip(request.getPrimaryClip().getId()));
            return deleteClipMenuItem;
        });
    }

    @Bean
    @Order(0)
    public ClipContextMenuChainItem scaleToFrameMenuItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager, ProjectRepository projectRepository,
            @Qualifier("scaleEffect") EffectFactory scaleFactory) {
        return typeSupportingContextMenuItem(VisualTimelineClip.class, request -> {
            MenuItem scaleToImageMenuItem = new MenuItem("Scale to frame");
            scaleToImageMenuItem.setOnAction(e -> {
                AddScaleCommand command = AddScaleCommand.builder()
                        .withClipId(request.getPrimaryClip().getId())
                        .withProjectRepository(projectRepository)
                        .withScaleEffectFactory(scaleFactory)
                        .withTimelineManager(timelineManager)
                        .build();

                commandInterpreter.sendWithResult(command);
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

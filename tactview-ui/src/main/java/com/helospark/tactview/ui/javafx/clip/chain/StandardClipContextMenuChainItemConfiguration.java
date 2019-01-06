package com.helospark.tactview.ui.javafx.clip.chain;

import java.util.function.Function;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.EffectFactory;
import com.helospark.tactview.ui.javafx.RemoveClipService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddScaleCommand;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardClipContextMenuChainItemConfiguration {

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
    public ClipContextMenuChainItem scaleToFrameMenuItem(UiCommandInterpreterService commandInterpreter, TimelineManager timelineManager, ProjectRepository projectRepository,
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

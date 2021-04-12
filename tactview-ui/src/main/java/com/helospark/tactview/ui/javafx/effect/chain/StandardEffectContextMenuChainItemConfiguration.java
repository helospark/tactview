package com.helospark.tactview.ui.javafx.effect.chain;

import java.util.function.Function;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.MoveEffectToChannelCommand;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.uicomponents.util.ExtendsClipToMaximizeLengthService;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardEffectContextMenuChainItemConfiguration {

    @Bean
    @Order(90)
    public EffectContextMenuChainItem moveUpItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem moveUpClip = new MenuItem("Move up");
            int index = timelineManager.findEffectChannel(request.getEffect().getId());
            System.out.println("Up index: " + index);
            if (index <= 0) {
                moveUpClip.setDisable(true);
            } else {
                moveUpClip.setOnAction(e -> commandInterpreter.sendWithResult(new MoveEffectToChannelCommand(timelineManager, request.getEffect().getId(), index - 1)));
            }
            return moveUpClip;
        });
    }

    @Bean
    @Order(91)
    public EffectContextMenuChainItem moveDownItem(UiCommandInterpreterService commandInterpreter, TimelineManagerAccessor timelineManager) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem moveDownClip = new MenuItem("Move down");
            int index = timelineManager.findEffectChannel(request.getEffect().getId());
            int numberOfEffectChannels = timelineManager.getNumberOfEffectChannels(request.getEffect().getId()) - 1;
            System.out.println("Down index: " + index + " " + numberOfEffectChannels);
            if (index >= numberOfEffectChannels) {
                moveDownClip.setDisable(true);
            } else {
                moveDownClip.setOnAction(e -> commandInterpreter.sendWithResult(new MoveEffectToChannelCommand(timelineManager, request.getEffect().getId(), index + 1)));
            }
            return moveDownClip;
        });
    }

    @Bean
    @Order(100)
    public EffectContextMenuChainItem copyMenuItem(CopyPasteRepository copyPasteRepository) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem copyClip = new MenuItem("Copy");
            copyClip.setOnAction(e -> copyPasteRepository.copyEffect(request.getEffect().getId()));
            return copyClip;
        });
    }

    @Bean
    @Order(110)
    public EffectContextMenuChainItem deleteMenuItem(RemoveEffectService removeEffectService) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem deleteClipMenuItem = new MenuItem("Delete");
            deleteClipMenuItem.setOnAction(e -> removeEffectService.removeEffect(request.getEffect().getId()));
            return deleteClipMenuItem;
        });
    }

    @Bean
    @Order(120)
    public EffectContextMenuChainItem maximizeMenuItem(ExtendsClipToMaximizeLengthService extendsClipToMaximizeLengthService, TimelineManagerAccessor timelineManager) {
        return alwaysSupportedContextMenuItem(request -> {
            MenuItem maximizeClipMenuItem = new MenuItem("Maximize");

            timelineManager.findClipForEffect(request.getEffect().getId()).ifPresent(clip -> {
                maximizeClipMenuItem.setOnAction(e -> extendsClipToMaximizeLengthService.extendEffectToClipSize(clip.getId(), request.getEffect()));
            });

            return maximizeClipMenuItem;
        });
    }

    private EffectContextMenuChainItem alwaysSupportedContextMenuItem(Function<EffectContextMenuChainItemRequest, MenuItem> factory) {
        return new EffectContextMenuChainItem() {

            @Override
            public boolean supports(EffectContextMenuChainItemRequest request) {
                return true;
            }

            @Override
            @Order(0)
            public MenuItem createMenu(EffectContextMenuChainItemRequest request) {
                return factory.apply(request);
            }
        };
    }
}

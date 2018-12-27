package com.helospark.tactview.ui.javafx.effect.chain;

import java.util.function.Function;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.RemoveEffectService;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardEffectContextMenuChainItemConfiguration {

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

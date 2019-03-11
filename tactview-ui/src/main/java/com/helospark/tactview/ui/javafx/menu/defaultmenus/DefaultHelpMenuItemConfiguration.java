package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.help.AboutDialogOpener;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.RegenerateAllImagePatternsMessage;

@Configuration
public class DefaultHelpMenuItemConfiguration {
    public static final String HELP_ROOT = "_Help";

    @Bean
    @Order(4990)
    public MenuContribution dropCachesContributionMenuItem(MessagingService messagingService) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, "Drop caches"), e -> {
            messagingService.sendAsyncMessage(new DropCachesMessage());
        });
    }

    @Bean
    @Order(4991)
    public MenuContribution regenerateImagePatternsContributionMenuItem(MessagingService messagingService) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, "Regenerate clip patterns"), e -> {
            messagingService.sendAsyncMessage(new RegenerateAllImagePatternsMessage());
        });
    }

    @Bean
    @Order(5000)
    public MenuContribution aboutContributionMenuItem(AboutDialogOpener aboutDialogOpener) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, "About..."), e -> {
            aboutDialogOpener.openDialog();
        });
    }

}

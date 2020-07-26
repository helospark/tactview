package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.message.DropCachesMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.help.AboutDialogOpener;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SeparatorMenuContribution;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.projectsize.RegenerateAllImagePatternsMessage;
import com.helospark.tactview.ui.javafx.save.UiLoadHandler;

@Configuration
public class DefaultHelpMenuItemConfiguration {
    public static final String HELP_ROOT = "_Help";
    public static final String DEVELOPER_ROOT = "_Developer";

    @Bean
    @Order(4990)
    public SelectableMenuContribution dropCachesContributionMenuItem(MessagingService messagingService) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, DEVELOPER_ROOT, "Drop caches"), e -> {
            messagingService.sendAsyncMessage(new DropCachesMessage());
        });
    }

    @Bean
    @Order(4991)
    public SelectableMenuContribution regenerateImagePatternsContributionMenuItem(MessagingService messagingService) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, DEVELOPER_ROOT, "Regenerate clip patterns"), e -> {
            messagingService.sendAsyncMessage(new RegenerateAllImagePatternsMessage());
        });
    }

    @Bean
    @Order(4992)
    public SelectableMenuContribution clearMemoryManagerContributionMenuItem(MessagingService messagingService, MemoryManager memoryManager) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, DEVELOPER_ROOT, "Clear memory manager"), e -> {
            memoryManager.dropAllBuffers();
        });
    }

    @Bean
    @Order(4993)
    public SelectableMenuContribution reloadStyleSheat(MessagingService messagingService) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, DEVELOPER_ROOT, "Reload stylesheet"), e -> {
            messagingService.sendAsyncMessage(new ReloadStylesheetMessage());
        });
    }

    @Bean
    @Order(4990)
    public SeparatorMenuContribution beforeAboutSeparatorMenuItem(UiLoadHandler loadHandler) {
        return new SeparatorMenuContribution(List.of(HELP_ROOT));
    }

    @Bean
    @Order(5000)
    public SelectableMenuContribution aboutContributionMenuItem(AboutDialogOpener aboutDialogOpener) {
        return new DefaultMenuContribution(List.of(HELP_ROOT, "About..."), e -> {
            aboutDialogOpener.openDialog();
        });
    }

}

package com.helospark.tactview.ui.javafx.menu.defaultmenus;

import java.util.List;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.aware.MainWindowStageAware;
import com.helospark.tactview.ui.javafx.menu.DefaultMenuContribution;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

@Configuration
public class DefaultViewMenuItemConfiguration implements MainWindowStageAware {
    public static final String VIEW_ROOT = "_View";

    private Stage stage;

    @Bean
    @Order(1800)
    public SelectableMenuContribution fullScreenWindowContributionMenuItem(MessagingService messagingService) {
        return new DefaultMenuContribution(List.of(VIEW_ROOT, "Full screen"), e -> {
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("Hit escape to exit fullscreen");
            stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
        });
    }

    @Override
    public void setMainWindowStage(Stage stage) {
        this.stage = stage;
    }

}

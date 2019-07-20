package com.helospark.tactview.ui.javafx.plugin;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.plugin.service.PluginInstallationService;

import javafx.event.ActionEvent;

@Component
@Order(4900)
public class PluginInstallationMenuItemContributor implements MenuContribution {
    private PluginInstallationService pluginInstallationService;

    public PluginInstallationMenuItemContributor(PluginInstallationService pluginInstallationService) {
        this.pluginInstallationService = pluginInstallationService;
    }

    @Override
    public List<String> getPath() {
        return List.of("_Help", "Install plugin");
    }

    @Override
    public void onAction(ActionEvent event) {
        pluginInstallationService.installPlugin();
    }

}

package com.helospark.tactview.ui.javafx.plugin;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.plugin.PluginManager;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.plugin.dialog.InstalledPluginsDialog;

import javafx.event.ActionEvent;

@Component
@Order(4901)
public class InstalledPluginListMenuItemContributor implements MenuContribution {
    private PluginManager pluginManager;

    public InstalledPluginListMenuItemContributor(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public List<String> getPath() {
        return List.of("_Help", "Plugins");
    }

    @Override
    public void onAction(ActionEvent event) {
        new InstalledPluginsDialog(pluginManager).show();;
    }

}

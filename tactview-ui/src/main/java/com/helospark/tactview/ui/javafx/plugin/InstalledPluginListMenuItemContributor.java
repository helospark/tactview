package com.helospark.tactview.ui.javafx.plugin;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.plugin.PluginManager;
import com.helospark.tactview.ui.javafx.menu.MenuContribution;
import com.helospark.tactview.ui.javafx.plugin.dialog.InstalledPluginsDialog;
import com.helospark.tactview.ui.javafx.plugin.service.PluginInstallationService;

import javafx.event.ActionEvent;

@Component
@Order(4901)
public class InstalledPluginListMenuItemContributor implements MenuContribution {
    private PluginManager pluginManager;
    private PluginInstallationService pluginInstallationService;
    private RestartDialogOpener restartDialogOpener;

    public InstalledPluginListMenuItemContributor(PluginManager pluginManager, PluginInstallationService pluginInstallationService, RestartDialogOpener restartDialogOpener) {
        this.pluginManager = pluginManager;
        this.pluginInstallationService = pluginInstallationService;
        this.restartDialogOpener = restartDialogOpener;
    }

    @Override
    public List<String> getPath() {
        return List.of("_Help", "Plugins");
    }

    @Override
    public void onAction(ActionEvent event) {
        InstalledPluginsDialog dialog = new InstalledPluginsDialog(pluginManager, pluginInstallationService, restartDialogOpener);
        dialog.show();
    }

}

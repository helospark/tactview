package com.helospark.tactview.ui.javafx.plugin;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.plugin.PluginManager;
import com.helospark.tactview.ui.javafx.menu.SelectableMenuContribution;
import com.helospark.tactview.ui.javafx.plugin.dialog.InstalledPluginsDialog;
import com.helospark.tactview.ui.javafx.plugin.service.PluginInstallationService;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;

import javafx.event.ActionEvent;

@Component
@Order(4901)
public class InstalledPluginListMenuItemContributor implements SelectableMenuContribution {
    private PluginManager pluginManager;
    private PluginInstallationService pluginInstallationService;
    private RestartDialogOpener restartDialogOpener;
    private StylesheetAdderService stylesheetAdderService;

    public InstalledPluginListMenuItemContributor(PluginManager pluginManager, PluginInstallationService pluginInstallationService, RestartDialogOpener restartDialogOpener,
            StylesheetAdderService stylesheetAdderService) {
        this.pluginManager = pluginManager;
        this.pluginInstallationService = pluginInstallationService;
        this.restartDialogOpener = restartDialogOpener;
        this.stylesheetAdderService = stylesheetAdderService;
    }

    @Override
    public List<String> getPath() {
        return List.of("_Help", "Plugins");
    }

    @Override
    public void onAction(ActionEvent event) {
        InstalledPluginsDialog dialog = new InstalledPluginsDialog(pluginManager, pluginInstallationService, restartDialogOpener, stylesheetAdderService);
        dialog.show();
    }

}

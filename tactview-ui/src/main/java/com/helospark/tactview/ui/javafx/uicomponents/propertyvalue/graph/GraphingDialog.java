package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory.GraphingComponentFactory;

@Component
public class GraphingDialog extends DetachableTab {
    public static final String ID = "graphing-tab";
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 500;

    private GraphingComponent graphingComponent;
    private EffectGraphAccessor effectGraphAccessor;
    private MessagingService messagingService;
    private List<GraphingComponentFactory> menuItemFactories;
    private UiCommandInterpreterService commandInterpreter;
    private StylesheetAdderService stylesheetAdderService;
    private NameToIdRepository nameToIdRepository;

    public GraphingDialog(EffectGraphAccessor effectGraphAccessor, MessagingService messagingService, List<GraphingComponentFactory> menuItemFactories,
            UiCommandInterpreterService commandInterpreter, StylesheetAdderService stylesheetAdderService, NameToIdRepository nameToIdRepository) {
        super(ID);
        this.effectGraphAccessor = effectGraphAccessor;
        this.messagingService = messagingService;
        this.menuItemFactories = menuItemFactories;
        this.commandInterpreter = commandInterpreter;
        this.stylesheetAdderService = stylesheetAdderService;
        this.nameToIdRepository = nameToIdRepository;
        this.setText("graphing");
    }

    public void showProvider(GraphProvider graphProvider) {
        graphingComponent = new GraphingComponent(DEFAULT_WIDTH, DEFAULT_HEIGHT, effectGraphAccessor, messagingService, menuItemFactories, commandInterpreter, stylesheetAdderService,
                nameToIdRepository);
        this.openTab();
        graphingComponent.setGraphProvider(graphProvider);
        graphingComponent.setCameraPositionX(-DEFAULT_WIDTH / 2);
        graphingComponent.setCameraPositionX(-DEFAULT_HEIGHT / 2);
        graphingComponent.setZoom(1.0);

        if (this.getTabPane() != null) {
            graphingComponent.setParent(this.getTabPane().getScene().getWindow());
        }

        graphingComponent.redrawGraphProvider();
    }

    protected void openTab() {
        this.setContent(graphingComponent);
    }
}

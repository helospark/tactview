package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph;

import java.util.List;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory.GraphingComponentFactory;
import com.helospark.tactview.ui.javafx.uicomponents.window.SingletonOpenableWindow;

import javafx.scene.Scene;

@Component
public class GraphingDialog extends SingletonOpenableWindow {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 500;

    private GraphingComponent graphingComponent;
    private EffectGraphAccessor effectGraphAccessor;
    private MessagingService messagingService;
    private List<GraphingComponentFactory> menuItemFactories;
    private UiCommandInterpreterService commandInterpreter;

    public GraphingDialog(EffectGraphAccessor effectGraphAccessor, MessagingService messagingService, List<GraphingComponentFactory> menuItemFactories,
            UiCommandInterpreterService commandInterpreter) {
        this.effectGraphAccessor = effectGraphAccessor;
        this.messagingService = messagingService;
        this.menuItemFactories = menuItemFactories;
        this.commandInterpreter = commandInterpreter;
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    public void open(GraphProvider graphProvider) {
        graphingComponent = new GraphingComponent(DEFAULT_WIDTH, DEFAULT_HEIGHT, effectGraphAccessor, messagingService, menuItemFactories, commandInterpreter);
        graphingComponent.setGraphProvider(graphProvider);
        graphingComponent.setCameraPositionX(-DEFAULT_WIDTH / 2);
        graphingComponent.setCameraPositionX(-DEFAULT_HEIGHT / 2);
        graphingComponent.setZoom(1.0);
        open();

        graphingComponent.setParent(stage);

        graphingComponent.redrawGraphProvider();
    }

    @Override
    public String getWindowId() {
        return "graphing-editor";
    }

    @Override
    protected Scene createScene() {
        return new Scene(graphingComponent, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

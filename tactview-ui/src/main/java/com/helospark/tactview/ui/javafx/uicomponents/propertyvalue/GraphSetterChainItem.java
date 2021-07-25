package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.GraphingComponent;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.GraphingDialog;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory.GraphingComponentFactory;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

@Component
public class GraphSetterChainItem extends TypeBasedPropertyValueSetterChainItem<GraphProvider> implements ScenePostProcessor {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager uiTimelineManager;
    private GraphingDialog graphingDialog;
    private EffectGraphAccessor effectGraphAccessor;
    private MessagingService messagingService;
    private StylesheetAdderService stylesheetAdderService;
    private NameToIdRepository nameToIdRepository;
    private DockableTabRepository dockableTabRepository;

    private Scene scene;

    private List<GraphingComponentFactory> menuItemFactories;

    public GraphSetterChainItem(UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository, UiTimelineManager uiTimelineManager, GraphingDialog graphingDialog, EffectGraphAccessor effectGraphAccessor,
            MessagingService messagingService, List<GraphingComponentFactory> menuItemFactories, StylesheetAdderService stylesheetAdderService, NameToIdRepository nameToIdRepository,
            DockableTabRepository dockableTabRepository) {
        super(GraphProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.graphingDialog = graphingDialog;
        this.effectGraphAccessor = effectGraphAccessor;
        this.messagingService = messagingService;
        this.menuItemFactories = menuItemFactories;
        this.stylesheetAdderService = stylesheetAdderService;
        this.nameToIdRepository = nameToIdRepository;
        this.dockableTabRepository = dockableTabRepository;
    }

    @Override
    protected EffectLine handle(GraphProvider graphingProvider, ValueProviderDescriptor descriptor) {
        Button button = new Button("Open graph window", new Glyph("FontAwesome", FontAwesome.Glyph.DIAMOND));

        GraphingComponent graphingComponent = new GraphingComponent(250, 100, effectGraphAccessor, messagingService, menuItemFactories, commandInterpreter, stylesheetAdderService, nameToIdRepository);
        graphingComponent.setGraphProvider(graphingProvider);
        graphingComponent.setZoom(0.4);
        graphingComponent.setParent(scene.getWindow());
        graphingComponent.redrawGraphProvider();

        VBox vbox = new VBox(button, graphingComponent);

        PrimitiveEffectLine result = PrimitiveEffectLine
                .builder()
                .withCurrentValueProvider(() -> effectParametersRepository.getValueAtAsObject(graphingProvider.getId(), uiTimelineManager.getCurrentPosition()))
                .withVisibleNode(vbox)
                .withDescriptorId(graphingProvider.getId())
                .withEffectParametersRepository(effectParametersRepository)
                .withCommandInterpreter(commandInterpreter)
                .withDescriptor(descriptor)
                .withUpdateFunction(position -> {
                    graphingComponent.redrawGraphProvider();
                })
                .withUpdateFromValue(value -> {
                })
                .build();

        button.setOnMouseClicked(event -> {
            dockableTabRepository.openTab(graphingDialog);
            graphingDialog.showProvider(graphingProvider);
        });

        return result;
    }

    @Override
    public void postProcess(Scene scene) {
        this.scene = scene;
    }

}

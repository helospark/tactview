package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.ConditionalOnProperty;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.OutputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.CameraOutputToV4L2LoopbackElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.realtime.camera.OpencvL4V2LoopbackImplementation;
import com.helospark.tactview.core.timeline.proceduralclip.ProceduralClipFactoryChainItem;
import com.helospark.tactview.core.util.conditional.ConditionalOnPlatform;
import com.helospark.tactview.core.util.conditional.TactviewPlatform;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command.GraphAddNewNodeByReferenceCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command.GraphAddNewNodeByUriCommand;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardCategoryMenuItemConfiguration {

    @Bean
    public GraphingComponentFactory outputGraphComponent(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor) {
        return referenceAwareFactory(commandInterpreter, effectGraphAccessor, "Output", "Effect output", request -> {
            OutputElement outputElement = new OutputElement();
            commandInterpreter.sendWithResult(new GraphAddNewNodeByReferenceCommand(request.provider, effectGraphAccessor, outputElement));
            return outputElement;
        });
    }

    @ConditionalOnPlatform(TactviewPlatform.LINUX)
    @ConditionalOnProperty(property = "tactview.realtime", havingValue = "true")
    @Bean
    public GraphingComponentFactory v4l2LoopbackGraphComponent(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor, OpencvL4V2LoopbackImplementation loopback) {
        return referenceAwareFactory(commandInterpreter, effectGraphAccessor, "Output", "V4L2 loopback", request -> {
            CameraOutputToV4L2LoopbackElement outputElement = new CameraOutputToV4L2LoopbackElement(loopback);
            commandInterpreter.sendWithResult(new GraphAddNewNodeByReferenceCommand(request.provider, effectGraphAccessor, outputElement));
            return outputElement;
        });
    }

    @Bean
    public List<GraphingComponentFactory> effectsGraphComponent(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor, List<StandardEffectFactory> effectFactories) {
        return effectFactories.stream()
                .map(factory -> uriAwareFactory(commandInterpreter, effectGraphAccessor, "Effect", factory.getEffectName(), "effect:" + factory.getId()))
                .collect(Collectors.toList());
    }

    @Bean
    public List<GraphingComponentFactory> proceduralClipsGraphComponent(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor,
            List<ProceduralClipFactoryChainItem> effectFactories) {
        return effectFactories.stream()
                .map(factory -> uriAwareFactory(commandInterpreter, effectGraphAccessor, "Procedural clip", factory.getProceduralClipName(), "clip:" + factory.getProceduralClipId()))
                .collect(Collectors.toList());
    }

    private GraphingComponentFactory referenceAwareFactory(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor, String category, String name,
            Function<GraphingMenuItemRequest, GraphElement> runnable) {
        return new GraphingComponentFactory() {

            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public MenuItem createMenuItem(GraphingMenuItemRequest request) {
                MenuItem menuItem = new MenuItem(name);

                menuItem.setOnAction(e -> {
                    GraphElement result = runnable.apply(request);
                    result.x = request.x;
                    result.y = request.y;
                    request.updateRunnable.run();
                });

                return menuItem;
            }
        };
    }

    private GraphingComponentFactory uriAwareFactory(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor, String category, String name,
            String uri) {
        return new GraphingComponentFactory() {

            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public MenuItem createMenuItem(GraphingMenuItemRequest request) {
                MenuItem menuItem = new MenuItem(name);

                menuItem.setOnAction(e -> {
                    GraphAddNewNodeByUriCommand result = commandInterpreter.sendWithResult(new GraphAddNewNodeByUriCommand(request.provider, effectGraphAccessor, uri)).join();
                    if (result != null) {
                        GraphElement graphElement = request.provider.getEffectGraph().getGraphElements().get(result.getGraphAddedNode());
                        graphElement.x = request.x;
                        graphElement.y = request.y;
                        request.updateRunnable.run();
                    } else {
                        System.out.println("Failed to add " + uri);
                    }
                });

                return menuItem;
            }
        };
    }

}

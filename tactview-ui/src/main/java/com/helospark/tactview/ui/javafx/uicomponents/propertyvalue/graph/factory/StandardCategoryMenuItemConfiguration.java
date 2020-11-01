package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElementFactory;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command.GraphAddNewNodeByUriCommand;

import javafx.scene.control.MenuItem;

@Configuration
public class StandardCategoryMenuItemConfiguration {

    @Bean
    public List<GraphingComponentFactory> uiGraphComponentFactories(UiCommandInterpreterService commandInterpreter, EffectGraphAccessor effectGraphAccessor, List<GraphElementFactory> factories) {
        return factories.stream()
                .filter(a -> !a.isNeedsInputParam())
                .map(a -> {
                    return uriAwareFactory(commandInterpreter, effectGraphAccessor, a.getCategory(), a.getName(), a.getId());
                }).collect(Collectors.toList());
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

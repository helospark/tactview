package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.stringinterpolator.StringInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.StringProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.InterpolatorChangedCommand;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@Component
@Order(40)
public class StringPropertyValueContextMenuItem implements PropertyValueContextMenuItem {
    private LightDiContext context;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public StringPropertyValueContextMenuItem(LightDiContext context, UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        this.context = context;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    public boolean supports(PropertyValueContextMenuRequest request) {
        return request.valueProvider.keyframesEnabled() && request.valueProvider instanceof StringProvider;
    }

    @Override
    public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
        return createInterpolators(request.valueProvider.getId());
    }

    private Menu createInterpolators(String id) {
        Menu menu = new Menu("Change interpolator");

        List<StringInterpolatorFactory> interpolators = context.getListOfBeans(StringInterpolatorFactory.class);
        List<MenuItem> menuItems = interpolators.stream()
                .map(interpolator -> {
                    MenuItem menuItem = new MenuItem(interpolator.getId());
                    menuItem.setOnAction(e -> {
                        InterpolatorChangedCommand interpolatorChangedCommand = InterpolatorChangedCommand.builder()
                                .withDescriptorId(id)
                                .withNewInterpolatorId(interpolator.getId())
                                .withEffectParametersRepository(effectParametersRepository)
                                .build();
                        commandInterpreter.sendWithResult(interpolatorChangedCommand);
                    });
                    return menuItem;
                })
                .collect(Collectors.toList());

        menu.getItems().addAll(menuItems);

        return menu;
    }
}

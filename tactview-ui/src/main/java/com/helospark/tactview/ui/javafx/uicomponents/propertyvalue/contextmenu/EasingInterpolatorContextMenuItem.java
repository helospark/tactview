package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.EaseFunction;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed.MixedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.ChangeEasingCommand;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@Component
@Order(50)
public class EasingInterpolatorContextMenuItem implements PropertyValueContextMenuItem {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public EasingInterpolatorContextMenuItem(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    public boolean supports(PropertyValueContextMenuRequest request) {
        return request.valueProvider.keyframesEnabled()
                && request.valueProvider instanceof DoubleProvider
                && ((DoubleProvider) request.valueProvider).getInterpolator() instanceof MixedDoubleInterpolator
                && ((MixedDoubleInterpolator) ((DoubleProvider) request.valueProvider).getInterpolator()).hasEasingFunctionAt(request.timelinePosition);
    }

    @Override
    public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
        return createInterpolators(request.valueProvider.getId(), request.timelinePosition);
    }

    private Menu createInterpolators(String id, TimelinePosition timelinePosition) {
        Menu menu = new Menu("Change easing");

        List<MenuItem> menuItems = Arrays.stream(EaseFunction.values())
                .map(easing -> {
                    MenuItem menuItem = new MenuItem(easing.getId());
                    menuItem.setOnAction(e -> {
                        ChangeEasingCommand interpolatorChangedCommand = ChangeEasingCommand.builder()
                                .withDescriptorId(id)
                                .withPosition(timelinePosition)
                                .withNewEasingId(easing.getId())
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

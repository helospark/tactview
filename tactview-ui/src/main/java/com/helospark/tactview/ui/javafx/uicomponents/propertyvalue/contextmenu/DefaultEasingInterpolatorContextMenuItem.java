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
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.ChangeDefaultEasingCommand;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;

@Component
@Order(50)
public class DefaultEasingInterpolatorContextMenuItem implements PropertyValueContextMenuItem {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;

    public DefaultEasingInterpolatorContextMenuItem(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository) {
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
    }

    @Override
    public boolean supports(PropertyValueContextMenuRequest request) {
        return request.valueProvider.keyframesEnabled()
                && (request.valueProvider).getInterpolatorClone() instanceof MixedDoubleInterpolator
                && ((MixedDoubleInterpolator) (request.valueProvider).getInterpolatorClone()).hasEasingFunctionAt(request.timelinePosition);
    }

    @Override
    public MenuItem createMenuItem(PropertyValueContextMenuRequest request) {
        return createInterpolators(request.valueProvider.getId(), request.timelinePosition);
    }

    public Menu createInterpolators(String id, TimelinePosition timelinePosition) {
        Menu menu = new Menu("Default easing function");

        List<MenuItem> menuItems = createMenuItems(id, timelinePosition);

        menu.getItems().addAll(menuItems);

        return menu;
    }

    public List<MenuItem> createMenuItems(String id, TimelinePosition timelinePosition) {
        MixedDoubleInterpolator mixedInterpolator = (MixedDoubleInterpolator) effectParametersRepository.getCurrentInterpolatorClone(id);
        List<MenuItem> menuItems = Arrays.stream(EaseFunction.values())
                .map(easing -> {
                    RadioMenuItem menuItem = new RadioMenuItem(easing.getId());
                    menuItem.setOnAction(e -> {
                        ChangeDefaultEasingCommand interpolatorChangedCommand = ChangeDefaultEasingCommand.builder()
                                .withDescriptorId(id)
                                .withNewEasingId(easing.getId())
                                .withEffectParametersRepository(effectParametersRepository)
                                .build();
                        commandInterpreter.sendWithResult(interpolatorChangedCommand);
                    });
                    boolean isSelected = mixedInterpolator.getDefaultEaseFunction().equals(easing);
                    menuItem.setSelected(isSelected);
                    return menuItem;
                })
                .collect(Collectors.toList());
        return menuItems;
    }
}

package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.List;
import java.util.stream.Collectors;

import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.aware.ContextAware;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.factory.functional.DoubleInterpolatorFactory;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.message.KeyframeRemovedRequest;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.CompositeCommand;
import com.helospark.tactview.ui.javafx.commands.impl.InterpolatorChangedCommand;
import com.helospark.tactview.ui.javafx.commands.impl.RemoveKeyframeCommand;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

@Component
public class DoublePropertyValueSetterChainItem extends TypeBasedPropertyValueSetterChainItem<DoubleProvider> implements ContextAware {
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private UiTimelineManager timelineManager;
    private LightDiContext context;

    public DoublePropertyValueSetterChainItem(EffectParametersRepository effectParametersRepository,
            UiCommandInterpreterService commandInterpreter, UiTimelineManager timelineManager) {
        super(DoubleProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
    }

    @Override
    protected PrimitiveEffectLine handle(DoubleProvider doubleProvider, ValueProviderDescriptor descriptor) {
        TextField textField = new TextField();
        textField.getStyleClass().add("double-property-field");

        HBox hbox = new HBox();
        hbox.getChildren().add(textField);

        if (doubleProvider.getSizeFunction().equals(SizeFunction.CLAMP_TO_MIN_MAX)) {
            Slider slider = new Slider();
            slider.setMin(doubleProvider.getMin());
            slider.setMax(doubleProvider.getMax());
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            StringConverter<Number> converter = new NumberStringConverter();
            Bindings.bindBidirectional(textField.textProperty(), slider.valueProperty(), converter);
            hbox.getChildren().add(slider);
        }

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textField.getText())
                .withDescriptorId(doubleProvider.getId())
                .withUpdateFunction(position -> {
                    textField.setText(doubleProviderValueToString(doubleProvider.getId(), position));
                    if (effectParametersRepository.isKeyframeAt(doubleProvider.getId(), position)) {
                        textField.getStyleClass().add("on-keyframe");
                    } else {
                        textField.getStyleClass().remove("on-keyframe");
                    }
                })
                .withUpdateFromValue(value -> textField.setText(String.valueOf(value)))
                .withVisibleNode(hbox)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            result.sendKeyframe(timelineManager.getCurrentPosition());
        });

        MenuItem addKeyframeMenuItem = new MenuItem("Add keyframe");
        addKeyframeMenuItem.setOnAction(e -> result.sendKeyframe(timelineManager.getCurrentPosition()));
        MenuItem removeKeyframeMenuItem = new MenuItem("Remove keyframe");
        removeKeyframeMenuItem.setOnAction(e -> removeKeyframe(doubleProvider));
        MenuItem removeAllAndSet = new MenuItem("Remove all and set");
        removeAllAndSet.setOnAction(e -> removeAllAndSet(result, doubleProvider.getId()));
        Menu menu = createInterpolators(doubleProvider.getId());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(addKeyframeMenuItem, removeKeyframeMenuItem, removeAllAndSet, menu);
        textField.setContextMenu(contextMenu);

        return result;

    }

    private Menu createInterpolators(String id) {
        Menu menu = new Menu("Change interpolator");

        List<DoubleInterpolatorFactory> interpolators = context.getListOfBeans(DoubleInterpolatorFactory.class);
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

    private void removeKeyframe(DoubleProvider doubleProvider) {
        KeyframeRemovedRequest request = KeyframeRemovedRequest.builder()
                .withDescriptorId(doubleProvider.getId())
                .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                .build();
        RemoveKeyframeCommand command = new RemoveKeyframeCommand(effectParametersRepository, request);
        commandInterpreter.sendWithResult(command);
    }

    private void removeAllAndSet(PrimitiveEffectLine result, String id) {
        RemoveAllKeyframeCommand removeAllKeyFrameCommand = new RemoveAllKeyframeCommand(effectParametersRepository, id);

        KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                .withDescriptorId(id)
                .withGlobalTimelinePosition(timelineManager.getCurrentPosition())
                .withValue(result.currentValueProvider.get())
                .build();
        AddKeyframeForPropertyCommand addKeyframeCommand = new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest);

        commandInterpreter.sendWithResult(new CompositeCommand(removeAllKeyFrameCommand, addKeyframeCommand));
    }

    private String doubleProviderValueToString(String id, TimelinePosition position) {
        return effectParametersRepository.getValueAt(id, position);
    }

    @Override
    public void setContext(LightDiContext context) {
        this.context = context;
    }

}

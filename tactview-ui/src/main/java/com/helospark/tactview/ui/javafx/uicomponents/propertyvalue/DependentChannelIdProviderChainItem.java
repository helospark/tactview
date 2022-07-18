package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelineManagerAccessor;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentChannelIdProvider;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.ui.javafx.GlobalTimelinePositionHolder;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.contextmenu.ContextMenuAppender;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

@Component
public class DependentChannelIdProviderChainItem extends TypeBasedPropertyValueSetterChainItem<DependentChannelIdProvider> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependentChannelIdProviderChainItem.class);
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private TimelineManagerAccessor timelineManager;
    private NameToIdRepository nameToIdRepository;
    private GlobalTimelinePositionHolder uiTimelineManager;
    private ContextMenuAppender contextMenuAppender;

    public DependentChannelIdProviderChainItem(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository,
            TimelineManagerAccessor timelineManager, NameToIdRepository nameToIdRepository, GlobalTimelinePositionHolder uiTimelineManager, ContextMenuAppender contextMenuAppender) {
        super(DependentChannelIdProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.nameToIdRepository = nameToIdRepository;
        this.uiTimelineManager = uiTimelineManager;
        this.contextMenuAppender = contextMenuAppender;
    }

    @Override
    protected EffectLine handle(DependentChannelIdProvider stringProvider, ValueProviderDescriptor descriptor) {
        TextField textArea = new TextField();

        Button browseButton = new Button("Browse");
        ContextMenu contextMenu = new ContextMenu();
        browseButton.setContextMenu(contextMenu);

        browseButton.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                contextMenu.getItems().clear();
                timelineManager.getAllChannelIds()
                        .stream()
                        .forEach(id -> {
                            String name = nameToIdRepository.getNameForId(id);
                            MenuItem menuItem = new MenuItem(name);
                            menuItem.setOnAction(e -> {
                                textArea.setText(name);
                            });
                            contextMenu.getItems().addAll(menuItem);
                        });
                contextMenu.show(browseButton, event.getScreenX(), event.getScreenY());
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().add(textArea);
        hbox.getChildren().add(browseButton);

        PrimitiveEffectLine result = PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> textArea.getText())
                .withDescriptorId(stringProvider.getId())
                .withUpdateFunction(position -> {
                    String currentValue = nameToIdRepository.getNameForId(stringProvider.getValueAt(position));
                    textArea.setText(currentValue);
                })
                .withVisibleNode(hbox)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            String id = nameToIdRepository.getIdForName(textArea.getText());
            if (id == null) {
                LOGGER.warn("Unable to set " + textArea.getText() + " because it has no id");
                return;
            }
            KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                    .withDescriptorId(stringProvider.getId())
                    .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                    .withValue(id)
                    .withRevertable(true)
                    .build();
            AddKeyframeForPropertyCommand command = new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest);
            this.commandInterpreter.sendWithResult(command);
        });

        contextMenuAppender.addContextMenu(result, stringProvider, descriptor, hbox);

        return result;
    }

}

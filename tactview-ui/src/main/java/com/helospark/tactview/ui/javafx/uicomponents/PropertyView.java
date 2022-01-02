package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderError;
import com.helospark.tactview.core.timeline.message.AbstractDescriptorsAddedMessage;
import com.helospark.tactview.core.timeline.message.AbstractKeyframeChangedMessage;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeAddedRequest;
import com.helospark.tactview.core.timeline.message.KeyframeEnabledWasChangedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.commands.impl.AddKeyframeForPropertyCommand;
import com.helospark.tactview.ui.javafx.commands.impl.UseKeyframeStatusToggleCommand;
import com.helospark.tactview.ui.javafx.notification.NotificationService;
import com.helospark.tactview.ui.javafx.repository.CleanableMode;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.EffectPropertyPage.Builder;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.DetailsGridChain;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.EffectLine;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.PropertyValueSetterChain;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.message.OpenClipPropertyPageMessage;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.message.OpenEffectPropertyPageMessage;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

@Component
public class PropertyView implements CleanableMode {
    private final Image keyframesOn;
    private final Image keyframesOff;
    private final Image warningImage;

    private VBox propertyWindow;
    private final Map<String, GridPane> details = new HashMap<>();
    private final Map<String, EffectPropertyPage> componentProperties = new HashMap<>();

    private final UiMessagingService messagingService;
    private final UiTimelineManager uiTimelineManager;
    private EffectPropertyPage shownEntries;
    private final PropertyValueSetterChain propertyValueSetterChain;
    private final DetailsGridChain detailsGridChain;
    private final NameToIdRepository nameToIdRepository;
    private final NotificationService notificationService;
    private final UiCommandInterpreterService commandInterpreter;
    private final EffectParametersRepository effectParametersRepository;

    @Slf4j
    private Logger logger;

    public PropertyView(UiMessagingService messagingService, UiTimelineManager uiTimelineManager, PropertyValueSetterChain propertyValueSetterChain,
            DetailsGridChain detailsGridChain, NameToIdRepository nameToIdRepository, NotificationService notificationService, UiCommandInterpreterService commandInterpreter,
            EffectParametersRepository effectParametersRepository) {
        this.messagingService = messagingService;
        this.uiTimelineManager = uiTimelineManager;
        this.propertyValueSetterChain = propertyValueSetterChain;
        this.detailsGridChain = detailsGridChain;
        this.nameToIdRepository = nameToIdRepository;
        this.notificationService = notificationService;
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;

        keyframesOn = new Image(getClass().getResourceAsStream("/clock_on.png"));
        keyframesOff = new Image(getClass().getResourceAsStream("/clock_off.png"));
        warningImage = new Image(getClass().getResourceAsStream("/warning_icon.png"));
    }

    @PostConstruct
    public void init() {
        propertyWindow = new VBox();
        propertyWindow.setId("property-view");
        propertyWindow.setMinWidth(200);

        messagingService.register(ClipAddedMessage.class, message -> {
            String clipId = message.getClipId();
            GridPane grid = detailsGridChain.createDetailsGridForClip(clipId);
            details.put(clipId, grid);
        });
        messagingService.register(EffectAddedMessage.class, message -> {
            String effectId = message.getEffectId();
            GridPane grid = detailsGridChain.createDetailsGridForEffect(effectId);
            details.put(effectId, grid);
        });

        messagingService.register(AbstractDescriptorsAddedMessage.class, message -> Platform.runLater(() -> {
            EffectPropertyPage asd = createBox(message.getDescriptors(), message.getComponentId());
            componentProperties.put(message.getComponentId(), asd);
        }));
        messagingService.register(AbstractKeyframeChangedMessage.class, message -> Platform.runLater(() -> {
            updateValuesAtCurrentPosition();
        }));
        messagingService.register(ClipRemovedMessage.class, message -> {
            getCurrentlyShownComponentId()
                    .filter(a -> a.equals(message.getElementId()))
                    .ifPresent(a -> closeCurrentPage());
        });
        messagingService.register(EffectRemovedMessage.class, message -> {
            getCurrentlyShownComponentId()
                    .filter(a -> a.equals(message.getEffectId()))
                    .ifPresent(a -> closeCurrentPage());
        });
        messagingService.register(KeyframeEnabledWasChangedMessage.class, message -> {
            getEffectPropertyPageForId(message.getContainerId(), message.getKeyframeableEffectId())
                    .ifPresent(a -> {
                        Platform.runLater(() -> {
                            a.accept(message.isUseKeyframes());
                        });
                    });
        });
        messagingService.register(OpenEffectPropertyPageMessage.class, message -> this.showEffectProperties(message.getId()));
        messagingService.register(OpenClipPropertyPageMessage.class, message -> this.showClipProperties(message.getId()));
    }

    private Optional<Consumer<Boolean>> getEffectPropertyPageForId(String containerId, String effectId) {
        return Optional.ofNullable(componentProperties.get(containerId))
                .or(() -> Optional.ofNullable(componentProperties.get(containerId)))
                .map(a -> a.getKeyframeEnabledConsumer().get(effectId));
    }

    static class LocalTitledPaneData {
        int localGridIndex = 0;
        GridPane createdGroup = null;
        TitledPane createdTitledPane = null;
        String createdGroupName = null;
    }

    private EffectPropertyPage createBox(List<ValueProviderDescriptor> descriptors, String id) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("effect-property-grid");
        grid.getRowConstraints().add(new RowConstraints());
        Builder result = EffectPropertyPage.builder()
                .withBox(grid)
                .withComponentId(id);

        addNameField(id, result);

        int globalGridIndex = 1;
        LocalTitledPaneData currentPropertyGroup = null;

        for (int i = 0; i < descriptors.size(); ++i) {
            ValueProviderDescriptor currentDescriptor = descriptors.get(i);
            Optional<String> groupName = currentDescriptor.getGroup();

            int indexToAddNewNode;
            GridPane gridToAddNewNode;

            if (hasPropertyGroup(groupName)) {
                if (currentPropertyGroup != null && currentPropertyGroup.createdGroupName.equals(groupName.get())) {
                    // do nothing
                } else if (currentPropertyGroup != null && !currentPropertyGroup.createdGroupName.equals(groupName.get())) {
                    grid.add(currentPropertyGroup.createdTitledPane, 0, globalGridIndex++, 2, 1);
                    currentPropertyGroup = createTitledPaneDataWithGroupName(groupName.get());
                } else if (currentPropertyGroup == null) {
                    currentPropertyGroup = createTitledPaneDataWithGroupName(groupName.get());
                }

                gridToAddNewNode = currentPropertyGroup.createdGroup;
                indexToAddNewNode = currentPropertyGroup.localGridIndex++;
            } else {
                if (currentPropertyGroup != null) {
                    grid.add(currentPropertyGroup.createdTitledPane, 0, globalGridIndex++, 2, 1);
                    currentPropertyGroup = null;
                }

                gridToAddNewNode = grid;
                indexToAddNewNode = globalGridIndex;
                globalGridIndex++;
            }
            addElement(currentDescriptor, result, indexToAddNewNode, gridToAddNewNode);
        }
        if (currentPropertyGroup != null) {
            grid.add(currentPropertyGroup.createdTitledPane, 0, globalGridIndex++, 2, 1);
        }
        return result.build();
    }

    private boolean hasPropertyGroup(Optional<String> group) {
        return group.isPresent();
    }

    private LocalTitledPaneData createTitledPaneDataWithGroupName(String groupName) {
        LocalTitledPaneData titledPaneData = new LocalTitledPaneData();
        titledPaneData.createdGroup = new GridPane();
        titledPaneData.createdGroup.getStyleClass().add("effect-property-grid");
        titledPaneData.createdGroupName = groupName;
        titledPaneData.createdTitledPane = new TitledPane(titledPaneData.createdGroupName, titledPaneData.createdGroup);
        titledPaneData.createdTitledPane.getStyleClass().add("effect-property-group");
        titledPaneData.localGridIndex = 0;
        return titledPaneData;
    }

    private void addNameField(String id, Builder result) {
        TextField nameField = new TextField();
        Button button = new Button();
        button.setText("update");
        button.setOnAction(a -> {
            if (!nameToIdRepository.containsName(nameField.getText())) {
                nameToIdRepository.addNameForId(nameField.getText(), id);
            } else {
                notificationService.showWarning("Unable to update", "Name already used");
            }
        });
        GridPane hbox = new GridPane();
        hbox.add(nameField, 0, 0);
        hbox.add(button, 1, 0);

        result.getBox().add(new Label("name"), 0, 0);
        result.getBox().add(hbox, 1, 0);
        result.addUpdateFunctions(position -> {
            if (nameToIdRepository.hasNameForId(id)) {
                nameField.setText(nameToIdRepository.getNameForId(id));
            }
        });
    }

    private void addElement(ValueProviderDescriptor descriptor, Builder result, int line, GridPane currentGridLocation) {
        logger.debug("Adding " + descriptor + " at index " + line);
        HBox labelBox = new HBox(10);
        Label label = new Label(descriptor.getName());
        labelBox.getChildren().add(label);

        Label warningImageLabel = createWarningLabel();

        labelBox.getChildren().add(warningImageLabel);

        KeyframeableEffect keyframeableEffect = descriptor.getKeyframeableEffect();
        boolean supportsKeyframes = keyframeableEffect.supportsKeyframes();
        if (supportsKeyframes) {
            ImageView imageView = createKeyframeSupportImageNode(result, keyframeableEffect);
            labelBox.getChildren().add(imageView);
        }

        EffectLine keyframeChange = createKeyframeUi(descriptor);

        Node key = keyframeChange.getVisibleNode();
        key.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.INSERT)) {
                KeyframeAddedRequest keyframeRequest = KeyframeAddedRequest.builder()
                        .withDescriptorId(keyframeChange.getDescriptorId())
                        .withGlobalTimelinePosition(uiTimelineManager.getCurrentPosition())
                        .withValue(keyframeChange.getCurrentValueSupplier().get())
                        .withRevertable(true)
                        .build();

                commandInterpreter.sendWithResult(new AddKeyframeForPropertyCommand(effectParametersRepository, keyframeRequest));

                logger.info("Keyframe added");
                event.consume();
            }
        });

        currentGridLocation.add(labelBox, 0, line);
        currentGridLocation.add(key, 1, line);

        result.addUpdateFunctions(currentTime -> Platform.runLater(() -> {
            keyframeChange.updateUi(currentTime);
            if (descriptor != null && descriptor.getShowPredicate().isPresent()) {
                Boolean visible = descriptor.getShowPredicate().get().apply(currentTime);

                setVisibleValue(labelBox, visible);
                setVisibleValue(key, visible);
            }
            if (descriptor != null && descriptor.getValueProviderValidator().isPresent()) {
                List<ValueProviderError> errors = descriptor.getValueProviderValidator().get().apply(currentTime);
                if (errors != null && errors.size() > 0) {
                    setVisibleValue(warningImageLabel, true);
                    String errorMessage = errors.stream()
                            .map(error -> error.getErrorMessage())
                            .collect(Collectors.joining("\n"));
                    Tooltip tooltip = new Tooltip(errorMessage);
                    tooltip.setShowDelay(javafx.util.Duration.millis(0));
                    warningImageLabel.setTooltip(tooltip);
                } else {
                    setVisibleValue(warningImageLabel, false);
                }
            }
        }));
    }

    private Label createWarningLabel() {
        ImageView warningImageView = new ImageView(warningImage);
        warningImageView.setFitWidth(16);
        warningImageView.setFitHeight(16);
        Label warningImageLabel = new Label();
        setVisibleValue(warningImageLabel, false);
        warningImageLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        warningImageLabel.setGraphic(warningImageView);
        return warningImageLabel;
    }

    private void setVisibleValue(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private ImageView createKeyframeSupportImageNode(Builder builder, KeyframeableEffect keyframeableEffect) {
        boolean keyframesEnabled = keyframeableEffect.keyframesEnabled();
        ImageView imageView = new ImageView();
        imageView.setPickOnBounds(true);

        changeImage(keyframesEnabled, imageView);
        builder.addKeyframeEnabledConsumer(keyframeableEffect.getId(), enabled -> changeImage(enabled, imageView));

        imageView.setOnMouseReleased(e -> {
            UseKeyframeStatusToggleCommand command = new UseKeyframeStatusToggleCommand(effectParametersRepository, keyframeableEffect.getId());

            commandInterpreter.sendWithResult(command);
        });

        return imageView;
    }

    private void changeImage(boolean keyframesEnabled, ImageView imageView) {
        Image imageToUse = keyframesEnabled ? keyframesOn : keyframesOff;
        imageView.setImage(imageToUse);
    }

    private EffectLine createKeyframeUi(ValueProviderDescriptor descriptor) {
        return propertyValueSetterChain.create(descriptor);
    }

    public VBox getPropertyWindow() {
        return propertyWindow;
    }

    public void showEffectProperties(String effectId) {
        showComponentProperties(effectId);
    }

    public void showClipProperties(String clipId) {
        showComponentProperties(clipId);
    }

    public void showComponentProperties(String componentId) {
        showProperties(componentProperties.get(componentId), componentId);
    }

    private void showProperties(EffectPropertyPage shownEntries2, String id) {
        shownEntries = shownEntries2;
        propertyWindow.getChildren().clear();
        GridPane dataGrid = details.get(id);
        if (dataGrid != null) {
            VBox vbox = new VBox();
            vbox.getStyleClass().add("description-vbox");
            vbox.getChildren().addAll(dataGrid, new Separator());

            propertyWindow.getChildren().add(vbox);
        }
        if (shownEntries2 != null) {
            propertyWindow.getChildren().add(shownEntries.getBox());
            shownEntries2.getUpdateFunctions().stream().forEach(a -> a.accept(uiTimelineManager.getCurrentPosition()));
        } else {
            logger.error("Effect not found, should not happen");
        }
    }

    public void clearProperties() {
        shownEntries = null;
    }

    public void updateValuesAtCurrentPosition() {
        updateValues(uiTimelineManager.getCurrentPosition());
    }

    public void updateValues(TimelinePosition position) {
        if (shownEntries != null) {
            shownEntries.getUpdateFunctions().stream().forEach(updateFunction -> updateFunction.accept(position));
        }
    }

    public Optional<String> getCurrentlyShownComponentId() {
        return Optional.ofNullable(shownEntries)
                .map(a -> a.getComponentId());
    }

    public void closeCurrentPage() {
        if (shownEntries != null) {
            shownEntries = null;
            Platform.runLater(() -> propertyWindow.getChildren().clear());
        }
    }

    @Override
    public void clean() {
        closeCurrentPage();
    }

}

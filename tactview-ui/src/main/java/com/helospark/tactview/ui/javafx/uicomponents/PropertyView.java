package com.helospark.tactview.ui.javafx.uicomponents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.message.ClipAddedMessage;
import com.helospark.tactview.core.timeline.message.ClipDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.ClipRemovedMessage;
import com.helospark.tactview.core.timeline.message.EffectAddedMessage;
import com.helospark.tactview.core.timeline.message.EffectDescriptorsAdded;
import com.helospark.tactview.core.timeline.message.EffectRemovedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyAddedMessage;
import com.helospark.tactview.core.timeline.message.KeyframeSuccesfullyRemovedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.notification.NotificationService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.uicomponents.EffectPropertyPage.Builder;
import com.helospark.tactview.ui.javafx.uicomponents.detailsdata.DetailsGridChain;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.EffectLine;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.PropertyValueSetterChain;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class PropertyView {
    private final Image keyframesOn;
    private final Image keyframesOff;

    private FlowPane propertyWindow;
    private Map<String, GridPane> details = new HashMap<>();
    private Map<String, EffectPropertyPage> effectProperties = new HashMap<>();
    private Map<String, EffectPropertyPage> clipProperties = new HashMap<>();

    private MessagingService messagingService;
    private UiTimelineManager uiTimelineManager;
    private EffectPropertyPage shownEntries;
    private PropertyValueSetterChain propertyValueSetterChain;
    private DetailsGridChain detailsGridChain;
    private NameToIdRepository nameToIdRepository;
    private NotificationService notificationService;

    @Slf4j
    private Logger logger;

    public PropertyView(MessagingService messagingService, UiTimelineManager uiTimelineManager, PropertyValueSetterChain propertyValueSetterChain,
            DetailsGridChain detailsGridChain, NameToIdRepository nameToIdRepository, NotificationService notificationService) {
        this.messagingService = messagingService;
        this.uiTimelineManager = uiTimelineManager;
        this.propertyValueSetterChain = propertyValueSetterChain;
        this.detailsGridChain = detailsGridChain;
        this.nameToIdRepository = nameToIdRepository;
        this.notificationService = notificationService;

        keyframesOn = new Image(getClass().getResourceAsStream("/clock_on.png"));
        keyframesOff = new Image(getClass().getResourceAsStream("/clock_off.png"));
    }

    @PostConstruct
    public void init() {
        propertyWindow = new FlowPane();
        propertyWindow.setId("property-view");
        propertyWindow.setPrefWidth(200);

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

        messagingService.register(EffectDescriptorsAdded.class, message -> Platform.runLater(() -> {
            EffectPropertyPage asd = createBox(message.getDescriptors(), message.getEffectId());
            effectProperties.put(message.getEffectId(), asd);
        }));
        messagingService.register(ClipDescriptorsAdded.class, message -> Platform.runLater(() -> {
            EffectPropertyPage asd = createBox(message.getDescriptors(), message.getClipId());
            clipProperties.put(message.getClipId(), asd);
        }));
        messagingService.register(KeyframeSuccesfullyAddedMessage.class, message -> Platform.runLater(() -> {
            updateValuesAtCurrentPosition();
        }));
        messagingService.register(KeyframeSuccesfullyRemovedMessage.class, message -> Platform.runLater(() -> {
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
    }

    private EffectPropertyPage createBox(List<ValueProviderDescriptor> descriptors, String id) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("effect-property-grid");
        Builder result = EffectPropertyPage.builder()
                .withBox(grid)
                .withComponentId(id);

        addNameField(id, result);

        for (int i = 0; i < descriptors.size(); ++i) {
            addElement(descriptors.get(i), result, i + 1);
        }
        return result.build();
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
        HBox hbox = new HBox();
        hbox.getChildren().addAll(nameField, button);

        result.getBox().add(new Label("name"), 0, 0);
        result.getBox().add(hbox, 1, 0);
        result.addUpdateFunctions(position -> {
            if (nameToIdRepository.hasNameForId(id)) {
                nameField.setText(nameToIdRepository.getNameForId(id));
            }
        });
    }

    private void addElement(ValueProviderDescriptor descriptor, Builder result, int line) {
        HBox labelBox = new HBox(10);
        Label label = new Label(descriptor.getName());
        labelBox.getChildren().add(label);

        KeyframeableEffect keyframeableEffect = descriptor.getKeyframeableEffect();
        boolean supportsKeyframes = keyframeableEffect.supportsKeyframes();
        if (supportsKeyframes) {
            ImageView imageView = createKeyframeSupportImageNode(keyframeableEffect);
            labelBox.getChildren().add(imageView);
        }

        EffectLine keyframeChange = createKeyframeUi(descriptor);

        Node key = keyframeChange.getVisibleNode();
        key.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.INSERT)) {
                keyframeChange.sendKeyframe(uiTimelineManager.getCurrentPosition());
                logger.info("Keyframe added");
                event.consume();
            }
        });

        result.getBox().add(labelBox, 0, line);
        result.getBox().add(key, 1, line);

        result.addUpdateFunctions(currentTime -> Platform.runLater(() -> keyframeChange.updateUi(currentTime)));
    }

    private ImageView createKeyframeSupportImageNode(KeyframeableEffect keyframeableEffect) {
        boolean keyframesEnabled = keyframeableEffect.keyframesEnabled();
        ImageView imageView = new ImageView();
        changeImage(keyframesEnabled, imageView);

        imageView.setOnMouseClicked(e -> {
            boolean currentStatus = keyframeableEffect.keyframesEnabled();
            keyframeableEffect.setUseKeyframes(!currentStatus); // TODO: command pattern and message
            changeImage(keyframeableEffect.keyframesEnabled(), imageView);
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

    public FlowPane getPropertyWindow() {
        return propertyWindow;
    }

    public void showEffectProperties(String effectId) {
        showProperties(effectProperties.get(effectId), effectId);
    }

    public void showClipProperties(String clipId) {
        showProperties(clipProperties.get(clipId), clipId);
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
            System.out.println("Effect not found, should not happen");
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

}

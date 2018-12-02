package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue;

import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineManager;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.EffectParametersRepository;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DependentClipProvider;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.repository.NameToIdRepository;
import com.helospark.tactview.ui.javafx.repository.UiProjectRepository;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

@Component
public class DependentClipProviderChainItem extends TypeBasedPropertyValueSetterChainItem<DependentClipProvider> {
    private static final int IMAGE_PREVIEW_SIZE = 35;
    private UiCommandInterpreterService commandInterpreter;
    private EffectParametersRepository effectParametersRepository;
    private TimelineManager timelineManager;
    private ByteBufferToJavaFxImageConverter imageConverter;
    private UiProjectRepository uiProjectRepository;
    private NameToIdRepository nameToIdRepository;

    private Image noLayerMaskImage;

    public DependentClipProviderChainItem(UiCommandInterpreterService commandInterpreter, EffectParametersRepository effectParametersRepository,
            TimelineManager timelineManager, ByteBufferToJavaFxImageConverter imageConverter, UiProjectRepository uiProjectRepository,
            NameToIdRepository nameToIdRepository) {
        super(DependentClipProvider.class);
        this.commandInterpreter = commandInterpreter;
        this.effectParametersRepository = effectParametersRepository;
        this.timelineManager = timelineManager;
        this.imageConverter = imageConverter;
        this.uiProjectRepository = uiProjectRepository;
        this.nameToIdRepository = nameToIdRepository;

        noLayerMaskImage = new Image(getClass().getResourceAsStream("/noLayerMask.png"));
    }

    @Override
    protected EffectLine handle(DependentClipProvider stringProvider) {
        TextField textArea = new TextField();

        Button browseButton = new Button("Browse");
        ContextMenu contextMenu = new ContextMenu();
        browseButton.setContextMenu(contextMenu);

        browseButton.setOnMouseClicked(event -> {
            contextMenu.getItems().clear();
            timelineManager.getAllClipIds()
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
        });

        ImageView imageView = new ImageView();
        imageView.prefWidth(40);
        imageView.prefHeight(27);

        HBox hbox = new HBox();
        hbox.getChildren().add(imageView);
        hbox.getChildren().add(textArea);
        hbox.getChildren().add(browseButton);

        return PrimitiveEffectLine.builder()
                .withCurrentValueProvider(() -> nameToIdRepository.getIdForName(textArea.getText()))
                .withDescriptorId(stringProvider.getId())
                .withUpdateFunction(position -> {
                    String currentValue = nameToIdRepository.getNameForId(stringProvider.getValueAt(position));
                    textArea.setText(currentValue);
                    renderFrameTo(imageView, currentValue, position);
                })
                .withVisibleNode(hbox)
                .withCommandInterpreter(commandInterpreter)
                .withEffectParametersRepository(effectParametersRepository)
                .build();
    }

    private void renderFrameTo(ImageView imageView, String currentValue, TimelinePosition position) {
        timelineManager.findClipById(currentValue)
                .ifPresentOrElse(a -> {
                    CompletableFuture.supplyAsync(() -> drawImage(a, position))
                            .thenAccept(image -> drawImage(imageView, image));
                }, () -> drawImage(imageView, noLayerMaskImage));
    }

    private void drawImage(ImageView imageView, Image image) {
        Platform.runLater(() -> {
            imageView.setImage(image);
        });
    }

    private Image drawImage(TimelineClip clip, TimelinePosition position) {
        if (clip instanceof VisualTimelineClip) {
            GetFrameRequest frameRequest = GetFrameRequest.builder()
                    .withApplyEffects(true)
                    .withExpectedWidth(IMAGE_PREVIEW_SIZE) // TODO: aspect ratio
                    .withExpectedHeight(27)
                    .withPosition(position)
                    .withScale(uiProjectRepository.getScaleFactor() / ((double) uiProjectRepository.getPreviewWidth() / IMAGE_PREVIEW_SIZE))
                    .build();
            ReadOnlyClipImage result = ((VisualTimelineClip) clip).getFrame(frameRequest);
            return imageConverter.convertToJavafxImage(result.getBuffer(), result.getWidth(), result.getHeight());
        } else {
            throw new IllegalStateException("Other formats not supported");
        }
    }

}

package com.helospark.tactview.ui.javafx.uicomponents.window;

import static com.helospark.tactview.core.util.async.RunnableExceptionLoggerDecorator.withExceptionLogging;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveLoadContributor;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.ClipFactoryChain;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.ui.javafx.repository.CopyPasteRepository;
import com.helospark.tactview.ui.javafx.repository.copypaste.ClipCopyPasteDomain;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.impl.TrimmingDockableTab;
import com.helospark.tactview.ui.javafx.tabs.listener.TabCloseListener;
import com.helospark.tactview.ui.javafx.tabs.listener.TabOpenListener;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.AudioImagePatternService;
import com.helospark.tactview.ui.javafx.uicomponents.window.projectmedia.ProjectMediaElement;
import com.helospark.tactview.ui.javafx.uicomponents.window.projectmedia.ThumbnailCreator;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

@Component
public class ProjectMediaWindow extends DetachableTab implements TabOpenListener, TabCloseListener, ResettableBean, SaveLoadContributor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectMediaWindow.class);
    public static final String PROJECT_MEDIA_ENTRY = "project-media-entry";
    private static final BigDecimal defaultImagePositionPercent = new BigDecimal("0.1");
    private static final int ELEMENT_WIDTH = 120;
    private static final int NUMBER_OF_PREVIEW_FRAMES = 20;
    public static final String ID = "project-media-window";

    private FlowPane pane;
    private ScrollPane scrollPane;
    ContextMenu elementContextMenu = null;

    private ThumbnailCreator thumbnailCreator;
    private ClipFactoryChain clipFactoryChain;
    private ThreadPoolExecutor executorService;
    private AlertDialogFactory alertDialogFactory;
    private CopyPasteRepository copyPasteRepository;

    List<ProjectMediaElement> elements = new ArrayList<>();

    public ProjectMediaWindow(ClipFactoryChain clipFactoryChain, MemoryManager memoryManager, ByteBufferToJavaFxImageConverter imageConverter,
            AudioImagePatternService audioImagePatternService, @Qualifier("longRunningTaskExecutorService") ThreadPoolExecutor executorService,
            AlertDialogFactory alertDialogFactory, CopyPasteRepository copyPasteRepository, ThumbnailCreator thumbnailCreator) {
        super(ID);
        this.clipFactoryChain = clipFactoryChain;
        this.thumbnailCreator = thumbnailCreator;
        this.executorService = executorService;
        this.alertDialogFactory = alertDialogFactory;
        this.copyPasteRepository = copyPasteRepository;
    }

    protected void openTab() {
        scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("clips-window-pane");

        pane = new FlowPane();

        scrollPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            List<File> files = db.getFiles();
            String dbString = db.getString();
            boolean hasFile = files != null && !files.isEmpty();
            if (hasFile) {
                boolean success = false;
                for (int i = 0; i < files.size(); ++i) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    File file = files.get(i);
                    AddClipRequest request = AddClipRequest.builder()
                            .withFilePath(file.getAbsolutePath())
                            .withPosition(TimelinePosition.ofZero())
                            .withChannelId("")
                            .build();
                    List<TimelineClip> templateClips = clipFactoryChain.createClips(request);
                    if (templateClips.size() > 0) {
                        String label = file.getName();
                        addClips(templateClips, label);
                        success = true;
                    }
                }
                if (success) {
                    event.consume();
                    db.clear();
                }
            } else if (dbString.equals(TrimmingDockableTab.TRIMMING_MEDIA_ENTRY)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                ProjectMediaElement content = (ProjectMediaElement) event.getDragboard().getContent(DataFormat.RTF);
                addClips(content.getTemplateClips(), content.getLabel());
                event.consume();
                db.clear();
            }
        });
        scrollPane.setOnMouseClicked(event -> {
            if (elementContextMenu != null) {
                elementContextMenu.hide();
            }
        });

        refreshPaneContent();

        ContextMenu contextMenu = new ContextMenu();
        scrollPane.setContextMenu(contextMenu);

        scrollPane.setContent(pane);

        this.setContent(scrollPane);
        this.setText("Project media");
    }

    public void addClips(List<TimelineClip> templateClips, String label) {
        Image defaultImage = getDefaultImageFor(templateClips);
        ProjectMediaElement element = new ProjectMediaElement(UUID.randomUUID().toString(), templateClips, defaultImage, label);
        elements.add(element);
        if (getFirstClipOfType(templateClips, VisualTimelineClip.class).isPresent()) {
            executorService.execute(withExceptionLogging(() -> fillImages(element)));
        }
        if (pane != null) {
            pane.getChildren().add(createEntry(element));
        }
    }

    private Image getDefaultImageFor(List<TimelineClip> templateClips) {
        return thumbnailCreator.getImageFor(templateClips, templateClips.get(0).getInterval().getEndPosition().multiply(defaultImagePositionPercent), ELEMENT_WIDTH);
    }

    private <T extends TimelineClip> Optional<T> getFirstClipOfType(List<TimelineClip> templateClips, Class<T> type) {
        return templateClips.stream().filter(clip -> type.isAssignableFrom(clip.getClass())).map(clip -> type.cast(clip)).findFirst();
    }

    private Node createEntry(ProjectMediaElement element) {
        VBox vbox = new VBox();
        vbox.getStyleClass().add(PROJECT_MEDIA_ENTRY);

        ImageView imageView = new ImageView(element.getDefaultImage());
        imageView.maxWidth(ELEMENT_WIDTH);
        Label text = new Label(element.getLabel());
        text.setAlignment(Pos.TOP_CENTER);
        text.setWrapText(true);
        text.setMaxWidth(ELEMENT_WIDTH);

        vbox.getChildren().add(imageView);
        vbox.getChildren().add(text);

        vbox.setUserData(element);

        vbox.setOnMouseMoved(event -> {
            double scaler = event.getX() / vbox.getWidth();
            List<Image> previewImages = element.getPreviewImages();
            int imageIndex = (int) (scaler * NUMBER_OF_PREVIEW_FRAMES);
            if (imageIndex >= 0 && imageIndex < previewImages.size()) {
                Image imageToSet = previewImages.get(imageIndex);
                imageView.setImage(imageToSet);
            }
        });
        vbox.setOnMouseExited(event -> {
            imageView.setImage(element.getDefaultImage());
        });
        vbox.setOnDragDetected(event -> {
            Dragboard db = vbox.startDragAndDrop(TransferMode.ANY);
            db.setDragView(element.getDefaultImage());

            ClipboardContent content = new ClipboardContent();
            content.putString(PROJECT_MEDIA_ENTRY);
            content.put(DataFormat.RTF, element);
            db.setContent(content);

            event.consume();
        });

        vbox.setOnMouseClicked(event -> {
            if (elementContextMenu != null) {
                elementContextMenu.hide();
            }
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                elementContextMenu = createContextMenu(element);
                elementContextMenu.show(this.pane, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });

        return vbox;
    }

    private ContextMenu createContextMenu(ProjectMediaElement element) {
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> {
            Optional<String> result = alertDialogFactory.showTextInputDialog("Rename", "Rename", element.getLabel());
            if (result.isPresent()) {
                element.setLabel(result.get());
            }
            refreshPaneContent();
        });
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            Map<String, List<String>> links = new HashMap<>();
            for (var clip : element.getTemplateClips()) {
                List<String> allLinks = element.getTemplateClips().stream().map(a -> a.getId()).collect(Collectors.toList());
                links.put(clip.getId(), allLinks);
            }

            copyPasteRepository.copyRawClips(element.getTemplateClips(), links);
        });
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> {
            if (copyPasteRepository.hasClipInClipboard()) {
                ClipCopyPasteDomain clipDomain = copyPasteRepository.getClipDomain();
                List<TimelineClip> clips = clipDomain.copiedData.stream()
                        .map(data -> data.clipboardContent)
                        .collect(Collectors.toList());
                String label = clips.get(0).getClass().getSimpleName();
                addClips(clips, label);
                refreshPaneContent();
            }
        });
        if (!copyPasteRepository.hasClipInClipboard()) {
            pasteItem.setDisable(true);
        }
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            elements.remove(element);
            refreshPaneContent();
        });
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(renameItem);
        contextMenu.getItems().add(copyItem);
        contextMenu.getItems().add(pasteItem);
        contextMenu.getItems().add(deleteItem);
        return contextMenu;
    }

    private void fillImages(ProjectMediaElement element) {
        List<Image> images = new ArrayList<>(NUMBER_OF_PREVIEW_FRAMES);
        TimelineLength length = element.getTemplateClips().get(0).getInterval().getLength();
        for (int i = 0; i < NUMBER_OF_PREVIEW_FRAMES; ++i) {
            double scaler = (double) i / NUMBER_OF_PREVIEW_FRAMES;
            Image image = thumbnailCreator.getImageFor(element.getTemplateClips(), length.toPosition().multiply(new BigDecimal(scaler)), ELEMENT_WIDTH);
            images.add(image);
        }
        TimelineLength newLength = element.getTemplateClips().get(0).getInterval().getLength();
        if (length.equals(newLength)) {
            element.setPreviewImages(images);
        }
    }

    @Override
    public void tabOpened() {
        this.openTab();
    }

    @Override
    public void tabClosed() {
    }

    @Override
    public void resetDefaults() {
        if (pane != null) {
            pane.getChildren().clear();
        }
        elements.clear();
    }

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent, SaveMetadata saveMetadata) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (var element : elements) {
            Map<String, Object> savedElementFragment = new HashMap<>();
            savedElementFragment.put("id", element.getId());
            savedElementFragment.put("label", element.getLabel());
            List<Object> clips = new ArrayList<>();
            for (var clip : element.getTemplateClips()) {
                Object savedClipContent = clip.generateSavedContent(saveMetadata);
                clips.add(savedClipContent);
            }
            savedElementFragment.put("clips", clips);
            result.add(savedElementFragment);
        }

        generatedContent.put("project_media", result);
    }

    @Override
    public void loadFrom(JsonNode tree, LoadMetadata metadata) {
        JsonNode list = tree.get("project_media");
        for (int i = 0; i < list.size(); ++i) {
            try {
                JsonNode listElement = list.get(i);
                String id = listElement.get("id").asText();
                String label = listElement.get("label").asText();
                List<TimelineClip> templateClips = new ArrayList<>();
                for (int clipIndex = 0; clipIndex < listElement.get("clips").size(); ++clipIndex) {
                    templateClips.add(clipFactoryChain.restoreClip(listElement.get("clips").get(clipIndex), metadata));
                }

                Image defaultImage = getDefaultImageFor(templateClips);
                ProjectMediaElement result = new ProjectMediaElement(id, templateClips, defaultImage, label);
                if (getFirstClipOfType(templateClips, VisualTimelineClip.class).isPresent()) {
                    executorService.execute(withExceptionLogging(() -> fillImages(result)));
                }
                elements.add(result);
            } catch (Exception e) {
                LOGGER.warn("Error while loading project media at index {}", i, e);
            }
        }
        Platform.runLater(() -> refreshPaneContent());
    }

    private void refreshPaneContent() {
        if (pane != null) {
            pane.getChildren().clear();
            for (var element : elements) {
                pane.getChildren().add(createEntry(element));
            }
        }
    }

}

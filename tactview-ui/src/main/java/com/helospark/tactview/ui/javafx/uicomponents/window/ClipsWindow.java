package com.helospark.tactview.ui.javafx.uicomponents.window;

import static com.helospark.tactview.core.util.async.RunnableExceptionLoggerDecorator.withExceptionLogging;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.VisualMediaMetadata;
import com.helospark.tactview.core.decoder.framecache.MemoryManager;
import com.helospark.tactview.core.timeline.AddClipRequest;
import com.helospark.tactview.core.timeline.AudibleTimelineClip;
import com.helospark.tactview.core.timeline.ClipFactoryChain;
import com.helospark.tactview.core.timeline.GetFrameRequest;
import com.helospark.tactview.core.timeline.TimelineClip;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.ui.javafx.tabs.listener.TabCloseListener;
import com.helospark.tactview.ui.javafx.tabs.listener.TabOpenListener;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;
import com.helospark.tactview.ui.javafx.uicomponents.pattern.AudioImagePatternService;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

@Component
public class ClipsWindow extends DetachableTab implements TabOpenListener, TabCloseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClipsWindow.class);
    public static final String CLIP_ENTRY = "clip-entry";
    private static final BigDecimal defaultImagePositionPercent = new BigDecimal("0.1");
    private static final int ELEMENT_WIDTH = 120;
    private static final int NUMBER_OF_PREVIEW_FRAMES = 20;
    public static final String ID = "clips-window";

    private boolean isTabOpen = false;

    private ClipFactoryChain clipFactoryChain;
    private MemoryManager memoryManager;
    private ByteBufferToJavaFxImageConverter imageConverter;
    private AudioImagePatternService audioImagePatternService;
    private ThreadPoolExecutor executorService;

    List<ClipsWindowsElement> elements = new ArrayList<>();

    public ClipsWindow(ClipFactoryChain clipFactoryChain, MemoryManager memoryManager, ByteBufferToJavaFxImageConverter imageConverter,
            AudioImagePatternService audioImagePatternService, @Qualifier("longRunningTaskExecutorService") ThreadPoolExecutor executorService) {
        super(ID);
        this.clipFactoryChain = clipFactoryChain;
        this.memoryManager = memoryManager;
        this.imageConverter = imageConverter;
        this.audioImagePatternService = audioImagePatternService;
        this.executorService = executorService;
    }

    protected void openTab() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("clips-window-pane");

        FlowPane pane = new FlowPane();

        scrollPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            List<File> files = db.getFiles();
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
                        Image defaultImage = getDefaultImageFor(templateClips);
                        String label = file.getName();
                        ClipsWindowsElement element = new ClipsWindowsElement(UUID.randomUUID().toString(), templateClips, defaultImage, label);
                        elements.add(element);
                        if (getFirstClipOfType(templateClips, VisualTimelineClip.class).isPresent()) {
                            executorService.execute(withExceptionLogging(() -> fillImages(element)));
                        }
                        pane.getChildren().add(createEntry(element));
                        success = true;
                    }
                }
                if (success) {
                    event.consume();
                    db.clear();
                }
            }
        });

        scrollPane.setContent(pane);

        this.setContent(scrollPane);
        this.setText("Clips");
    }

    private Image getDefaultImageFor(List<TimelineClip> templateClips) {
        return getImageFor(templateClips, templateClips.get(0).getInterval().getEndPosition().multiply(defaultImagePositionPercent));
    }

    private Image getImageFor(List<TimelineClip> templateClips, TimelinePosition timelinePosition) {
        Optional<VisualTimelineClip> visualClip = getFirstClipOfType(templateClips, VisualTimelineClip.class);
        Optional<AudibleTimelineClip> audioClip = getFirstClipOfType(templateClips, AudibleTimelineClip.class);

        if (visualClip.isPresent()) {
            VisualMediaMetadata metadata = visualClip.get().getMediaMetadata();
            double aspectRatio = (double) metadata.getWidth() / metadata.getHeight();
            GetFrameRequest getFrameRequest = GetFrameRequest.builder()
                    .withApplyEffects(true)
                    .withExpectedWidth(ELEMENT_WIDTH)
                    .withExpectedHeight((int) (ELEMENT_WIDTH / aspectRatio))
                    .withPosition(timelinePosition)
                    .withRelativePosition(timelinePosition)
                    .withScale((double) ELEMENT_WIDTH / metadata.getWidth())
                    .withUseApproximatePosition(true)
                    .build();
            ReadOnlyClipImage frame = visualClip.get().getFrame(getFrameRequest);
            Image result = imageConverter.convertToJavafxImage(frame.getBuffer(), frame.getWidth(), frame.getHeight());
            memoryManager.returnBuffer(frame.getBuffer());
            return result;
        } else if (audioClip.isPresent()) {
            AudibleTimelineClip actualClip = audioClip.get();
            double endPosition = actualClip.getInterval().getLength().getSeconds().doubleValue();
            return audioImagePatternService.createAudioImagePattern(actualClip, ELEMENT_WIDTH, 0.0, endPosition);
        } else {
            return new WritableImage(ELEMENT_WIDTH, ELEMENT_WIDTH);
        }
    }

    private <T extends TimelineClip> Optional<T> getFirstClipOfType(List<TimelineClip> templateClips, Class<T> type) {
        return templateClips.stream().filter(clip -> type.isAssignableFrom(clip.getClass())).map(clip -> type.cast(clip)).findFirst();
    }

    private Node createEntry(ClipsWindowsElement element) {
        VBox vbox = new VBox();
        vbox.getStyleClass().add(CLIP_ENTRY);

        ImageView imageView = new ImageView(element.getDefaultImage());
        imageView.maxWidth(ELEMENT_WIDTH);
        Label text = new Label(element.getLabel());
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
            content.putString(CLIP_ENTRY);
            content.put(DataFormat.RTF, element);
            db.setContent(content);

            event.consume();
        });

        return vbox;
    }

    private void fillImages(ClipsWindowsElement element) {
        List<Image> images = new ArrayList<>(NUMBER_OF_PREVIEW_FRAMES);
        TimelineLength length = element.getTemplateClips().get(0).getInterval().getLength();
        for (int i = 0; i < NUMBER_OF_PREVIEW_FRAMES; ++i) {
            double scaler = (double) i / NUMBER_OF_PREVIEW_FRAMES;
            Image image = getImageFor(element.getTemplateClips(), length.toPosition().multiply(new BigDecimal(scaler)));
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
        isTabOpen = true;
    }

    @Override
    public void tabClosed() {
        isTabOpen = false;
    }

}

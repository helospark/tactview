package com.helospark.tactview.ui.javafx.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelineManagerFramesRequest;
import com.helospark.tactview.core.timeline.TimelineManagerRenderService;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;
import com.helospark.tactview.ui.javafx.UiTimelineManager;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Component
public class SingleFullImageViewController {
    private UiTimelineManager uiTimelineManager;
    private TimelineManagerRenderService timelineManagerRenderService;
    private ProjectRepository projectRepository;
    private ByteBufferToJavaFxImageConverter byteBufferToImageConverter;
    @Slf4j
    private Logger logger;

    public SingleFullImageViewController(UiTimelineManager uiTimelineManager, TimelineManagerRenderService timelineManagerRenderService, ProjectRepository projectRepository,
            ByteBufferToJavaFxImageConverter byteBufferToImageConverter) {
        this.uiTimelineManager = uiTimelineManager;
        this.timelineManagerRenderService = timelineManagerRenderService;
        this.projectRepository = projectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
    }

    public void renderFullScreenAtCurrentLocation() {
        TimelinePosition currentPosition = uiTimelineManager.getCurrentPosition();

        int height = projectRepository.getHeight();
        int width = projectRepository.getWidth();

        TimelineManagerFramesRequest frameRequest = TimelineManagerFramesRequest.builder()
                .withPosition(currentPosition)
                .withScale(1.0)
                .withPreviewWidth(width)
                .withPreviewHeight(height)
                .build();

        CompletableFuture.supplyAsync(() -> {
            ByteBuffer image = timelineManagerRenderService.getFrame(frameRequest).getVideoResult().getBuffer();
            return byteBufferToImageConverter.convertToJavafxImage(image, width, height);
        }).exceptionally(e -> {
            logger.error("Error rendering image", e);
            return null;
        }).thenAccept(image -> {
            Platform.runLater(() -> {
                ImageShowDialog dialog = new ImageShowDialog(image);
                dialog.show();
            });
        });

    }

    static class ImageShowDialog {
        private Stage stage;

        public ImageShowDialog(Image image) {
            ImageView imageView = new ImageView();
            imageView.setImage(image);

            ScrollPane root = new ScrollPane();
            root.setContent(imageView);

            Scene dialog = new Scene(root);
            stage = new Stage();
            stage.setTitle("FullScreenRender");
            stage.setScene(dialog);

            dialog.setOnKeyPressed(e -> {
                if (e.getCode().equals(KeyCode.ESCAPE)) {
                    stage.close();
                }
            });

            ContextMenu contextMenu = new ContextMenu();

            MenuItem item1 = new MenuItem("Save");
            item1.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save image");
                File file = fileChooser.showSaveDialog(JavaFXUiMain.STAGE);
                if (file != null) {
                    saveToFile(image, file);
                }
            });
            contextMenu.getItems().add(item1);

            imageView.setOnContextMenuRequested(e -> {
                contextMenu.show(imageView, e.getScreenX(), e.getScreenY());
            });

            stage.show();
            stage.toFront();
        }

        public void show() {
            stage.show();
        }

        private void saveToFile(Image image, File file) {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            try {
                ImageIO.write(bufferedImage, "png", new File(file.getAbsolutePath() + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

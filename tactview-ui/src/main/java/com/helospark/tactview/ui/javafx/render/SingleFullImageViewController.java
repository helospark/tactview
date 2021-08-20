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
import com.helospark.tactview.ui.javafx.stylesheet.StylesheetAdderService;
import com.helospark.tactview.ui.javafx.util.ByteBufferToJavaFxImageConverter;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Component
public class SingleFullImageViewController {
    private UiTimelineManager uiTimelineManager;
    private TimelineManagerRenderService timelineManagerRenderService;
    private ProjectRepository projectRepository;
    private ByteBufferToJavaFxImageConverter byteBufferToImageConverter;
    private StylesheetAdderService stylesheetAdderService;
    @Slf4j
    private Logger logger;

    public SingleFullImageViewController(UiTimelineManager uiTimelineManager, TimelineManagerRenderService timelineManagerRenderService, ProjectRepository projectRepository,
            ByteBufferToJavaFxImageConverter byteBufferToImageConverter, StylesheetAdderService stylesheetAdderService) {
        this.uiTimelineManager = uiTimelineManager;
        this.timelineManagerRenderService = timelineManagerRenderService;
        this.projectRepository = projectRepository;
        this.byteBufferToImageConverter = byteBufferToImageConverter;
        this.stylesheetAdderService = stylesheetAdderService;
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
            ByteBuffer image = timelineManagerRenderService.getFrame(frameRequest).getAudioVideoFragment().getVideoResult().getBuffer();
            return byteBufferToImageConverter.convertToJavafxImage(image, width, height);
        }).exceptionally(e -> {
            logger.error("Error rendering image", e);
            return null;
        }).thenAccept(image -> {
            Platform.runLater(() -> {
                ImageShowDialog dialog = new ImageShowDialog(image, stylesheetAdderService);
                dialog.show();
            });
        });

    }

    static class ImageShowDialog {
        private Stage stage;

        public ImageShowDialog(Image image, StylesheetAdderService stylesheetAdderService) {
            Canvas imageView = new Canvas(image.getWidth(), image.getHeight());
            GraphicsContext graphics = imageView.getGraphicsContext2D();

            graphics.setFill(Color.BLACK);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(image, 0, 0);

            ScrollPane root = new ScrollPane();

            root.setContent(imageView);

            Scene dialog = new Scene(root);
            stage = new Stage();
            stylesheetAdderService.styleDialog(stage, root, "stylesheet.css");
            stage.setTitle("FullScreenRender");
            stage.setScene(dialog);

            dialog.setOnKeyPressed(e -> {
                if (e.getCode().equals(KeyCode.ESCAPE)) {
                    stage.close();
                }
            });

            ContextMenu contextMenu = new ContextMenu();

            root.setContextMenu(contextMenu);

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

            imageView.setOnMouseClicked(e -> {
                if (e.getButton().equals(MouseButton.PRIMARY)) {
                    contextMenu.hide();
                }
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
                String outputFileName = file.getAbsolutePath();
                if (!outputFileName.endsWith(".png")) {
                    outputFileName = outputFileName + ".png";
                }
                ImageIO.write(bufferedImage, "png", new File(outputFileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

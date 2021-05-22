package com.helospark.tactview.ui.javafx.stylesheet;

import java.awt.Taskbar;
import java.awt.Taskbar.Feature;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Service;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.message.WatchedFileChangedMessage;
import com.helospark.tactview.core.service.FileChangedWatchService;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.ReloadStylesheetMessage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@Service
public class StylesheetAdderService {
    private static final String ICON_PATH = "/icons/tactview_icon.png";
    private FileChangedWatchService fileChangedWatchService;

    private UiMessagingService messagingService;
    @Slf4j
    private Logger logger;

    private boolean automaticallyReloadStylesheets;

    Map<Parent, List<String>> mapping = new WeakHashMap<>();

    public StylesheetAdderService(UiMessagingService messagingService, @Value("${stylesheet.autoreload}") boolean automaticallyReloadStylesheets,
            FileChangedWatchService fileChangedWatchService) {
        this.messagingService = messagingService;
        this.automaticallyReloadStylesheets = automaticallyReloadStylesheets;
        this.fileChangedWatchService = fileChangedWatchService;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ReloadStylesheetMessage.class, m -> {
            updateStylesheet();
        });
        messagingService.register(WatchedFileChangedMessage.class, message -> updateStylesheet());
    }

    public void setTactviewIconForStage(Stage stage) {
        setTactviewIconForStageStatic(stage);
    }

    public static void setTactviewIconForStageStatic(Stage stage) {
        Image image = new Image(StylesheetAdderService.class.getResource(ICON_PATH).toString());
        stage.getIcons().add(image);
        if (Taskbar.isTaskbarSupported()) {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            if (Taskbar.getTaskbar().isSupported(Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(bufferedImage);
            }
        }
    }

    private void updateStylesheet() {
        mapping.entrySet().stream()
                .forEach(e -> {
                    e.getKey().getStylesheets().clear();
                    e.getKey().getStylesheets().addAll(e.getValue());
                });
    }

    private void watchFilesIfNeeded(List<String> files) {
        if (automaticallyReloadStylesheets) {
            try {

                for (String file : files) {
                    URL resource = this.getClass().getResource("/" + file);

                    if (resource != null) {
                        String pathName = resource.getPath();
                        File resourceFile = new File(pathName);

                        fileChangedWatchService.requestFileWatch(resourceFile);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void styleDialog(Stage stage, Parent parent, String... cssFiles) {
        addStyleSheets(parent, cssFiles);

        setTactviewIconForStage(stage);
    }

    public void addStyleSheets(Parent parent, String... cssFiles) {
        parent.getStylesheets().addAll(cssFiles);
        List<String> cssFileList = Arrays.asList(cssFiles);
        mapping.put(parent, cssFileList);
        watchFilesIfNeeded(cssFileList);
    }

}

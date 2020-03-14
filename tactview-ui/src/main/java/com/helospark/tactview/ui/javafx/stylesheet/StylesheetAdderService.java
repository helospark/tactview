package com.helospark.tactview.ui.javafx.stylesheet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Service;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.UiMessagingService;
import com.helospark.tactview.ui.javafx.menu.defaultmenus.ReloadStylesheetMessage;

import javafx.application.Platform;
import javafx.scene.Parent;

@Service
public class StylesheetAdderService {
    private volatile WatchService watchService;

    private UiMessagingService messagingService;
    @Slf4j
    private Logger logger;

    private boolean automaticallyReloadStylesheets;

    Map<Parent, List<String>> mapping = new WeakHashMap<>();

    public StylesheetAdderService(UiMessagingService messagingService, @Value("${stylesheet.autoreload}") boolean automaticallyReloadStylesheets) {
        this.messagingService = messagingService;
        this.automaticallyReloadStylesheets = automaticallyReloadStylesheets;
    }

    @PostConstruct
    public void init() {
        messagingService.register(ReloadStylesheetMessage.class, m -> {
            updateStylesheet();
        });
        startThread();
    }

    private void updateStylesheet() {
        mapping.entrySet().stream()
                .forEach(e -> {
                    e.getKey().getStylesheets().clear();
                    e.getKey().getStylesheets().addAll(e.getValue());
                });
    }

    private void startThread() {
        if (automaticallyReloadStylesheets) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Thread thread = new Thread(() -> {
                    while (true) {
                        try {
                            WatchKey wk = watchService.poll(5000, TimeUnit.MILLISECONDS);
                            if (wk != null) {
                                for (WatchEvent<?> event : wk.pollEvents()) {
                                    //we only register "ENTRY_MODIFY" so the context is always a Path.
                                    final Path changed = (Path) event.context();
                                    logger.info("Reloading stylesheet, because file " + changed + " changed");
                                    Platform.runLater(() -> updateStylesheet());
                                }
                                wk.reset();
                            }
                        } catch (InterruptedException e) {
                            logger.warn("Error while polling CSS change ", e);
                        }
                    }
                }, "stylesheet-autoreloader");

                thread.start();

            } catch (IOException e) {
                logger.warn("Exception while watching files", e);
            }
        }
    }

    private void watchFilesIfNeeded(List<String> files) {
        if (automaticallyReloadStylesheets) {
            try {

                for (String file : files) {
                    URL resource = this.getClass().getResource("/" + file);

                    if (resource != null) {
                        String pathName = resource.getPath();
                        File resourceFile = new File(pathName);

                        // String resourceName = resourceFile.getName();
                        String resourceFolder = resourceFile.getParent();

                        Path path = FileSystems.getDefault().getPath(resourceFolder);

                        path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                        logger.info("Watching for " + path);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addStyleSheets(Parent parent, String... cssFiles) {
        parent.getStylesheets().addAll(cssFiles);
        List<String> cssFileList = Arrays.asList(cssFiles);
        mapping.put(parent, cssFileList);
        watchFilesIfNeeded(cssFileList);
    }

}

package com.helospark.tactview.core.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.WatchedFileChangedMessage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class FileChangeWatchServiceImpl implements FileChangedWatchService {
    private volatile WatchService watchService;
    private MessagingService messagingService;

    private Map<String, Boolean> watchedFiles = new ConcurrentHashMap<>();
    private Map<WatchKey, File> watchKeyToFileMap = new ConcurrentHashMap<WatchKey, File>();

    public FileChangeWatchServiceImpl(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Slf4j
    private Logger logger;

    @PostConstruct
    public void init() {
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
                                File changedFile = changed.toFile();
                                logger.info("Reloading stylesheet, because file " + changed + " changed");

                                Path dir = (Path) wk.watchable();
                                Path fullPath = dir.resolve(changed);

                                if (watchedFiles.containsKey(fullPath.toFile().getAbsolutePath())) {
                                    messagingService.sendAsyncMessage(new WatchedFileChangedMessage(changedFile));
                                }
                            }
                            wk.reset();
                        }
                    } catch (InterruptedException e) {
                        logger.warn("Error while polling CSS change ", e);
                    }
                }
            }, "watch-service");

            thread.start();

        } catch (IOException e) {
            logger.warn("Exception while watching files", e);
        }

    }

    @Override
    public void requestFileWatch(File file) {
        watchedFiles.put(file.getAbsolutePath(), true);
        updateWatchedFolders(file);
    }

    private void updateWatchedFolders(File file) {
        try {
            String resourceFolder = file.getParent();

            Path path = FileSystems.getDefault().getPath(resourceFolder);

            WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            watchKeyToFileMap.put(watchKey, file);
            logger.info("Watching for " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package com.helospark.tactview.core.save;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.ConditionalOnProperty;
import com.helospark.lightdi.annotation.Service;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.util.logger.Slf4j;

@Service
@ConditionalOnProperty(property = "autosave.enabled", havingValue = "true")
public class AutoSaveService {
    private SaveAndLoadHandler saveAndLoadHandler;
    private DirtyRepository dirtyRepository;

    private File autosaveFileSaveDirectory;
    private Integer interval;
    private Integer numberOfFilesToKeep;
    @Slf4j
    private Logger logger;

    private volatile boolean running = true;
    private volatile long lastDirtySave = 0;

    public AutoSaveService(SaveAndLoadHandler saveAndLoadHandler, @Value("${autosave.directory}") File temporaryFileSaveDirectory,
            @Value("${autosave.intervalSeconds}") Integer interval, @Value("${autosave.numberOfFilesToKeep}") Integer numberOfFilesToKeep, DirtyRepository dirtyRepository) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.autosaveFileSaveDirectory = temporaryFileSaveDirectory;
        this.interval = interval;
        this.dirtyRepository = dirtyRepository;
        this.numberOfFilesToKeep = numberOfFilesToKeep;
    }

    @PostConstruct
    public void init() {
        autosaveFileSaveDirectory.mkdirs();
        new Thread(() -> {
            while (running) {
                doAutosave();
                cleanupOldAutosaves();
            }
        }, "autosave-thread").start();
    }

    private void cleanupOldAutosaves() {
        List<File> sortedAutosaveFiles = Stream.of(autosaveFileSaveDirectory.listFiles())
                .filter(a -> a.getName().startsWith("autosave_"))
                .sorted((a, b) -> Long.compare(a.lastModified(), b.lastModified()))
                .collect(Collectors.toList());

        if (sortedAutosaveFiles.size() > numberOfFilesToKeep) {
            for (int i = 0; i < sortedAutosaveFiles.size() - numberOfFilesToKeep; ++i) {
                File autosavedFile = sortedAutosaveFiles.get(i);
                logger.info("Removing old autosave {}", autosavedFile.getName());
                autosavedFile.delete();
            }
        }

    }

    protected void doAutosave() {
        try {
            Thread.sleep(interval * 1000);

            long dirtyTime = dirtyRepository.getDirtyStatusChange();
            if (dirtyRepository.isDirty() && dirtyTime != lastDirtySave) {
                LocalDateTime localDateTime = LocalDateTime.now();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");

                File saveFile = new File(autosaveFileSaveDirectory, "autosave_" + formatter.format(localDateTime));

                SaveRequest saveRequest = new SaveRequest(saveFile.getAbsolutePath());

                saveAndLoadHandler.save(saveRequest);

                lastDirtySave = dirtyTime;
            }
        } catch (Exception e) {
            logger.warn("Error while performing autosaving", e);
        }
    }

    @PreDestroy
    public void destroy() {
        this.running = false;
    }

}

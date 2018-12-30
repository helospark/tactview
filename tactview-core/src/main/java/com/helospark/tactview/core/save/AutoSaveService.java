package com.helospark.tactview.core.save;

import java.io.File;
import java.time.LocalDateTime;

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
    private File temporaryFileSaveDirectory;
    private Integer interval;
    @Slf4j
    private Logger logger;

    private volatile boolean running = true;

    public AutoSaveService(SaveAndLoadHandler saveAndLoadHandler, @Value("${autosave.directory}") File temporaryFileSaveDirectory,
            @Value("${autosave.intervalSeconds}") Integer interval) {
        this.saveAndLoadHandler = saveAndLoadHandler;
        this.temporaryFileSaveDirectory = temporaryFileSaveDirectory;
        this.interval = interval;
    }

    @PostConstruct
    public void init() {
        temporaryFileSaveDirectory.mkdirs();
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(interval * 1000);

                    LocalDateTime localDateTime = LocalDateTime.now();

                    File saveFile = new File(temporaryFileSaveDirectory, "autosave_" + localDateTime.toString());

                    SaveRequest saveRequest = new SaveRequest(saveFile.getAbsolutePath());

                    saveAndLoadHandler.save(saveRequest);

                } catch (Exception e) {
                    logger.warn("Error while performing autosaving", e);
                }
            }
        }, "autosave-thread").start();
    }

    @PreDestroy
    public void destroy() {
        this.running = false;
    }

}

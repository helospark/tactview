package com.helospark.tactview.ui.javafx;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

@Component
public class UiCommandInterpreterService {
    private DirtyRepository dirtyRepository;

    @Slf4j
    private Logger logger;

    private Deque<UiCommand> commandHistory = new ConcurrentLinkedDeque<>();
    private Deque<UiCommand> redoHistory = new ConcurrentLinkedDeque<>();

    public UiCommandInterpreterService(DirtyRepository dirtyRepository) {
        this.dirtyRepository = dirtyRepository;
    }

    public <T extends UiCommand> T synchronousSend(T uiCommand) {
        logger.info("Executing {}", uiCommand);
        dirtyRepository.setDirty(true);
        redoHistory.clear();
        uiCommand.execute();
        if (uiCommand.isRevertable()) {
            commandHistory.push(uiCommand);
        }
        return uiCommand;
    }

    public <T extends UiCommand> CompletableFuture<T> sendWithResult(T uiCommand) {
        logger.debug("Adding command {}", uiCommand);
        return CompletableFuture.supplyAsync(() -> {
            return synchronousSend(uiCommand);
        }).exceptionally(e -> {
            logger.error("Unable to execute command {}", uiCommand, e);
            return null;
        });
    }

    public CompletableFuture<UiCommand> revertLast() {
        return CompletableFuture.supplyAsync(() -> {
            UiCommand previousOperation = commandHistory.poll();
            if (previousOperation != null) {
                dirtyRepository.setDirty(true);
                logger.info("Reverting " + previousOperation);
                previousOperation.revert();
                redoHistory.push(previousOperation);
            }
            return previousOperation;
        }).exceptionally(e -> {
            logger.error("Unable to revert command {}", e);
            return null;
        });
    }

    public CompletableFuture<UiCommand> redoLast() {
        return CompletableFuture.supplyAsync(() -> {
            UiCommand previousOperation = redoHistory.poll();
            if (previousOperation != null) {
                dirtyRepository.setDirty(true);
                logger.info("Redo " + previousOperation);
                previousOperation.redo();
                commandHistory.push(previousOperation);
            }
            return previousOperation;
        }).exceptionally(e -> {
            logger.error("Unable to redo command {}", e);
            return null;
        });
    }
}

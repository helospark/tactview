package com.helospark.tactview.ui.javafx;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.markers.ResettableBean;
import com.helospark.tactview.core.preference.PreferenceValue;
import com.helospark.tactview.core.save.DirtyRepository;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.ui.javafx.commands.UiCommand;
import com.helospark.tactview.ui.javafx.stylesheet.AlertDialogFactory;

@Component
public class UiCommandInterpreterService implements ResettableBean {
    private DirtyRepository dirtyRepository;
    private AlertDialogFactory alertDialogFactory;

    @Slf4j
    private Logger logger;

    private Deque<UiCommand> commandHistory = new ConcurrentLinkedDeque<>();
    private Deque<UiCommand> redoHistory = new ConcurrentLinkedDeque<>();

    private int historySize;

    public UiCommandInterpreterService(DirtyRepository dirtyRepository, @Value("${commandinterpreter.history.size}") int redoSize, AlertDialogFactory alertDialogFactory) {
        this.dirtyRepository = dirtyRepository;
        this.historySize = redoSize;
        this.alertDialogFactory = alertDialogFactory;
    }

    public <T extends UiCommand> T synchronousSend(T uiCommand) {
        logger.info("Executing {}", uiCommand);
        dirtyRepository.setDirty(true);
        redoHistory.clear();
        uiCommand.execute();
        if (uiCommand.isRevertable()) {
            commandHistory.push(uiCommand);
            while (commandHistory.size() > historySize) {
                UiCommand command = commandHistory.pollLast();
                if (command != null) {
                    command.preDestroy();
                }
            }
        }
        return uiCommand;
    }

    public <T extends UiCommand> CompletableFuture<T> sendWithResult(T uiCommand) {
        logger.debug("Adding command {}", uiCommand);
        return CompletableFuture.supplyAsync(() -> {
            return synchronousSend(uiCommand);
        }).exceptionally(e -> {
            logger.error("Unable to execute command {}", uiCommand, e);
            alertDialogFactory.showExceptionDialog("Unable to execute", e);
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
            alertDialogFactory.showExceptionDialog("Unable to revert", e);
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
            alertDialogFactory.showExceptionDialog("Unable to redo", e);
            return null;
        });
    }

    @PreferenceValue(name = "Revert history size", defaultValue = "20", group = "Clip")
    public void setPreferenceValue(int historySize) {
        this.historySize = historySize;
    }

    @Override
    public void resetDefaults() {
        commandHistory.clear();
        redoHistory.clear();
    }
}

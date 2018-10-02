package com.helospark.tactview.ui.javafx;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.commands.UiCommand;

@Component
public class UiCommandInterpreterService {
    private Deque<UiCommand> commandHistory = new ConcurrentLinkedDeque<>();
    private Deque<UiCommand> redoHistory = new ConcurrentLinkedDeque<>();

    public <T extends UiCommand> CompletableFuture<T> sendWithResult(T uiCommand) {
        return CompletableFuture.supplyAsync(() -> {
            redoHistory.clear();
            uiCommand.execute();
            if (uiCommand.isRevertable()) {
                commandHistory.push(uiCommand);
            }
            return uiCommand;
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<UiCommand> revertLast() {
        return CompletableFuture.supplyAsync(() -> {
            UiCommand previousOperation = commandHistory.poll();
            previousOperation.revert();
            redoHistory.push(previousOperation);
            return previousOperation;
        });
    }

    public CompletableFuture<UiCommand> redoLast() {
        return CompletableFuture.supplyAsync(() -> {
            UiCommand previousOperation = redoHistory.poll();
            previousOperation.redo();
            commandHistory.push(previousOperation);
            return previousOperation;
        });
    }
}

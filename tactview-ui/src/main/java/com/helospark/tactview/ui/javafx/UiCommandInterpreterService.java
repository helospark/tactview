package com.helospark.tactview.ui.javafx;

import java.util.concurrent.CompletableFuture;

import com.helospark.tactview.api.UiCommand;

public interface UiCommandInterpreterService {

    public void send(UiCommand uiCommand);

    public <T> CompletableFuture<T> sendAndGetCommand(UiCommand uiCommand, Class<T> expectedResult);
}

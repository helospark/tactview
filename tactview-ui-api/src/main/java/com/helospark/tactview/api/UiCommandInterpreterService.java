package com.helospark.tactview.api;

import java.util.concurrent.CompletableFuture;

public interface UiCommandInterpreterService {

    public void send(UiCommand uiCommand);

    public <T> CompletableFuture<T> sendAndGetCommand(UiCommand uiCommand, Class<T> expectedResult);
}

package com.helospark.tactview.core.uibridge;

import java.util.concurrent.CompletableFuture;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.api.UiCommand;
import com.helospark.tactview.api.UiCommandInterpreterService;
import com.helospark.tactview.api.commands.ClipAddedResponse;

@Component
public class UiCommandInterpreterServiceImpl implements UiCommandInterpreterService {

    @Override
    public void send(UiCommand uiCommand) {
        System.out.println("Recieved " + uiCommand);
    }

    @Override
    public <T> CompletableFuture<T> sendAndGetCommand(UiCommand uiCommand, Class<T> expectedResult) {
        System.out.println("Recieved get " + uiCommand);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return (T) new ClipAddedResponse(true, "Becuase it's ok");
        });
    }

}

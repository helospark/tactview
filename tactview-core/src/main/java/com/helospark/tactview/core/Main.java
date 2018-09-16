package com.helospark.tactview.core;

import java.util.List;

import javax.annotation.PostConstruct;

import com.helospark.lightdi.LightDi;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.LightDiContextConfiguration;
import com.helospark.lightdi.annotation.Autowired;
import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Eager;
import com.helospark.tactview.api.UiCommandInterpreterService;
import com.helospark.tactview.core.util.jpaplugin.JnaLightDiPlugin;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;

@Configuration
@ComponentScan
@Eager
public class Main {
    private static LightDiContext lightDi;

    @Autowired
    private StandardLib standardLib;

    @PostConstruct
    public void post() {
        standardLib.printf("%s %d", "hello", 1);
    }

    public static void main(String[] args) {
        LightDiContextConfiguration configuration = LightDiContextConfiguration.builder()
                .withThreadNumber(4)
                .withCheckForIntegrity(true)
                .withAdditionalDependencies(List.of(new JnaLightDiPlugin()))
                .build();
        lightDi = LightDi.initContextByClass(Main.class, configuration);
        UiCommandInterpreterService commandInterpreter = lightDi.getBean(UiCommandInterpreterService.class);
        launchUi(commandInterpreter);
    }

    private static void launchUi(UiCommandInterpreterService commandInterpreter) {
        // Now I really don't like JavaFX
        JavaFXUiMain ui = new JavaFXUiMain();
        JavaFXUiMain.setCommandInterpreter(commandInterpreter);
        ui.launchUi();
    }

}

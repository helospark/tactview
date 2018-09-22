package com.helospark.tactview.core;

import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.lightdi.annotation.Eager;

@Configuration
@ComponentScan
@Eager
public class Main {
    //    private static LightDiContext lightDi;

    //    @Autowired
    //    private StandardLib standardLib;

    //    private static void launchUi(UiCommandInterpreterService commandInterpreter, MediaDataResponse response) {
    //        // Now I really don't like JavaFX
    //        JavaFXUiMain ui = new JavaFXUiMain();
    //        JavaFXUiMain.setCommandInterpreter(commandInterpreter);
    //        JavaFXUiMain.setImage(response.getVideoFrames().get(0));
    //        ui.launchUi();
    //    }

}

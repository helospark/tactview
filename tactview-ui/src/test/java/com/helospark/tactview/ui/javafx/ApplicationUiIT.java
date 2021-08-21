package com.helospark.tactview.ui.javafx;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Init;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class ApplicationUiIT {
    private JavaFXUiMain javaxUiMain;

    // https://nofluffjuststuff.com/blog/andres_almiray/2016/02/running_testfx_tests_in_headless_mode
    @BeforeAll
    public static void beforeClass() {
        if (Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }

    @Init
    private void init() throws Exception {
        javaxUiMain = new JavaFXUiMain();
        javaxUiMain.init();
    }

    @Start
    private void start(Stage stage) throws Exception {
        javaxUiMain.start(stage);
    }

    @Test
    void should_contain_button_with_text(FxRobot robot) {
        Assertions.assertThat(robot.lookup("#timeline-view")).isNotNull();
    }
}

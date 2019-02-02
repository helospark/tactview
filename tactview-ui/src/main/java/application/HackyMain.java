package application;

import com.helospark.tactview.ui.javafx.JavaFXUiMain;

// https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
public class HackyMain {

    public static void main(String[] args) {
        JavaFXUiMain.main(args);
    }
}

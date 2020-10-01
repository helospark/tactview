package application;

import java.util.ServiceLoader;

import com.helospark.tactview.core.init.ApplicationInitializedMainThreadCallback;
import com.helospark.tactview.ui.javafx.JavaFXUiMain;

// https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
public class HackyMain {

    public static void main(String[] args) {
        try {
            ServiceLoader<ApplicationInitializedMainThreadCallback> initializationCallbacks = ServiceLoader.load(ApplicationInitializedMainThreadCallback.class);
            for (var callback : initializationCallbacks) {
                callback.call(args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JavaFXUiMain.main(args);
    }
}

package com.helospark.tactview.core.init;

/**
 * SPI interface for main callback, right after Java main() is called.
 * These will be called from the Java main thread before DI system is initialized.
 * Unless your requirement is to do pre-DI initialization on main thread, avoid using this and use proper DI solutions (ex. PostConstruct, Component, etc.),
 * because you cannot rely on DI features, using this is single threaded and before the Splash screen is rendered.
 *   
 * @author helospark
 */
public interface ApplicationInitializedMainThreadCallback {

    public void call(String[] args);

}

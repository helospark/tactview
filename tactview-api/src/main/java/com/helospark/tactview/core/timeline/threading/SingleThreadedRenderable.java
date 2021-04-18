package com.helospark.tactview.core.timeline.threading;

/**
 * If implemented by an effect or a clip, it is guaranteed to be rendered in single thread continously, 
 * however during preview no ordered is guaranteed.
 * @author helospark
 */
public interface SingleThreadedRenderable {

    public default void onStartRender() {

    }

    public default void onEndRender() {

    }

    public default boolean isSequentialRenderEnabled() {
        return true;
    }

}

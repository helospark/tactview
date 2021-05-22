package com.helospark.tactview.core.util;

import java.util.Map;

public class PixelUpdateRequest {
    public int x, y;
    private Map<ThreadLocalProvider<?>, Object> threadLocals;

    public PixelUpdateRequest(int x, int y, Map<ThreadLocalProvider<?>, Object> threadLocals) {
        this.x = x;
        this.y = y;
        this.threadLocals = threadLocals;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public <T> T getThreadLocal(ThreadLocalProvider<T> clazz) {
        return (T) threadLocals.get(clazz);
    }

}

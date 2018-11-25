package com.helospark.tactview.core.util.brush;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.ClipFrameResult;
import com.helospark.tactview.core.util.cacheable.CacheCleanable;

public class GetBrushResponse implements CacheCleanable {
    private ClipFrameResult brush;

    public GetBrushResponse(ClipFrameResult brush) {
        this.brush = brush;
    }

    public ClipFrameResult getBrush() {
        return brush;
    }

    @Override
    public void clean() {
        // hopefully noone cleans this while someone else uses it
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(brush.getBuffer());
    }

}

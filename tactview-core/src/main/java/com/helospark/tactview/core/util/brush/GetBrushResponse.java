package com.helospark.tactview.core.util.brush;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.cacheable.CacheCleanable;

public class GetBrushResponse implements CacheCleanable {
    private ReadOnlyClipImage brush;

    public GetBrushResponse(ReadOnlyClipImage brush) {
        this.brush = brush;
    }

    public ReadOnlyClipImage getBrush() {
        return brush;
    }

    @Override
    public void clean() {
        // hopefully noone cleans this while someone else uses it
        GlobalMemoryManagerAccessor.memoryManager.returnBuffer(brush.getBuffer());
    }

}

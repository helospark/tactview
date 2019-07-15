package com.helospark.tactview.core.util.brush;

import java.io.InputStream;

import com.helospark.tactview.core.util.cacheable.Cacheable;

public interface BrushLoader {

    GimpBrush loadBrush(InputStream inputStream);

}
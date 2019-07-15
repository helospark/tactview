package com.helospark.tactview.core.util.brush;

import java.io.InputStream;

public interface BrushLoader {

    GimpBrush loadBrush(InputStream inputStream);

}
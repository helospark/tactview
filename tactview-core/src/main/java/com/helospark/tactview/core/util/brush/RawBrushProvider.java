package com.helospark.tactview.core.util.brush;

import java.io.InputStream;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.FilePathToInputStream;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class RawBrushProvider {
    private GimpBrushLoader brushLoader;
    private FilePathToInputStream filePathToInputStream;

    public RawBrushProvider(GimpBrushLoader brushLoader, FilePathToInputStream filePathToInputStream) {
        this.brushLoader = brushLoader;
        this.filePathToInputStream = filePathToInputStream;
    }

    @Cacheable(cacheTimeInMilliseconds = 60000, size = 100)
    public GimpBrush getBrush(String filename) {
        InputStream inputStream = filePathToInputStream.fileNameToStream(filename);
        return brushLoader.loadBrush(inputStream);
    }

}

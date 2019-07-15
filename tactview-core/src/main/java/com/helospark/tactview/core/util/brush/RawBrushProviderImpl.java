package com.helospark.tactview.core.util.brush;

import java.io.InputStream;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.FilePathToInputStream;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Component
public class RawBrushProviderImpl {
    private BrushLoader brushLoader;
    private FilePathToInputStream filePathToInputStream;

    public RawBrushProviderImpl(BrushLoader brushLoader, FilePathToInputStream filePathToInputStream) {
        this.brushLoader = brushLoader;
        this.filePathToInputStream = filePathToInputStream;
    }

    @Cacheable(cacheTimeInMilliseconds = 60000, size = 100)
    public GimpBrush getBrush(String filename) {
        InputStream inputStream = filePathToInputStream.fileNameToStream(filename);
        return brushLoader.loadBrush(inputStream);
    }

}

package com.helospark.tactview.core.util.lut;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.lut.cube.CubeLutLoader;
import com.helospark.tactview.core.util.lut.lutre3d.Lutre3dLoader;

@Component
public class LutLoaderImpl implements LutLoader {
    private Lutre3dLoader lutreLoader;
    private CubeLutLoader cubeLutLoader;

    public LutLoaderImpl(Lutre3dLoader lutreLoader, CubeLutLoader cubeLutLoader) {
        this.lutreLoader = lutreLoader;
        this.cubeLutLoader = cubeLutLoader;
    }

    @Override
    public AbstractLut load(String filename) {
        if (filename.toLowerCase().endsWith(".cube")) {
            return cubeLutLoader.readLut(filename);
        } else if (filename.toLowerCase().endsWith(".3dl")) {
            return lutreLoader.readLut(filename);
        } else {
            throw new IllegalArgumentException("Unsupported lut format " + filename);
        }
    }
}

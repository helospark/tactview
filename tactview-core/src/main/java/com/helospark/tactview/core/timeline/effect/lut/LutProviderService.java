package com.helospark.tactview.core.timeline.effect.lut;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;
import com.helospark.tactview.core.util.lut.AbstractLut;
import com.helospark.tactview.core.util.lut.cube.CubeLutLoader;

@Component
public class LutProviderService {
    private CubeLutLoader cubeLutLoader;

    public LutProviderService(CubeLutLoader cubeLutLoader) {
        this.cubeLutLoader = cubeLutLoader;
    }

    @Cacheable(size = 100, cacheTimeInMilliseconds = 10000000)
    public AbstractLut provideLutFromFile(String filename) {
        return cubeLutLoader.readLut(filename);
    }

}

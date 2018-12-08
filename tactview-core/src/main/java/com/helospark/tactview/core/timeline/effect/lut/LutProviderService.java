package com.helospark.tactview.core.timeline.effect.lut;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.util.cacheable.Cacheable;
import com.helospark.tactview.core.util.lut.AbstractLut;
import com.helospark.tactview.core.util.lut.LutLoader;

@Component
public class LutProviderService {
    private LutLoader lutLoader;

    public LutProviderService(LutLoader lutLoader) {
        this.lutLoader = lutLoader;
    }

    @Cacheable(size = 100, cacheTimeInMilliseconds = 10000000)
    public AbstractLut provideLutFromFile(String filename) {
        return lutLoader.load(filename);
    }

}

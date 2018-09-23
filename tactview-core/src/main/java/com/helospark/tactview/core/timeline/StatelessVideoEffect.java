package com.helospark.tactview.core.timeline;

import java.nio.ByteBuffer;

public interface StatelessVideoEffect extends IntervalAware, StatelessEffect {

    public void fillFrame(ByteBuffer result, ByteBuffer currentFrame);

}

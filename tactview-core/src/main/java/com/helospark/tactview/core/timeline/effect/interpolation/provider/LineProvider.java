package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.InterpolationLine;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.DesSerFactory;

public class LineProvider extends CompositeKeyframeableEffect {
    PointProvider startPointProvider;
    PointProvider endPointProvider;

    public LineProvider(PointProvider startPointProvider, PointProvider endPointProvider) {
        super(List.of(startPointProvider, endPointProvider));
        this.startPointProvider = startPointProvider;
        this.endPointProvider = endPointProvider;
    }

    @Override
    public InterpolationLine getValueAt(TimelinePosition position) {
        Point x = startPointProvider.getValueAt(position);
        Point y = endPointProvider.getValueAt(position);
        return new InterpolationLine(x, y);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect> getChildren() {
        return Arrays.asList(startPointProvider, endPointProvider);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return startPointProvider.getSizeFunction();
    }

    @Override
    public KeyframeableEffect deepClone() {
        return new LineProvider(startPointProvider.deepClone(), endPointProvider.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return LineProviderFactory.class;
    }

}

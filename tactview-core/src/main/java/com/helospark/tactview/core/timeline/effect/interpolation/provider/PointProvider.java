package com.helospark.tactview.core.timeline.effect.interpolation.provider;

import java.util.Arrays;
import java.util.List;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.util.DesSerFactory;

public class PointProvider extends CompositeKeyframeableEffect {
    DoubleProvider xProvider;
    DoubleProvider yProvider;

    public PointProvider(DoubleProvider xProvider, DoubleProvider yProvider) {
        super(List.of(xProvider, yProvider));
        this.xProvider = xProvider;
        this.yProvider = yProvider;
    }

    @Override
    public Point getValueAt(TimelinePosition position) {
        double x = xProvider.getValueAt(position);
        double y = yProvider.getValueAt(position);
        return new Point(x, y);
    }

    public DoubleProvider getxProvider() {
        return xProvider;
    }

    public DoubleProvider getyProvider() {
        return yProvider;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<KeyframeableEffect> getChildren() {
        return Arrays.asList(xProvider, yProvider);
    }

    @Override
    public SizeFunction getSizeFunction() {
        return xProvider.getSizeFunction();
    }

    @Override
    public PointProvider deepClone() {
        return new PointProvider(xProvider.deepClone(), yProvider.deepClone());
    }

    @Override
    public Class<? extends DesSerFactory<? extends KeyframeableEffect>> generateSerializableContent() {
        return PointProviderFactory.class;
        //        return new PointProvider((DoubleProvider)xProvider, (DoubleProvider) xProvider);
    }

}

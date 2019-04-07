package com.helospark.tactview.ui.javafx.control;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KeyFrameInfo;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.CurveProvider.KnotAwareUnivariateFunction;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class CurveWidget extends Control {
    private ObjectProperty<KnotAwareUnivariateFunction> curveProperty;
    private ObjectProperty<KeyFrameInfo> onActionProvider;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public CurveWidget(KnotAwareUnivariateFunction curve, double minX, double maxX, double minY, double maxY) {
        this.curveProperty = new SimpleObjectProperty<>(curve);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CurveWidgetSkin(this);
    }

    public ObjectProperty<KeyFrameInfo> onActionProperty() {
        if (onActionProvider == null) {
            onActionProvider = new SimpleObjectProperty<>(this, "action", null);
        }
        return onActionProvider;
    }

    public ObjectProperty<KnotAwareUnivariateFunction> getCurveProperty() {
        return curveProperty;
    }

    public void setValue(KnotAwareUnivariateFunction curve) {
        curveProperty.set(curve);
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

}

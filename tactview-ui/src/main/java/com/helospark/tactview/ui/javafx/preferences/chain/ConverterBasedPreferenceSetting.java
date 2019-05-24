package com.helospark.tactview.ui.javafx.preferences.chain;

import java.util.function.Supplier;

import com.dlsc.preferencesfx.model.Setting;

public class ConverterBasedPreferenceSetting implements CustomPreferenceSetting {
    private Setting setting;
    private Supplier<String> currentValue;

    public ConverterBasedPreferenceSetting(Setting setting, Supplier<String> currentValue) {
        this.setting = setting;
        this.currentValue = currentValue;
    }

    @Override
    public String getCurrentValueAsString() {
        return currentValue.get();
    }

    @Override
    public Setting getSetting() {
        return setting;
    }

}

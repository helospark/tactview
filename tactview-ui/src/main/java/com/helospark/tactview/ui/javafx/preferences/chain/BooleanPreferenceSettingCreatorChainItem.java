package com.helospark.tactview.ui.javafx.preferences.chain;

import com.dlsc.preferencesfx.model.Setting;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.preferences.TreeOptions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@Component
public class BooleanPreferenceSettingCreatorChainItem implements PreferenceSettingCreatorChainItem {

    @Override
    public boolean supports(TreeOptions option) {
        return option.data.type.equals(Boolean.class) || option.data.type.equals(Boolean.TYPE);
    }

    @Override
    public ConverterBasedPreferenceSetting createSetting(TreeOptions option, String currentValue) {
        Boolean currentValueAsBoolean = Boolean.valueOf(currentValue);
        BooleanProperty booleanProperty = new SimpleBooleanProperty(currentValueAsBoolean);
        booleanProperty.addListener((e, oldValue, newValue) -> {
            try {
                updateValue(option, newValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                booleanProperty.set(oldValue);
            }
        });
        updateValue(option, currentValueAsBoolean);
        return new ConverterBasedPreferenceSetting(Setting.of(option.name, booleanProperty), () -> String.valueOf(booleanProperty.get()));
    }

    private void updateValue(TreeOptions option, boolean newValue) {
        try {
            option.data.method.invoke(option.data.bean, newValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package com.helospark.tactview.ui.javafx.preferences.chain;

import com.dlsc.preferencesfx.model.Setting;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.preferences.TreeOptions;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Component
public class StringPreferenceSettingCreatorChainItem implements PreferenceSettingCreatorChainItem {

    @Override
    public boolean supports(TreeOptions option) {
        return option.data.type.equals(String.class);
    }

    @Override
    public ConverterBasedPreferenceSetting createSetting(TreeOptions option, String currentValue) {
        StringProperty stringProperty = new SimpleStringProperty(currentValue);
        stringProperty.addListener((e, oldValue, newValue) -> {
            try {
                updateValue(option, newValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                stringProperty.set(oldValue);
            }
        });
        updateValue(option, currentValue);
        return new ConverterBasedPreferenceSetting(Setting.of(option.name, stringProperty), () -> stringProperty.get());
    }

    private Object updateValue(TreeOptions option, String newValue) {
        try {
            return option.data.method.invoke(option.data.bean, newValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

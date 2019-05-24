package com.helospark.tactview.ui.javafx.preferences.chain;

import com.dlsc.preferencesfx.model.Setting;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.ui.javafx.preferences.TreeOptions;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

@Component
public class IntegerPreferenceSettingCreatorChainItem implements PreferenceSettingCreatorChainItem {

    @Override
    public boolean supports(TreeOptions option) {
        return option.data.type.equals(Integer.class) || option.data.type.equals(Integer.TYPE);
    }

    @Override
    public ConverterBasedPreferenceSetting createSetting(TreeOptions option, String currentValue) {
        int currentValueAsInt = Integer.parseInt(currentValue);
        IntegerProperty integerProperty = new SimpleIntegerProperty(currentValueAsInt);
        integerProperty.addListener((e, oldValue, newValue) -> {
            try {
                updateValue(option, newValue.intValue());
            } catch (Exception ex) {
                ex.printStackTrace();
                integerProperty.set(oldValue.intValue());
            }
        });
        updateValue(option, currentValueAsInt);
        return new ConverterBasedPreferenceSetting(Setting.of(option.name, integerProperty), () -> String.valueOf(integerProperty.get()));
    }

    private Object updateValue(TreeOptions option, int newValue) {
        try {
            return option.data.method.invoke(option.data.bean, newValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

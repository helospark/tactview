package com.helospark.tactview.ui.javafx.preferences.chain;

import com.helospark.tactview.ui.javafx.preferences.TreeOptions;

public interface PreferenceSettingCreatorChainItem {

    public boolean supports(TreeOptions option);

    public CustomPreferenceSetting createSetting(TreeOptions option, String currentValue);

}

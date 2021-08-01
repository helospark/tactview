package com.helospark.tactview.ui.javafx.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.lightdi.annotation.Value;
import com.helospark.tactview.core.preference.PreferenceValueBeanPostProcessor;
import com.helospark.tactview.core.preference.PreferenceValueData;
import com.helospark.tactview.ui.javafx.preferences.chain.CustomPreferenceSetting;
import com.helospark.tactview.ui.javafx.preferences.chain.PreferenceSettingCreatorChainItem;
import com.helospark.tactview.ui.javafx.scenepostprocessor.ScenePostProcessor;

import javafx.scene.Scene;

@Component
public class PreferencesPage implements ScenePostProcessor {
    TypeReference<HashMap<String, String>> typeRef = new TypeReference<>() {
    };

    private PreferenceValueBeanPostProcessor preferenceValueBeanPostProcessor;
    private List<PreferenceSettingCreatorChainItem> preferenceChain;
    private Map<String, CustomPreferenceSetting> allSettings = new HashMap<>();
    private ObjectMapper objectMapper;

    private String preferencesFile;

    private PreferencesFx preferencesFx;

    public PreferencesPage(PreferenceValueBeanPostProcessor preferenceValueBeanPostProcessor, List<PreferenceSettingCreatorChainItem> preferenceChain,
            @Value("${tactview.homedirectory}/preferences.json") String preferencesFile, @Qualifier("prettyPrintingObjectMapper") ObjectMapper objectMapper) {
        this.preferenceValueBeanPostProcessor = preferenceValueBeanPostProcessor;
        this.preferenceChain = preferenceChain;
        this.preferencesFile = preferencesFile;
        this.objectMapper = objectMapper;
    }

    @Override
    public void postProcess(Scene scene) {
        Map<String, PreferenceValueData> values = preferenceValueBeanPostProcessor.getPreferenceValues();

        Map<String, String> currentPreferences = readCurrentPreferences();
        GroupTreeElement treeRoot = createTree(values);

        List<Category> categories = new ArrayList<>();
        for (var cat : treeRoot.children.values()) {
            categories.add(createCategory(cat, currentPreferences));
        }

        preferencesFx = PreferencesFx.of(PreferencesPage.class, categories.toArray(new Category[0]));
        preferencesFx.saveSettings(true);
    }

    private Map<String, String> readCurrentPreferences() {
        File file = new File(preferencesFile);
        if (file.exists()) {
            try (FileInputStream fos = new FileInputStream(file)) {
                return objectMapper.readValue(fos, typeRef);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Map.of();
    }

    private Category createCategory(GroupTreeElement treeRoot, Map<String, String> currentPreferences) {
        List<Category> subcategories = new ArrayList<>();

        for (var entry : treeRoot.children.entrySet()) {
            subcategories.add(createCategory(entry.getValue(), currentPreferences));
        }

        List<Setting> settings = new ArrayList<>();
        for (var option : treeRoot.options.values()) {
            String currentValue;

            if (currentPreferences.containsKey(option.data.name)) {
                currentValue = currentPreferences.get(option.data.name);
            } else {
                currentValue = option.data.defaultValue;
            }

            CustomPreferenceSetting customSetting = preferenceChain.stream()
                    .filter(a -> a.supports(option))
                    .map(a -> a.createSetting(option, currentValue))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unsupported type " + option.data.type));

            settings.add(customSetting.getSetting());
            allSettings.put(option.data.name, customSetting);
        }
        Category category = Category.of(treeRoot.groupName, settings.toArray(new Setting[0]));
        if (subcategories.size() > 0) {
            category.subCategories(subcategories.toArray(new Category[0]));
        }

        return category;
    }

    static class GroupTreeElement {
        String groupName;
        Map<String, TreeOptions> options;
        Map<String, GroupTreeElement> children;

        public GroupTreeElement(String groupName) {
            this.groupName = groupName;
            options = new LinkedHashMap<>();
            children = new LinkedHashMap<>();
        }

    }

    private GroupTreeElement createTree(Map<String, PreferenceValueData> values) {
        GroupTreeElement treeRoot = new GroupTreeElement("");

        for (var element : values.entrySet()) {
            GroupTreeElement currentTreeElement = treeRoot;
            String[] parts = element.getKey().split("\\.");
            for (int i = 0; i < parts.length - 1; ++i) {
                String groupName = parts[i];
                GroupTreeElement newTreeElement = currentTreeElement.children.get(groupName);
                if (newTreeElement == null) {
                    newTreeElement = new GroupTreeElement(groupName);
                    currentTreeElement.children.put(groupName, newTreeElement);
                } else {
                    currentTreeElement.children.put(groupName, newTreeElement);
                }
                currentTreeElement = newTreeElement;
            }
            String option = parts[parts.length - 1];

            TreeOptions leafElement = new TreeOptions(option, element.getValue());
            currentTreeElement.options.put(option, leafElement);
        }

        return treeRoot;
    }

    public void open() {
        preferencesFx.show(true);
        writeCurrentPreferences();
    }

    private void writeCurrentPreferences() {
        try {
            File file = new File(preferencesFile);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                Map<String, String> dataToWrite = new HashMap<>();
                for (var entry : allSettings.entrySet()) {
                    dataToWrite.put(entry.getKey(), entry.getValue().getCurrentValueAsString());
                }

                objectMapper.writeValue(fos, dataToWrite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

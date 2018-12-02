package com.helospark.tactview.ui.javafx.repository;

import java.util.HashMap;
import java.util.Map;

import com.helospark.lightdi.annotation.Component;

@Component
public class NameToIdRepository {
    private Map<String, String> nameToId = new HashMap<>();
    private Map<String, String> idToName = new HashMap<>();

    public String generateAndAddNameForIdIfNotPresent(String baseName, String id) {
        if (!hasNameForId(id)) {
            int i;
            for (i = 1; i < 1000; ++i) {
                String result = nameToId.get(baseName + i);
                if (result == null) {
                    break;
                }
            }
            String generatedName = baseName + i;
            nameToId.put(generatedName, id);
            idToName.put(id, generatedName);
            return generatedName;
        } else {
            return idToName.get(id);
        }
    }

    public boolean hasNameForId(String id) {
        return idToName.get(id) != null;
    }

    public void addNameForId(String name, String id) {
        nameToId.put(name, id);
        idToName.put(id, name);
    }

    public void removeId(String id) {
        String name = idToName.remove(id);
        if (name != null) {
            nameToId.remove(name);
        }
    }

    public void removeName(String name) {
        String id = nameToId.remove(name);
        if (id != null) {
            idToName.remove(id);
        }
    }

    public String getNameForId(String id) {
        return idToName.get(id);
    }

    public String getIdForName(String name) {
        return nameToId.get(name);
    }

    public boolean containsName(String text) {
        return nameToId.get(text) != null;
    }
}

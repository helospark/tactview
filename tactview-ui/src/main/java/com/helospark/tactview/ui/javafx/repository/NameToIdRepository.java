package com.helospark.tactview.ui.javafx.repository;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Order;
import com.helospark.tactview.core.api.SaveLoadContributor;
import com.helospark.tactview.core.util.StaticObjectMapper;

import javafx.application.Platform;

@Component
@Order(value = 1)
public class NameToIdRepository implements SaveLoadContributor {
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
        String originalName = idToName.get(id);
        nameToId.remove(originalName);
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

    @Override
    public void generateSavedContent(Map<String, Object> generatedContent) {
        generatedContent.put("nameToIdMap", nameToId);
    }

    @Override
    public void loadFrom(JsonNode tree) {
        ObjectMapper mapper = StaticObjectMapper.objectMapper;

        try {
            JsonNode mapNode = tree.get("nameToIdMap");

            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
            };

            JavaType jt = mapper.getTypeFactory().constructType(typeRef);
            Map<String, String> newNameToId = new HashMap<>(mapper.readValue(mapper.treeAsTokens(mapNode), jt));
            Map<String, String> newIdToName = new HashMap<>();
            for (var entry : newNameToId.entrySet()) {
                newIdToName.put(entry.getValue(), entry.getKey());
            }
            Platform.runLater(() -> {
                this.nameToId = newNameToId;
                this.idToName = newIdToName;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

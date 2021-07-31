package com.helospark.tactview.ui.javafx.hotkey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Value;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;

@Component
public class HotKeyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotKeyRepository.class);
    private Map<String, KeyDescriptor> descriptors = new LinkedHashMap<>();

    private String hotkeyFileName;
    private ObjectMapper objectMapper;

    public HotKeyRepository(@Value("${tactview.hotkeyFileName}") String hotKeyFileName) {
        this.hotkeyFileName = hotKeyFileName;

        objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(KeyCodeCombination.class, new KeyCodeCombinationDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            File file = new File(hotkeyFileName);
            if (file.exists()) {
                Map<String, KeyDescriptor> result = objectMapper.readValue(file, new TypeReference<Map<String, KeyDescriptor>>() {
                });
                if (result != null) {
                    descriptors = result;
                    LOGGER.info("Read hotkeys {}", descriptors);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to load hotkey file {}", hotkeyFileName, e);
        }
    }

    public void saveHotkeys() {
        try {
            File file = new File(hotkeyFileName);
            objectMapper.writeValue(file, descriptors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public KeyDescriptor registerOrGetHotKey(String id, KeyCodeCombination keyCodeCombination, String name) {
        KeyDescriptor result = descriptors.get(id);
        if (result == null) {
            KeyDescriptor keyDescriptorToAdd = new KeyDescriptor(name, keyCodeCombination);
            descriptors.put(id, keyDescriptorToAdd);
            result = keyDescriptorToAdd;
        }
        return result;
    }

    public KeyDescriptor registerHotKey(String id, KeyDescriptor keyDescriptor) {
        KeyDescriptor result = descriptors.get(id);
        if (result == null) {
            descriptors.put(id, keyDescriptor);
            result = keyDescriptor;
        }
        return result;
    }

    public KeyDescriptor getHotKeyById(String id) {
        KeyDescriptor keyDescriptor = descriptors.get(id);
        if (keyDescriptor == null) {
            throw new RuntimeException("No hotkey with id " + id);
        }
        return keyDescriptor;
    }

    public Map<String, KeyDescriptor> getKeyDescriptors() {
        return new LinkedHashMap<>(descriptors);
    }

    public void changeHotKeyForId(String id, KeyCodeCombination combination) {
        KeyDescriptor keyDescriptor = descriptors.get(id);
        descriptors.put(id, keyDescriptor.butWithCombination(combination));
    }

    private class KeyCodeCombinationDeserializer extends JsonDeserializer<KeyCodeCombination> {
        @Override
        public KeyCodeCombination deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Map<String, Modifier> nameToModifier = Map.of(
                    "shift", KeyCombination.SHIFT_DOWN,
                    "control", KeyCombination.CONTROL_DOWN,
                    "alt", KeyCombination.ALT_DOWN,
                    "shortcut", KeyCombination.SHORTCUT_DOWN);

            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);

            List<Modifier> modifiers = new ArrayList<>();

            for (var element : nameToModifier.entrySet()) {
                if (node.get((element.getKey().toString())).asText().equals("DOWN")) {
                    modifiers.add(element.getValue());
                }
            }
            KeyCode code = KeyCode.valueOf(node.get("code").asText());

            return new KeyCodeCombination(code, modifiers.toArray(new Modifier[0]));
        }
    }

}

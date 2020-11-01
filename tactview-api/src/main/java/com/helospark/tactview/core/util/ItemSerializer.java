package com.helospark.tactview.core.util;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.helospark.tactview.core.save.SaveMetadata;

public class ItemSerializer extends StdSerializer<SavedContentAddable> {
    private SaveMetadata saveMetadata;

    public ItemSerializer(SaveMetadata saveMetadata) {
        this((Class<SavedContentAddable>) null);
        this.saveMetadata = saveMetadata;
    }

    public ItemSerializer(Class<SavedContentAddable> t) {
        super(t);
    }

    @Override
    public void serialize(SavedContentAddable value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        try {
            Class<? extends DesSerFactory<Object>> factoryClass = value.generateSerializableContent();
            DesSerFactory<Object> factory;
            factory = factoryClass.newInstance();

            Map<String, Object> innerMap = new LinkedHashMap<>();
            innerMap.put("deserializer", factoryClass.getName());
            factory.serializeInto(value, innerMap, saveMetadata);

            gen.writeObject(innerMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package com.helospark.tactview.ui.javafx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.helospark.lightdi.annotation.Service;

@Service
public class UiSettingsRepository {
    static final Map<Class<?>, Function<String, ?>> converters;
    private HashMap<String, String> settings;

    static {
        converters = new HashMap<>();
        converters.put(String.class, input -> input);
        converters.put(Integer.class, input -> Integer.valueOf(input));
        converters.put(Double.class, input -> Double.valueOf(input));
    }

    public <T> T getAs(String value, Class<T> type) {
        String result = settings.get(value);
        if (result == null) {
            throw new IllegalArgumentException("No setting with that name");
        }
        Function<String, ?> converterFunction = converters.get(type);
        if (converterFunction == null) {
            throw new IllegalArgumentException("No converter for that type");
        }
        return type.cast(converterFunction.apply(result));
    }

}

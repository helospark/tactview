package com.helospark.tactview.core.timeline.effect.interpolation;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.save.SaveMetadata;
import com.helospark.tactview.core.util.DesSerFactory;
import com.helospark.tactview.core.util.SavedContentAddable;

public abstract class AbstractKeyframeableEffectDesSerFactory<T extends KeyframeableEffect> implements DesSerFactory<T> {

    @Override
    public void serializeInto(T instance, Map<String, Object> data, SaveMetadata saveMetadata) {
        data.put("id", instance.getId());
        if (instance.getExpression() != null) {
            data.put("expression", instance.getExpression());
        }
        addDataForDeserializeInternal(instance, data);
    }

    @Override
    public T deserialize(JsonNode data, SavedContentAddable<?> currentFieldValue, LoadMetadata loadMetadata) {
        T result = deserializeInternal(data, (T) currentFieldValue, loadMetadata);
        KeyframeableEffect current = (KeyframeableEffect) currentFieldValue;
        result.id = data.get("id").asText();
        JsonNode expressionNode = data.get("expression");
        if (expressionNode != null && !expressionNode.isNull()) {
            result.expression = expressionNode.asText();
        }
        //        boolean useKeyframes = data.get("useKeyframes").asBoolean(false);
        //        result.setUseKeyframes(useKeyframes);
        return result;
    }

    protected abstract void addDataForDeserializeInternal(T instance, Map<String, Object> data);

    protected abstract T deserializeInternal(JsonNode data, T currentFieldValue, LoadMetadata loadMetadata);
}

package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator;

import java.util.Map;
import java.util.Objects;

import com.helospark.tactview.core.timeline.effect.interpolation.KeyframeableEffect;

public class EvaluationContextProviderData {
    public Map<String, KeyframeableEffect<?>> data;
    public Map<String, Object> globals;

    public EvaluationContextProviderData(Map<String, KeyframeableEffect<?>> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EvaluationContextProviderData [data=" + data + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvaluationContextProviderData other = (EvaluationContextProviderData) obj;
        return Objects.equals(data, other.data);
    }

}

package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator;

import java.util.HashMap;
import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.ExpressionScriptEvaluator;

public class EvaluationContext {
    private String currentClipId;
    private Map<String, EvaluationContextProviderData> providerData;
    private Map<String, Object> globals;
    private Map<String, Object> lastAvailableVariables = new HashMap<>();
    private ExpressionScriptEvaluator javascriptExpressionEvaluator;

    public EvaluationContext(Map<String, EvaluationContextProviderData> userSetData, ExpressionScriptEvaluator javascriptExpressionEvaluator,
            Map<String, Object> globals) {
        this.providerData = userSetData;
        this.globals = globals;
        this.javascriptExpressionEvaluator = javascriptExpressionEvaluator;
    }

    public Map<String, EvaluationContextProviderData> getProviderData() {
        return providerData;
    }

    public Map<String, Object> getGlobals() {
        return globals;
    }

    public EvaluationContext butWithClipId(String clipId) {
        EvaluationContext result = new EvaluationContext(providerData, javascriptExpressionEvaluator, globals);
        result.currentClipId = clipId;
        return result;
    }

    public <T> T evaluateExpression(String expression, TimelinePosition position, Class<T> toClass) {
        return javascriptExpressionEvaluator.evaluate(expression, position, toClass, this);
    }

    public void addToLastUsedStore(String string, Object data) {
        this.lastAvailableVariables.put(string, data);
    }

    public Map<String, Object> getLastAvailableVariables() {
        return lastAvailableVariables;
    }

}

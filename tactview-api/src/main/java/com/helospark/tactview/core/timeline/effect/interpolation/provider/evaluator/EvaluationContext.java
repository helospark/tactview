package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.ExpressionScriptEvaluator;

public class EvaluationContext {
    private Map<String, EvaluationContextProviderData> providerData;
    private Map<String, Object> globals;
    private Map<String, Map<String, Object>> dynamics = new ConcurrentHashMap<>();

    private Map<String, Object> lastAvailableVariables = new HashMap<>();
    private Map<String, String> exceptions = new HashMap<>();
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

    public Map<String, Map<String, Object>> getDynamics() {
        return dynamics;
    }

    public void addException(String script, String exception) {
        exceptions.put(script, exception);
    }

    public Map<String, String> getExceptions() {
        return exceptions;
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

    public void addDynamicVariable(String id, String keyId, Object data) {
        Map<String, Object> clipElements = dynamics.compute(id, (key, oldValue) -> {
            if (oldValue != null) {
                return oldValue;
            } else {
                return new LinkedHashMap<>();
            }
        });
        clipElements.put(keyId, data);
    }

}

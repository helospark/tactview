package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script.ExpressionScriptEvaluator;

public class EvaluationContext {
    private String currentClipId;
    private Map<String, EvaluationContextProviderData> userSetData;
    private ExpressionScriptEvaluator javascriptExpressionEvaluator;

    public EvaluationContext(Map<String, EvaluationContextProviderData> userSetData, ExpressionScriptEvaluator javascriptExpressionEvaluator) {
        this.userSetData = userSetData;
        this.javascriptExpressionEvaluator = javascriptExpressionEvaluator;
    }

    public EvaluationContext butWithClipId(String clipId) {
        EvaluationContext result = new EvaluationContext(userSetData, javascriptExpressionEvaluator);
        result.currentClipId = clipId;
        return result;
    }

    public <T> T evaluateExpression(String expression, TimelinePosition position, Class<T> toClass) {
        return javascriptExpressionEvaluator.evaluate(expression, position, toClass, userSetData);
    }

}

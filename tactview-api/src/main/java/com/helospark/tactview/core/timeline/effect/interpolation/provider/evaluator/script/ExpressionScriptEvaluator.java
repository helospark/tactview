package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script;

import java.util.Map;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContextProviderData;

public interface ExpressionScriptEvaluator {

    <T> T evaluate(String expression, TimelinePosition position, Class<T> toClass, Map<String, EvaluationContextProviderData> userSetData);

}
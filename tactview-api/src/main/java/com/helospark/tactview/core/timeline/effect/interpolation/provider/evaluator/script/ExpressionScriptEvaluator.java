package com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.script;

import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;

public interface ExpressionScriptEvaluator {

    <T> T evaluate(String expression, TimelinePosition position, Class<T> toClass, EvaluationContext evaluationContext);

}
package com.helospark.tactview.ui.javafx.script;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.evaluator.EvaluationContext;

@Component
public class ScriptVariablesStore {
    private EvaluationContext evaluationContext;

    public void cacheLastContext(EvaluationContext evaluationContext) {
        if (this.evaluationContext == null || !evaluationContext.getProviderData().isEmpty()) {
            this.evaluationContext = evaluationContext;
        }
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }
}

package com.helospark.tactview.core.util.conditional;

import java.util.Arrays;

import com.helospark.lightdi.conditional.condition.ConditionalEvaluationRequest;
import com.helospark.lightdi.conditional.condition.DependencyCondition;

public class ConditionalOnPlatformCondition implements DependencyCondition {
    private TactviewPlatform[] plaforms;

    public ConditionalOnPlatformCondition(TactviewPlatform[] platforms) {
        this.plaforms = platforms;
    }

    @Override
    public boolean evaluate(ConditionalEvaluationRequest request) {
        return Arrays.stream(plaforms)
                .anyMatch(platform -> platform.isActive());
    }

}

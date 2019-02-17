package com.helospark.tactview.core.timeline.effect.interpolation.interpolator.mixed;

import java.util.Arrays;

import penner.easing.Back;
import penner.easing.Circ;
import penner.easing.Cubic;
import penner.easing.Elastic;
import penner.easing.Expo;
import penner.easing.Linear;
import penner.easing.Quad;
import penner.easing.Quart;
import penner.easing.Quint;
import penner.easing.Sine;

public enum EaseFunction {
    LINEAR("linear", Linear::easeIn),

    BACK_IN("backEaseIn", Back::easeIn),
    BACK_OUT("backEaseOut", Back::easeOut),
    BACK_IN_OUT("backEaseInOut", Back::easeInOut),

    CIRC_IN("circEaseIn", Circ::easeIn),
    CIRC_OUT("circEaseOut", Circ::easeOut),
    CIRC_IN_OUT("circEaseInOut", Circ::easeInOut),

    CUBIC_IN("cubicEaseIn", Cubic::easeIn),
    CUBIC_OUT("cubicEaseOut", Cubic::easeOut),
    CUBIC_IN_OUT("cubicEaseInOut", Cubic::easeInOut),

    ELASTIC_IN("elasticEaseIn", Elastic::easeIn),
    ELASTIC_OUT("elasticEaseOut", Elastic::easeOut),
    ELASTIC_IN_OUT("elasticEaseInOut", Elastic::easeInOut),

    EXPO_IN("expoEaseIn", Expo::easeIn),
    EXPO_OUT("expoEaseOut", Expo::easeOut),
    EXPO_IN_OUT("expoEaseInOut", Expo::easeInOut),

    QUAD_IN("quadEaseIn", Quad::easeIn),
    QUAD_OUT("quadEaseOut", Quad::easeOut),
    QUAD_IN_OUT("quadEaseInOut", Quad::easeInOut),

    QUART_IN("quartEaseIn", Quart::easeIn),
    QUART_OUT("quartEaseOut", Quart::easeOut),
    QUART_IN_OUT("quartEaseInOut", Quart::easeInOut),

    QUINT_IN("quintEaseIn", Quint::easeIn),
    QUINT_OUT("quintEaseOut", Quint::easeOut),
    QUINT_IN_OUT("quintEaseInOut", Quint::easeInOut),

    SINE_IN("sineEaseIn", Sine::easeIn),
    SINE_OUT("sineEaseOut", Sine::easeOut),
    SINE_IN_OUT("sineEaseInOut", Sine::easeInOut);

    private String id;
    private PennerFunction function;

    private EaseFunction(String id, PennerFunction function) {
        this.id = id;
        this.function = function;
    }

    public String getId() {
        return id;
    }

    public PennerFunction getFunction() {
        return function;
    }

    public static EaseFunction fromId(String newEasingId) {
        return Arrays.stream(values())
                .filter(id -> id.getId().equals(newEasingId))
                .findFirst()
                .orElseThrow();
    }

}

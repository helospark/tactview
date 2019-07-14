package com.helospark.tactview.core.timeline.alignment;

import java.util.function.BiFunction;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class AlignmentValueListElement extends ValueListElement {
    private BiFunction<Integer, Integer, Integer> function;

    public AlignmentValueListElement(String id, BiFunction<Integer, Integer, Integer> function) {
        super(id, id);
        this.function = function;
    }

    public BiFunction<Integer, Integer, Integer> getFunction() {
        return function;
    }

}

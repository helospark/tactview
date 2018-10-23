package com.helospark.tactview.ui.javafx.inputmode;

import java.util.function.Consumer;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.SizeFunction;
import com.helospark.tactview.ui.javafx.inputmode.strategy.InputTypeStrategy;

public class InputModeInput<T> {
    public Class<T> requestedType;
    public Consumer<T> consumer;
    public InputTypeStrategy<T> currentStrategy;
    public SizeFunction sizeFunction;

    public InputModeInput(Class<T> requestedType, Consumer<T> consumer, InputTypeStrategy<T> currentStrategy,
            SizeFunction sizeFunction) {
        this.requestedType = requestedType;
        this.consumer = consumer;
        this.currentStrategy = currentStrategy;
        this.sizeFunction = sizeFunction;
    }

}

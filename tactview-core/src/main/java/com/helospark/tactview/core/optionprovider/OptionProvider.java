package com.helospark.tactview.core.optionprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Generated;

import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class OptionProvider<T> {
    private String title;
    private Class<T> type;
    private Function<String, T> valueConverter;
    private Function<T, List<String>> validationErrorProvider;
    private Supplier<Boolean> shouldShow;
    private Supplier<Boolean> isEnabled;
    private List<ValueListElement> validValues;
    private boolean shouldTriggerUpdate;
    private T value;

    @Generated("SparkTools")
    private OptionProvider(Builder<T> builder) {
        this.title = builder.title;
        this.valueConverter = builder.valueConverter != null ? builder.valueConverter : stringValue -> null;
        this.validationErrorProvider = builder.validationErrorProvider != null ? builder.validationErrorProvider : value -> List.of();
        this.shouldShow = builder.shouldShow != null ? builder.shouldShow : () -> true;
        this.isEnabled = builder.isEnabled != null ? builder.isEnabled : () -> true;
        this.validValues = builder.validValues != null ? builder.validValues : List.of();
        this.value = builder.defaultValue;
    }

    public List<ValueListElement> getValidValues() {
        return validValues;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public Function<String, T> getValueConverter() {
        return valueConverter;
    }

    public Function<T, List<String>> getValidationErrorProvider() {
        return validationErrorProvider;
    }

    public Supplier<Boolean> getShouldShow() {
        return shouldShow;
    }

    public Supplier<Boolean> getIsEnabled() {
        return isEnabled;
    }

    public boolean shouldTriggerUpdate() {
        return shouldTriggerUpdate;
    }

    @Generated("SparkTools")
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<T>(type);
    }

    public static Builder<Integer> integerOptionBuilder() {
        return new Builder<Integer>(Integer.class)
                .withValueConverter(Integer::valueOf);
    }

    public static Builder<String> stringOptionBuilder() {
        return new Builder<String>(String.class)
                .withValueConverter(String::valueOf);
    }

    public OptionProvider<T> butWithUpdatedValidValues(List<ValueListElement> validValues) {
        OptionProvider<T> cloned = cloneThis();
        cloned.validValues = validValues;

        boolean currentValueContainedInNewValues = false;
        for (ValueListElement element : cloned.validValues) {
            if (element.getId().equals(cloned.value)) {
                currentValueContainedInNewValues = true;
                break;
            }
        }
        if (!currentValueContainedInNewValues) {
            cloned.value = (T) validValues.get(0).getId();
        }
        return cloned;
    }

    private OptionProvider<T> cloneThis() {
        return OptionProvider.builder(this.type)
                .withTitle(title)
                .withShouldShow(shouldShow)
                .withDefaultValue(value)
                .withIsEnabled(isEnabled)
                .withValidationErrorProvider(validationErrorProvider)
                .withValidValues(new ArrayList<>(validValues))
                .withValueConverter(valueConverter)
                .build();
    }

    @Generated("SparkTools")
    public static final class Builder<T> {
        private String title;
        private Class<T> type;
        private Function<String, T> valueConverter;
        private Function<T, List<String>> validationErrorProvider;
        private Supplier<Boolean> shouldShow;
        private Supplier<Boolean> isEnabled;
        private List<ValueListElement> validValues;
        private boolean shouldTriggerUpdate;
        private T defaultValue;

        private Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder<T> withValueConverter(Function<String, T> valueConverter) {
            this.valueConverter = valueConverter;
            return this;
        }

        public Builder<T> withValidationErrorProvider(Function<T, List<String>> validationErrorProvider) {
            this.validationErrorProvider = validationErrorProvider;
            return this;
        }

        public Builder<T> withShouldShow(Supplier<Boolean> shouldShow) {
            this.shouldShow = shouldShow;
            return this;
        }

        public Builder<T> withIsEnabled(Supplier<Boolean> isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder<T> withValidValues(List<ValueListElement> validValues) {
            this.validValues = validValues;
            return this;
        }

        public Builder<T> withDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<T> withShouldTriggerUpdate(boolean shouldTriggerUpdate) {
            this.shouldTriggerUpdate = shouldTriggerUpdate;
            return this;
        }

        public OptionProvider<T> build() {
            return new OptionProvider<>(this);
        }
    }

}

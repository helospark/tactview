package com.helospark.tactview.core.optionprovider;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Generated;

public class OptionProvider<T> {
    private String title;
    private Function<String, T> valueConverter;
    private Function<T, List<String>> validationErrorProvider;
    private Supplier<Boolean> shouldShow;
    private Supplier<Boolean> isEnabled;
    private T value;

    @Generated("SparkTools")
    private OptionProvider(Builder<T> builder) {
        this.title = builder.title;
        this.valueConverter = builder.valueConverter != null ? builder.valueConverter : stringValue -> null;
        this.validationErrorProvider = builder.validationErrorProvider != null ? builder.validationErrorProvider : value -> List.of();
        this.shouldShow = builder.shouldShow != null ? builder.shouldShow : () -> true;
        this.isEnabled = builder.isEnabled != null ? builder.isEnabled : () -> true;
        this.value = builder.defaultValue;
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

    @Generated("SparkTools")
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<T>();
    }

    public static Builder<Integer> integerOptionBuilder() {
        return new Builder<Integer>()
                .withValueConverter(Integer::valueOf);
    }

    @Generated("SparkTools")
    public static final class Builder<T> {
        private String title;
        private Function<String, T> valueConverter;
        private Function<T, List<String>> validationErrorProvider;
        private Supplier<Boolean> shouldShow;
        private Supplier<Boolean> isEnabled;
        private T defaultValue;

        private Builder() {
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

        public Builder<T> withDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public OptionProvider<T> build() {
            return new OptionProvider<>(this);
        }
    }

}

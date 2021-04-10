package com.helospark.tactview.core.optionprovider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Generated;

import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class OptionProvider<T> {
    private final String title;
    private Class<T> type;
    private final Function<String, T> valueConverter;
    private final Function<T, List<String>> validationErrorProvider;
    private final Function<RenderRequest, Boolean> shouldShow;
    private final Function<RenderRequest, Boolean> isEnabled;
    private List<ValueListElement> validValues;
    private boolean shouldTriggerUpdate;
    private T value;
    private final T defaultValue;

    @Generated("SparkTools")
    private OptionProvider(Builder<T> builder) {
        this.type = builder.type;
        this.title = builder.title;
        this.valueConverter = builder.valueConverter != null ? builder.valueConverter : stringValue -> null;
        this.validationErrorProvider = builder.validationErrorProvider != null ? builder.validationErrorProvider : value -> List.of();
        this.shouldShow = builder.shouldShow != null ? builder.shouldShow : a -> true;
        this.isEnabled = builder.isEnabled != null ? builder.isEnabled : a -> true;
        this.validValues = builder.validValues != null ? builder.validValues : List.of();
        this.value = builder.defaultValue;
        this.defaultValue = builder.defaultValue;
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

    public Class<T> getType() {
        return type;
    }

    public Function<String, T> getValueConverter() {
        return valueConverter;
    }

    public Function<T, List<String>> getValidationErrorProvider() {
        return validationErrorProvider;
    }

    public Function<RenderRequest, Boolean> getShouldShow() {
        return shouldShow;
    }

    public Function<RenderRequest, Boolean> getIsEnabled() {
        return isEnabled;
    }

    public boolean shouldTriggerUpdate() {
        return shouldTriggerUpdate;
    }

    @Generated("SparkTools")
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    @Generated("SparkTools")
    public static <T> Builder<T> builder(OptionProvider<?> builder) {
        return new Builder<>(builder);
    }

    public static Builder<Integer> integerOptionBuilder() {
        return new Builder<>(Integer.class)
                .withValueConverter(Integer::valueOf);
    }

    public static Builder<File> fileOptionBuilder() {
        return new Builder<>(File.class)
                .withValueConverter(File::new);
    }

    public static Builder<String> stringOptionBuilder() {
        return new Builder<>(String.class)
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

    @Override
    public String toString() {
        return "OptionProvider [title=" + title + ", type=" + type + ", shouldShow=" + shouldShow + ", isEnabled=" + isEnabled + ", validValues=" + validValues + ", value=" + value + "]";
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof OptionProvider)) {
            return false;
        }
        OptionProvider castOther = (OptionProvider) other;
        return Objects.equals(title, castOther.title) && Objects.equals(type, castOther.type) && Objects.equals(shouldShow, castOther.shouldShow) && Objects.equals(isEnabled, castOther.isEnabled)
                && Objects.equals(validValues, castOther.validValues) && Objects.equals(shouldTriggerUpdate, castOther.shouldTriggerUpdate) && Objects.equals(value, castOther.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, type, shouldShow, isEnabled, validValues, shouldTriggerUpdate, value);
    }

    @Generated("SparkTools")
    public static final class Builder<T> {
        private String title;
        private final Class<T> type;
        private Function<String, T> valueConverter;
        private Function<T, List<String>> validationErrorProvider;
        private Function<RenderRequest, Boolean> shouldShow;
        private Function<RenderRequest, Boolean> isEnabled;
        private List<ValueListElement> validValues;
        private boolean shouldTriggerUpdate;
        private T defaultValue;
        private T previousValue = null;

        private Builder(Class<T> type) {
            this.type = type;
        }

        private Builder(OptionProvider optionProvider) {
            this.title = optionProvider.title;
            this.type = optionProvider.type;
            this.valueConverter = optionProvider.valueConverter;
            this.validationErrorProvider = optionProvider.validationErrorProvider;
            this.shouldShow = optionProvider.shouldShow;
            this.isEnabled = optionProvider.isEnabled;
            this.validValues = optionProvider.validValues;
            this.shouldTriggerUpdate = optionProvider.shouldTriggerUpdate;
            this.defaultValue = (T) optionProvider.defaultValue;
            this.previousValue = (T) optionProvider.value;
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

        public Builder<T> withShouldShow(Function<RenderRequest, Boolean> shouldShow) {
            this.shouldShow = shouldShow;
            return this;
        }

        public Builder<T> withIsEnabled(Function<RenderRequest, Boolean> isEnabled) {
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
            OptionProvider<T> result = new OptionProvider<>(this);
            if (previousValue != null) {
                result.value = previousValue;
            }
            return result;
        }

    }

    public OptionProvider<?> deepClone() {
        return OptionProvider.builder(this).build();
    }

}

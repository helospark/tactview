package com.helospark.tactview.core.optionprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Generated;

import com.helospark.tactview.core.render.RenderRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;

public class OptionProvider<T> {
    private String title;
    private Class<T> type;
    private Function<String, T> valueConverter;
    private Function<T, List<String>> validationErrorProvider;
    private Function<RenderRequest, Boolean> shouldShow;
    private Function<RenderRequest, Boolean> isEnabled;
    private List<ValueListElement> validValues;
    private boolean shouldTriggerUpdate;
    private T value;

    @Generated("SparkTools")
    private OptionProvider(Builder<T> builder) {
        this.title = builder.title;
        this.valueConverter = builder.valueConverter != null ? builder.valueConverter : stringValue -> null;
        this.validationErrorProvider = builder.validationErrorProvider != null ? builder.validationErrorProvider : value -> List.of();
        this.shouldShow = builder.shouldShow != null ? builder.shouldShow : a -> true;
        this.isEnabled = builder.isEnabled != null ? builder.isEnabled : a -> true;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((isEnabled == null) ? 0 : isEnabled.hashCode());
        result = prime * result + ((shouldShow == null) ? 0 : shouldShow.hashCode());
        result = prime * result + (shouldTriggerUpdate ? 1231 : 1237);
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((validValues == null) ? 0 : validValues.hashCode());
        result = prime * result + ((validationErrorProvider == null) ? 0 : validationErrorProvider.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((valueConverter == null) ? 0 : valueConverter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OptionProvider other = (OptionProvider) obj;
        if (isEnabled == null) {
            if (other.isEnabled != null)
                return false;
        } else if (!isEnabled.equals(other.isEnabled))
            return false;
        if (shouldShow == null) {
            if (other.shouldShow != null)
                return false;
        } else if (!shouldShow.equals(other.shouldShow))
            return false;
        if (shouldTriggerUpdate != other.shouldTriggerUpdate)
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (validValues == null) {
            if (other.validValues != null)
                return false;
        } else if (!validValues.equals(other.validValues))
            return false;
        if (validationErrorProvider == null) {
            if (other.validationErrorProvider != null)
                return false;
        } else if (!validationErrorProvider.equals(other.validationErrorProvider))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (valueConverter == null) {
            if (other.valueConverter != null)
                return false;
        } else if (!valueConverter.equals(other.valueConverter))
            return false;
        return true;
    }

    @Generated("SparkTools")
    public static final class Builder<T> {
        private String title;
        private Class<T> type;
        private Function<String, T> valueConverter;
        private Function<T, List<String>> validationErrorProvider;
        private Function<RenderRequest, Boolean> shouldShow;
        private Function<RenderRequest, Boolean> isEnabled;
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
            return new OptionProvider<>(this);
        }

    }

}

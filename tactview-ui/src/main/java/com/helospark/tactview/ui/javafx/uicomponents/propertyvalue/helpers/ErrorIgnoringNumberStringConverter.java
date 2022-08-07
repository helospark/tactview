package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.helpers;

import java.util.regex.Pattern;

import com.helospark.lightdi.annotation.Component;

import javafx.util.converter.NumberStringConverter;

@Component
public class ErrorIgnoringNumberStringConverter extends NumberStringConverter {
    private Pattern numericRegex = Pattern.compile("-?\\d+(\\.\\d+)?");

    @Override
    public Number fromString(String value) {
        if (numericRegex.matcher(value).matches()) {
            return super.fromString(value);
        } else {
            return 0.1;
        }
    }
}

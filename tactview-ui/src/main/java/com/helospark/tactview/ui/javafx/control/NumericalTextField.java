package com.helospark.tactview.ui.javafx.control;

import javafx.scene.control.TextField;

// https://stackoverflow.com/a/18959399/8258222
public class NumericalTextField extends TextField {

    public NumericalTextField() {
        super();
    }

    public NumericalTextField(int initialText) {
        super(Integer.toString(initialText));
    }

    @Override
    public void replaceText(int start, int end, String text) {
        if (validate(text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (validate(text)) {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text) {
        return text.matches("[0-9]*"); // TODO: negatives?
    }

    public int getValue() {
        return Integer.parseInt(this.getText());
    }

    public void setValue(int expectedHeight) {
        super.textProperty().set(Integer.toString(expectedHeight));
    }
}
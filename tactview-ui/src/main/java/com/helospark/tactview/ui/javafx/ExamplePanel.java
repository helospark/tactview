package com.helospark.tactview.ui.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
// additional imports omitted

public class ExamplePanel extends HBox {
    public ExamplePanel() {
        // must be final so that it can be used in the anonymous inner class
        final Label label = new Label("Label");
        TextField textField = new TextField("Text Field");
        Button button = new Button("Button");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                label.setText("Clicked");
            }
        });

        this.getChildren().add(label);
        this.getChildren().add(textField);
        this.getChildren().add(button);
    }
}

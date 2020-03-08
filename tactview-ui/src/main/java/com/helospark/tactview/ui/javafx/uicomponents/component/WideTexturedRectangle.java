package com.helospark.tactview.ui.javafx.uicomponents.component;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Created to avoid `java.lang.RuntimeException: Requested texture dimensions (181489x50) require dimensions (181489x50) that exceed maximum texture size (16384)`
 * @author helospark
 */
public class WideTexturedRectangle {

    static final int MAX_WIDTH_PER_RECTANGLE = 16000;

    private ObservableList<Rectangle> components;

    private SimpleDoubleProperty widthProperty;

    private VBox box;

    public WideTexturedRectangle(int width, int height) {
        components = FXCollections.observableArrayList();

        int currentWidth = width;

        widthProperty.addListener((value, x, y) -> {

        });

        Bindings.bindContentBidirectional((ObservableList<Node>) (Object) components, box.getChildren());

        while (currentWidth > 0) {
            int rectangleWidth;

            if (currentWidth >= MAX_WIDTH_PER_RECTANGLE) {
                rectangleWidth = MAX_WIDTH_PER_RECTANGLE;
            } else {
                rectangleWidth = currentWidth;
            }

            Rectangle rectangle = new Rectangle(rectangleWidth, height);
            components.add(rectangle);
            box.getChildren();

            width -= MAX_WIDTH_PER_RECTANGLE;
        }

        //        widthProperty.
    }

    public DoublePropertyBase widthProperty() {
        return widthProperty;
    }
}

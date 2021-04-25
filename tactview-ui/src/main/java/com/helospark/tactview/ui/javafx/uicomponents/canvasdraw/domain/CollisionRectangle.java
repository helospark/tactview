package com.helospark.tactview.ui.javafx.uicomponents.canvasdraw.domain;

public class CollisionRectangle {
    public double topLeftX, topLeftY;
    public double width, height;

    public CollisionRectangle(double topLeftX, double topLeftY, double width, double height) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    public boolean containsPoint(double x, double y) {
        return (x >= topLeftX && x <= topLeftX + width) &&
                (y >= topLeftY && y <= topLeftY + height);
    }

    public double minXDistanceFromEdge(double x) {
        double leftDistance = Math.abs(x - topLeftX);
        double rightDistance = Math.abs(x - (topLeftX + width));
        return Math.min(leftDistance, rightDistance);
    }

    public boolean intersects(CollisionRectangle other) {
        boolean notIntersecting = (topLeftX > (other.topLeftX + other.width) || (topLeftY > (other.topLeftY + other.height))
                || (topLeftX + width < other.topLeftX) || (topLeftY + height < other.topLeftY));

        return !notIntersecting;
    }

    @Override
    public String toString() {
        return "CollisionRectangle [topLeftX=" + topLeftX + ", topLeftY=" + topLeftY + ", width=" + width + ", height=" + height + "]";
    }

}

package com.helospark.tactview.core.timeline.effect.interpolation.pojo;

public class IntPoint {
    public int x, y;

    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static IntPoint fromPoint(Point other) {
        return new IntPoint((int) other.x, (int) other.y);
    }

    public IntPoint offset(IntPoint intPoint) {
        return new IntPoint(x + intPoint.x, y + intPoint.y);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
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
        IntPoint other = (IntPoint) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

}

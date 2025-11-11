package domain;

import java.util.Objects;

public final class CampusCoordinate {
    private final double x;
    private final double y;

    /** Stores a normalized (0..1) x/y point relative to the campus map. */
    public CampusCoordinate(double x, double y) {
        if (x < 0.0 || x > 1.0) {
            throw new IllegalArgumentException("x must be within [0,1]");
        }
        if (y < 0.0 || y > 1.0) {
            throw new IllegalArgumentException("y must be within [0,1]");
        }
        this.x = x;
        this.y = y;
    }

    /** Returns the normalized horizontal position. */
    public double getX() {
        return x;
    }

    /** Returns the normalized vertical position. */
    public double getY() {
        return y;
    }

    @Override
    /** Ensures coordinates can be compared in collections and tests. */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampusCoordinate other)) return false;
        return Double.compare(other.x, x) == 0 && Double.compare(other.y, y) == 0;
    }

    @Override
    /** Produces a hash consistent with equals for use in maps/sets. */
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}

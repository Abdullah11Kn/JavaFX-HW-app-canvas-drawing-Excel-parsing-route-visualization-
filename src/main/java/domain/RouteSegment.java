package domain;

public class RouteSegment {
    private final Building from;
    private final Building to;
    private final double distanceMeters;

    /** Represents one leg of the walking route between two buildings. */
    public RouteSegment(Building from, Building to, double distanceMeters) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Route segment endpoints are required");
        }
        if (distanceMeters < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        this.from = from;
        this.to = to;
        this.distanceMeters = distanceMeters;
    }

    /** Returns the origin building of the segment. */
    public Building getFrom() {
        return from;
    }

    /** Returns the destination building of the segment. */
    public Building getTo() {
        return to;
    }

    /** Returns the precomputed distance between the two endpoints. */
    public double getDistanceMeters() {
        return distanceMeters;
    }
}

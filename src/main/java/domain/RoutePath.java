package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoutePath {
    private final List<Building> orderedBuildings;
    private final List<RouteSegment> segments;
    private double totalDistanceMeters;

    /** Initializes an empty route ready to collect buildings and segments. */
    public RoutePath() {
        this.orderedBuildings = new ArrayList<>();
        this.segments = new ArrayList<>();
        this.totalDistanceMeters = 0.0;
    }

    /** Appends a building to the chronological list of visited locations. */
    public void addStop(Building building) {
        if (building == null) {
            throw new IllegalArgumentException("Building is required");
        }
        orderedBuildings.add(building);
    }

    /** Records a travel leg between two consecutive buildings. */
    public void addSegment(RouteSegment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("Segment is required");
        }
        segments.add(segment);
    }

    /** Returns the stops in the order the student visits them. */
    public List<Building> getOrderedBuildings() {
        return Collections.unmodifiableList(orderedBuildings);
    }

    /** Provides the immutable list of route segments for rendering. */
    public List<RouteSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /** Returns the accumulated walking distance across the route. */
    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    /** Updates the total distance once segments have been processed. */
    public void setTotalDistanceMeters(double totalDistanceMeters) {
        if (totalDistanceMeters < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        this.totalDistanceMeters = totalDistanceMeters;
    }

    /** Indicates whether any stops have been recorded yet. */
    public boolean isEmpty() {
        return orderedBuildings.isEmpty();
    }
}

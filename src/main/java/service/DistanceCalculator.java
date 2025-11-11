package service;

import domain.Building;
import domain.CampusCoordinate;

public class DistanceCalculator {
    private final double metersPerUnit;

    /** Configures a simple Euclidean distance calculator with a campus scale factor. */
    public DistanceCalculator(double metersPerUnit) {
        if (metersPerUnit <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }
        this.metersPerUnit = metersPerUnit;
    }

    /** Converts normalized building coordinates into meters, returning 0 when inputs are missing. */
    public double calculate(Building from, Building to) {
        if (from == null || to == null) {
            return 0.0;
        }
        CampusCoordinate start = from.getLocation();
        CampusCoordinate end = to.getLocation();
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance * metersPerUnit;
    }
}

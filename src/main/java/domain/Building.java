package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Building {
    private final String code;
    private final String name;
    private final CampusCoordinate location;
    private final List<CampusCoordinate> entrances;

    /** Convenience constructor for buildings without specific entrance coordinates. */
    public Building(String code, String name, CampusCoordinate location) {
        this(code, name, location, List.of());
    }

    /** Fully initializes a building and optionally records entrance points. */
    public Building(String code, String name, CampusCoordinate location, Iterable<CampusCoordinate> entrances) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Building code is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Building name is required");
        }
        if (location == null) {
            throw new IllegalArgumentException("Building location is required");
        }
        this.code = code.trim();
        this.name = name.trim();
        this.location = location;
        this.entrances = new ArrayList<>();
        if (entrances != null) {
            entrances.forEach(e -> {
                if (e != null) {
                    this.entrances.add(e);
                }
            });
        }
    }

    /** Returns the short code used in the Excel sheet (e.g., 22). */
    public String getCode() {
        return code;
    }

    /** Returns the descriptive name (if known) for presentation. */
    public String getName() {
        return name;
    }

    /** Provides the normalized coordinate used for drawing. */
    public CampusCoordinate getLocation() {
        return location;
    }

    /** Lists any additional entrance coordinates registered for the building. */
    public List<CampusCoordinate> getEntrances() {
        return Collections.unmodifiableList(entrances);
    }

    @Override
    /** Formats the building for debugging. */
    public String toString() {
        return code + " (" + name + ")";
    }
}

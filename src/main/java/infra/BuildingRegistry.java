package infra;

import domain.Building;
import domain.CampusCoordinate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BuildingRegistry {
    private final Map<String, Building> buildings = new LinkedHashMap<>();

    /** Returns an existing building by code or creates a placeholder at the center. */
    public Building getOrCreate(String code) {
        return getOrCreate(code, code, null);
    }

    /** Ensures a single Building instance per code, creating one when needed. */
    public Building getOrCreate(String code, String name, CampusCoordinate location) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Building code is required");
        }
        String normalized = code.trim();
        Building existing = buildings.get(normalized);
        if (existing != null) {
            if (location != null && !existing.getLocation().equals(location)) {
                
            }
            return existing;
        }
        CampusCoordinate resolved = location != null ? location : new CampusCoordinate(0.5, 0.5);
        Building building = new Building(normalized, name == null || name.isBlank() ? normalized : name.trim(), resolved);
        buildings.put(normalized, building);
        return building;
    }

    /** Registers a fully populated building, overwriting any placeholder. */
    public void register(Building building) {
        buildings.put(building.getCode(), building);
    }

    /** Returns a building by code without creating a fallback. */
    public Building get(String code) {
        if (code == null) {
            return null;
        }
        return buildings.get(code.trim());
    }

    /** Exposes every registered building as an immutable collection. */
    public Collection<Building> getAll() {
        return Collections.unmodifiableCollection(buildings.values());
    }
}

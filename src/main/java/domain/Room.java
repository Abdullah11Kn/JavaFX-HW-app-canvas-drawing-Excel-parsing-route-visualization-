package domain;

public class Room {
    private final String number;
    private final int floor;
    private final Building building;

    /** Binds a room identifier to its containing building and floor. */
    public Room(String number, int floor, Building building) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }
        if (building == null) {
            throw new IllegalArgumentException("Building is required");
        }
        this.number = number.trim();
        this.floor = floor;
        this.building = building;
    }

    /** Returns the room number (e.g., 120). */
    public String getNumber() {
        return number;
    }

    /** Returns the floor the room is located on. */
    public int getFloor() {
        return floor;
    }

    /** Provides access to the building that owns the room. */
    public Building getBuilding() {
        return building;
    }

    @Override
    /** Combines building code and room for compact display. */
    public String toString() {
        return building.getCode() + "-" + number;
    }
}

package domain;

import java.time.DayOfWeek;

public class MeetingSession {
    private final DayOfWeek day;
    private final TimeSlot timeSlot;
    private final ActivityType activityType;
    private final Room room;

    /** Encapsulates a single scheduled meeting pulled from one Excel row. */
    public MeetingSession(DayOfWeek day, TimeSlot timeSlot, ActivityType activityType, Room room) {
        if (day == null) {
            throw new IllegalArgumentException("Day is required");
        }
        if (timeSlot == null) {
            throw new IllegalArgumentException("Time slot is required");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room is required");
        }
        this.day = day;
        this.timeSlot = timeSlot;
        this.activityType = activityType == null ? ActivityType.OTHER : activityType;
        this.room = room;
    }

    /** Returns the weekday on which the session occurs. */
    public DayOfWeek getDay() {
        return day;
    }

    /** Provides the precise start/end time window for the session. */
    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    /** Identifies the type of meeting (lecture, lab, internship, etc.). */
    public ActivityType getActivityType() {
        return activityType;
    }

    /** Supplies the room where the meeting takes place. */
    public Room getRoom() {
        return room;
    }

    /** Convenience accessor for the building that hosts the session. */
    public Building getBuilding() {
        return room.getBuilding();
    }
}

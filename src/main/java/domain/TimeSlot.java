package domain;

import java.time.Duration;
import java.time.LocalTime;

public class TimeSlot {
    private final LocalTime start;
    private final LocalTime end;

    /** Validates and stores the temporal window for a meeting. */
    public TimeSlot(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end time are required");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }

    /** Returns the inclusive start time of the slot. */
    public LocalTime getStart() {
        return start;
    }

    /** Returns the exclusive end time of the slot. */
    public LocalTime getEnd() {
        return end;
    }

    /** Calculates the total length of the session in minutes/hours. */
    public Duration getDuration() {
        return Duration.between(start, end);
    }

    /** Checks whether two time slots collide on the schedule. */
    public boolean overlaps(TimeSlot other) {
        if (other == null) {
            throw new IllegalArgumentException("Other timeslot is required");
        }
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    /** Formats the slot for logs or debugging. */
    public String toString() {
        return start + " - " + end;
    }
}

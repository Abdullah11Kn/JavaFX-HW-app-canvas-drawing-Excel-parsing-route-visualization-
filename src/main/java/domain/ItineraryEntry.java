package domain;

import java.time.LocalTime;

public class ItineraryEntry {
    private final CourseOffering courseOffering;
    private final MeetingSession session;

    /** Links a specific offering to one of its meeting sessions for an itinerary. */
    public ItineraryEntry(CourseOffering courseOffering, MeetingSession session) {
        if (courseOffering == null) {
            throw new IllegalArgumentException("Course offering is required");
        }
        if (session == null) {
            throw new IllegalArgumentException("Meeting session is required");
        }
        this.courseOffering = courseOffering;
        this.session = session;
    }

    /** Returns the parent course offering for this entry. */
    public CourseOffering getCourseOffering() {
        return courseOffering;
    }

    /** Provides the meeting session details associated with the entry. */
    public MeetingSession getSession() {
        return session;
    }

    /** Convenience accessor for the session start time. */
    public LocalTime getStartTime() {
        return session.getTimeSlot().getStart();
    }

    /** Convenience accessor for the session end time. */
    public LocalTime getEndTime() {
        return session.getTimeSlot().getEnd();
    }
}

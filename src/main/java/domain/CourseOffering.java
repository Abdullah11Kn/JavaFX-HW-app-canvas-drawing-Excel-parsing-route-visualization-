package domain;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CourseOffering {
    private final String crn;
    private final String section;
    private final DeliveryMode deliveryMode;
    private final Course course;
    private Instructor instructor;
    private final List<MeetingSession> sessions;

    /** Captures the metadata for a single CRN/section pairing and seeds its sessions list. */
    public CourseOffering(String crn, String section, DeliveryMode deliveryMode, Course course, Instructor instructor) {
        if (crn == null || crn.isBlank()) {
            throw new IllegalArgumentException("CRN is required");
        }
        if (course == null) {
            throw new IllegalArgumentException("Course is required");
        }
        this.crn = crn.trim();
        this.section = section == null ? "" : section.trim();
        this.deliveryMode = deliveryMode == null ? DeliveryMode.OTHER : deliveryMode;
        this.course = course;
        this.instructor = instructor;
        this.sessions = new ArrayList<>();
    }

    /** Returns the CRN identifier used in lookups and user input. */
    public String getCrn() {
        return crn;
    }

    /** Exposes the section code (lecture, lab, etc.) for display purposes. */
    public String getSection() {
        return section;
    }

    /** Indicates the overall modality (lecture, lab, internship, ...) for this offering. */
    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    /** Provides access to the underlying catalog course metadata. */
    public Course getCourse() {
        return course;
    }

    /** Returns the assigned instructor, which can be null when the sheet omits it. */
    public Instructor getInstructor() {
        return instructor;
    }

    /** Fills in a missing instructor when a later row supplies the name. */
    public void updateInstructorIfMissing(Instructor newInstructor) {
        if (this.instructor == null && newInstructor != null) {
            this.instructor = newInstructor;
        }
    }

    /** Adds a meeting session row to the offering after validating the input. */
    public void addSession(MeetingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session is required");
        }
        sessions.add(session);
    }

    /** Returns every scheduled meeting as an immutable list. */
    public List<MeetingSession> getSessions() {
        return Collections.unmodifiableList(sessions);
    }

    /** Filters the sessions to those that occur on the requested day. */
    public List<MeetingSession> getSessionsByDay(DayOfWeek day) {
        return sessions.stream()
                .filter(s -> s.getDay() == day)
                .collect(Collectors.toUnmodifiableList());
    }
}

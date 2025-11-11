package service;

import domain.CourseOffering;
import domain.DailyItinerary;
import domain.ItineraryEntry;
import domain.MeetingSession;
import domain.TermSchedule;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class ScheduleService {
    private final ScheduleRepository repository;

    /** Provides higher-level schedule queries on top of a repository implementation. */
    public ScheduleService(ScheduleRepository repository) {
        
        if (repository == null) {
            throw new IllegalArgumentException("Schedule repository is required");
        }
        this.repository  = repository;
    }

    /** Retrieves offerings that match the provided CRNs. */
    public List<CourseOffering> getOfferingsByCrns(Collection<String> crns) {
        
        if (crns == null) {
            throw new IllegalArgumentException("CRN collection is required");
        }
        TermSchedule schedule = repository.getTermSchedule();
        return schedule.findAllByCrns(crns);
    }

    /** Builds a day itinerary by first resolving the CRNs. */
    public DailyItinerary getDailyItinerary(Collection<String> crns, DayOfWeek day) {
        return getDailyItineraryFromOfferings(getOfferingsByCrns(crns), day);
    }

    /** Builds a daily itinerary using pre-fetched course offerings. */
    public DailyItinerary getDailyItineraryFromOfferings(Collection<CourseOffering> offerings, DayOfWeek day) {
        
        if (day == null) {
            throw new IllegalArgumentException("Day is required");
        }
        List<ItineraryEntry> entries = new ArrayList<>();
        
        for (CourseOffering offering : offerings) {
            for (MeetingSession session : offering.getSessionsByDay(day)) {
                entries.add(new ItineraryEntry(offering, session));
            }
        }

        DailyItinerary itinerary = new DailyItinerary(day, entries);
        itinerary.sortByStartTime(); 
        return itinerary;
    }

    /** Lists unique course codes present in the supplied offerings. */
    public List<String> listCourseCodes(Collection<CourseOffering> offerings) {
        if (offerings == null) {
            throw new IllegalArgumentException("Offerings collection is required");
        }
        Set<String> codes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        offerings.stream()
                .filter(Objects::nonNull)
                .map(o -> o.getCourse().getCode())
                .forEach(codes::add);
        return new ArrayList<>(codes);
    }

    /** Lists unique course titles from the supplied offerings. */
    public List<String> listCourseTitles(Collection<CourseOffering> offerings) {
        if (offerings == null) {
            throw new IllegalArgumentException("Offerings collection is required");
        }
        Set<String> titles = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        offerings.stream()
                .filter(Objects::nonNull)
                .map(o -> o.getCourse().getTitle())
                .forEach(titles::add);
        return new ArrayList<>(titles);
    }
}

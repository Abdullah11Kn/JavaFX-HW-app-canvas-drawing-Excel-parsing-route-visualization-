package domain;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DailyItinerary {
    private final DayOfWeek day;
    private final List<ItineraryEntry> entries;

    /** Captures all meetings for a single weekday. */
    public DailyItinerary(DayOfWeek day, Iterable<ItineraryEntry> entries) {
        if (day == null) {
            throw new IllegalArgumentException("Day is required");
        }
        this.day = day;
        this.entries = new ArrayList<>();
        if (entries != null) {
            entries.forEach(entry -> {
                if (entry != null) {
                    this.entries.add(entry);
                }
            });
        }
    }

    /** Returns the day this itinerary represents. */
    public DayOfWeek getDay() {
        return day;
    }

    /** Exposes an immutable snapshot of the entries in schedule order. */
    public List<ItineraryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Orders the entries chronologically based on their start times. */
    public void sortByStartTime() {
        entries.sort(Comparator.comparing(ItineraryEntry::getStartTime));
    }
}

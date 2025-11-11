package service;

import domain.Building;
import domain.CourseOffering;
import domain.DailyItinerary;
import domain.ItineraryEntry;
import domain.RoutePath;
import domain.RouteSegment;
import domain.RouteVisualizationModel;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RoutePlanningService {
    private final DistanceCalculator distanceCalculator;

    /** Collaborates with DistanceCalculator to transform itineraries into draw-ready models. */
    public RoutePlanningService(DistanceCalculator distanceCalculator) {
        
        if (distanceCalculator == null) {
            throw new IllegalArgumentException("Distance calculator is required");
        }
        this.distanceCalculator = distanceCalculator;
    }

    /** Generates the visualization payload (route + summary) for the given day. */
    public RouteVisualizationModel buildVisualization(DailyItinerary itinerary) {
        if (itinerary == null) {
            throw new IllegalArgumentException("Itinerary is required");
        }
        List<ItineraryEntry> orderedEntries = new ArrayList<>(itinerary.getEntries());
        orderedEntries.sort(java.util.Comparator.comparing(ItineraryEntry::getStartTime));  

        RoutePath routePath = buildRoutePath(orderedEntries);

        Set<CourseOffering> uniqueOfferings = new LinkedHashSet<>();
        for (ItineraryEntry entry : orderedEntries) {
            uniqueOfferings.add(entry.getCourseOffering());
        }

        List<Building> uniqueBuildings = new ArrayList<>(new LinkedHashSet<>(routePath.getOrderedBuildings()));  

        List<String> summary = buildSummary(itinerary.getDay(), orderedEntries, uniqueOfferings, uniqueBuildings, routePath.getTotalDistanceMeters());

        return new RouteVisualizationModel(itinerary.getDay(), new ArrayList<>(uniqueOfferings), uniqueBuildings, routePath, summary);
    }

    /** Builds an ordered path with segments and total distance from itinerary entries. */
    private RoutePath buildRoutePath(List<ItineraryEntry> entries) {
        RoutePath path = new RoutePath();
        Building lastBuilding = null;
        double totalDistance = 0.0;
        boolean firstAdded = false;

        for (ItineraryEntry entry : entries) {
            Building current = entry.getSession().getBuilding();
            if (current == null) {
                continue;
            }

            if (!firstAdded) {
                path.addStop(current);
                lastBuilding = current;
                firstAdded = true;
                continue;
            }

            path.addStop(current);
            if (!sameBuilding(lastBuilding, current)) {
                double segmentDistance = distanceCalculator.calculate(lastBuilding, current);
                totalDistance += segmentDistance;
                path.addSegment(new RouteSegment(lastBuilding, current, segmentDistance));
            }
            lastBuilding = current;
        }

        path.setTotalDistanceMeters(totalDistance);

        return path;
    }

    /** Formats the textual summary displayed alongside the map. */
    private List<String> buildSummary(DayOfWeek day,
                                      List<ItineraryEntry> orderedEntries,
                                      Set<CourseOffering> offerings,
                                      List<Building> buildings,
                                      double distanceMeters) {
        List<String> lines = new ArrayList<>();
        lines.add("Selected Day: " + capitalize(day));
        lines.add(String.format(Locale.ROOT, "Number of Courses = %d", offerings.size()));

        if (!orderedEntries.isEmpty()) {
            LinkedHashSet<CourseOffering> orderedCourses = new LinkedHashSet<>();
            for (ItineraryEntry entry : orderedEntries) {
                orderedCourses.add(entry.getCourseOffering());
            }
            for (CourseOffering offering : orderedCourses) {
                lines.add("• " + offering.getCourse().getCode() + ": " + offering.getCourse().getTitle());
            }
        }

        lines.add("");
        lines.add(String.format(Locale.ROOT, "Number of Different Buildings = %d", new LinkedHashSet<>(buildings).size()));
        lines.add("");
        lines.add(String.format(Locale.ROOT, "Distance Traveled = %.0f m", distanceMeters));
        return lines;
    }

    /** Compares building codes case-insensitively to detect repeated stops. */
    private boolean sameBuilding(Building a, Building b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getCode().equalsIgnoreCase(b.getCode());
    }

    /** Produces a nicely capitalized day name for the summary. */
    private String capitalize(DayOfWeek day) {
        String lower = day.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

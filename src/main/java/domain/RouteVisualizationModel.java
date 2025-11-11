package domain;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteVisualizationModel {
    private final DayOfWeek day;
    private final List<CourseOffering> courses;
    private final List<Building> buildings;
    private final RoutePath routePath;
    private final List<String> summaryLines;

    /** Bundles all data needed by the UI to render the visualization and summary. */
    public RouteVisualizationModel(DayOfWeek day,
                                   List<CourseOffering> courses,
                                   List<Building> buildings,
                                   RoutePath routePath,
                                   List<String> summaryLines) {
        if (day == null) {
            throw new IllegalArgumentException("Day is required");
        }
        if (courses == null || buildings == null || routePath == null || summaryLines == null) {
            throw new IllegalArgumentException("Visualization components cannot be null");
        }
        this.day = day;
        this.courses = new ArrayList<>(courses);
        this.buildings = new ArrayList<>(buildings);
        this.routePath = routePath;
        this.summaryLines = new ArrayList<>(summaryLines);
    }

    /** Returns the day represented by this visualization. */
    public DayOfWeek getDay() {
        return day;
    }

    /** Supplies the distinct course offerings included in the route. */
    public List<CourseOffering> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    /** Lists the buildings traversed during the day. */
    public List<Building> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    /** Exposes the path object used for drawing arrows. */
    public RoutePath getRoutePath() {
        return routePath;
    }

    /** Provides formatted text describing the visualization. */
    public List<String> getSummaryLines() {
        return Collections.unmodifiableList(summaryLines);
    }
}

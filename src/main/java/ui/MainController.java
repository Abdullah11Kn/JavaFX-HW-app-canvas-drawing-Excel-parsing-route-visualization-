package ui;

import domain.DailyItinerary;
import domain.CourseOffering;
import domain.RouteVisualizationModel;
import infra.BuildingRegistry;
import infra.CoordinateSeeder;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import service.DistanceCalculator;
import service.ExcelScheduleRepository;
import service.RoutePlanningService;
import service.ScheduleRepository;
import service.ScheduleService;

import java.io.File;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainController {
    private final MainView view;
    private final BuildingRegistry buildingRegistry = new BuildingRegistry();
    private RoutePlanningService routePlanningService;
    private DistanceCalculator distanceCalculator;
    private ScheduleRepository repository;
    private ScheduleService scheduleService;
    private Image mapImage;

    /** Wires the controller to the view that hosts user-interaction controls. */
    public MainController(MainView view) {
        this.view = Objects.requireNonNull(view, "view");
    }

    /** Initializes resources, seeds coordinates, and hooks up button handlers. */
    public void initialize() {
        mapImage = new Image(Objects.requireNonNull(getClass().getResource("/map.png"), "map.png not found").toExternalForm());
        view.getMapPane().setBackgroundImage(mapImage);
        CoordinateSeeder.seed(buildingRegistry, mapImage);
        calibrateDistanceScale(350.0, "59", "11");

        view.getLoadButton().setOnAction(event -> handleLoadExcel());
        view.getVisualizeButton().setOnAction(event -> handleVisualize());
    }

    /** Calibrates the distance scale using a known real-world distance between two buildings. */
    private void calibrateDistanceScale(double actualMeters, String fromCode, String toCode) {
        var from = buildingRegistry.get(fromCode);
        var to = buildingRegistry.get(toCode);

        if (from != null && to != null) {
            double dx = to.getLocation().getX() - from.getLocation().getX();
            double dy = to.getLocation().getY() - from.getLocation().getY();
            double normalized = Math.hypot(dx, dy);
            if (normalized > 0) {
                double metersPerUnit = actualMeters / normalized;
                distanceCalculator = new DistanceCalculator(metersPerUnit);
                routePlanningService = new RoutePlanningService(distanceCalculator);
                return;
            }
        }
        distanceCalculator = new DistanceCalculator(900);
        routePlanningService = new RoutePlanningService(distanceCalculator);
    }

    /** Lets the user pick the Excel file and loads it into memory. */
    private void handleLoadExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Term Schedule Excel File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        Window window = view.getScene() != null ? view.getScene().getWindow() : null;
        File selected = chooser.showOpenDialog(window);
        if (selected == null) {
            return;
        }
        Path path = selected.toPath();
        try {
            repository = new ExcelScheduleRepository(path, buildingRegistry);
            scheduleService = new ScheduleService(repository);
            int count = repository.getTermSchedule().allOfferings().size();
            view.getSummaryArea().setText("Loaded offerings: " + count + System.lineSeparator() + path);
            view.getMapPane().clearVisualization();
            showMessage(Alert.AlertType.INFORMATION, "Excel Loaded", "Schedule data loaded successfully.");
        } catch (RuntimeException ex) {
            showMessage(Alert.AlertType.ERROR, "Load Failed", "Unable to read Excel file: " + ex.getMessage());
        }
    }

    /** Builds the visualization after validating CRNs and selected day. */
    private void handleVisualize() {
        if (scheduleService == null) {
            showMessage(Alert.AlertType.WARNING, "Missing Data", "Load the Excel file before visualizing.");
            return;
        }

        List<String> crns = parseCrns(view.getCrnField().getText());
        if (crns.isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Invalid Input", "Enter at least one CRN.");
            return;
        }

        DayOfWeek day = parseDay(view.getDaySelector().getValue());
        List<CourseOffering> offerings = scheduleService.getOfferingsByCrns(crns);
        List<String> missingCrns = findMissingCrns(crns, offerings);
        if (!missingCrns.isEmpty()) {
            showMessage(Alert.AlertType.WARNING,
                    "Unknown CRNs",
                    "The following CRNs were not found: " + String.join(", ", missingCrns));
        }

        DailyItinerary itinerary = scheduleService.getDailyItineraryFromOfferings(offerings, day);
        if (itinerary.getEntries().isEmpty()) {
            view.getMapPane().clearVisualization();
            view.getSummaryArea().setText("No sessions found for " + day + " with the selected CRNs.");
            return;
        }

        RouteVisualizationModel model = routePlanningService.buildVisualization(itinerary);
        view.getMapPane().setVisualizationModel(model);

        String summaryText = String.join(System.lineSeparator(), model.getSummaryLines());
        if (!missingCrns.isEmpty()) {
            summaryText = "Missing CRNs: " + String.join(", ", missingCrns) + System.lineSeparator() + summaryText;
        }
        view.getSummaryArea().setText(summaryText);
    }

    /** Splits the CRN input into unique, trimmed tokens. */
    private List<String> parseCrns(String raw) {
        if (raw == null) {
            return List.of();
        }
        return Arrays.stream(raw.split("[\\s,;]+"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /** Computes which requested CRNs were not found in the loaded offerings. */
   private List<String> findMissingCrns(List<String> requested, List<CourseOffering> offerings) {
        java.util.Set<String> found = offerings.stream()
                .map(o -> o.getCrn().trim())
                .collect(Collectors.toSet());
        return requested.stream()
                .filter(crn -> !found.contains(crn.trim()))
                .collect(Collectors.toList());
    }

    /** Converts the UI-selected day string into a DayOfWeek enum value. */
    private DayOfWeek parseDay(String value) {
        if (value == null || value.isBlank()) {
            return DayOfWeek.MONDAY;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "sunday" -> DayOfWeek.SUNDAY;
            case "monday" -> DayOfWeek.MONDAY;
            case "tuesday" -> DayOfWeek.TUESDAY;
            case "wednesday" -> DayOfWeek.WEDNESDAY;
            case "thursday" -> DayOfWeek.THURSDAY;
            case "friday" -> DayOfWeek.FRIDAY;
            case "saturday" -> DayOfWeek.SATURDAY;
            default -> DayOfWeek.MONDAY;
        };
    }

    /** Displays alerts on the JavaFX application thread. */
    private void showMessage(Alert.AlertType type, String title, String message) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showMessage(type, title, message));
            return;
        }
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

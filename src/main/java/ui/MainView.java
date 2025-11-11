package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final TextField crnField;
    private final ComboBox<String> daySelector;
    private final Button loadButton;
    private final Button visualizeButton;
    private final TextArea summaryArea;
    private final MapCanvasPane mapPane;

    /** Builds the UI layout containing input controls, summary area, and map pane. */
    public MainView() {
        setPadding(new Insets(10));

        mapPane = new MapCanvasPane();
        setCenter(mapPane);

        crnField = new TextField();
        crnField.setPromptText("Enter CRNs (comma separated)");

        daySelector = new ComboBox<>();
        daySelector.getItems().addAll("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday");
        daySelector.getSelectionModel().select("Monday");

        loadButton = new Button("Load Excel");
        visualizeButton = new Button("Visualize");

        summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setWrapText(true);

        GridPane buttonRow = new GridPane();
        buttonRow.setHgap(10);
        buttonRow.add(loadButton, 0, 0);
        buttonRow.add(visualizeButton, 1, 0);

        VBox controlBox = new VBox(10,
                new Label("CRNs"), crnField,
                new Label("Weekday"), daySelector,
                buttonRow
        );
        controlBox.setAlignment(Pos.TOP_LEFT);
        controlBox.setPadding(new Insets(0, 10, 0, 0));

        VBox leftBox = new VBox(10,
                controlBox,
                new Label("Summary"),
                summaryArea
        );
        leftBox.setPrefWidth(320);
        VBox.setVgrow(summaryArea, Priority.ALWAYS);

        setLeft(leftBox);
        setCenter(mapPane);
    }

    /** Exposes the CRN input text field for controller wiring. */
    public TextField getCrnField() {
        return crnField;
    }

    /** Exposes the weekday selector combo box. */
    public ComboBox<String> getDaySelector() {
        return daySelector;
    }

    /** Provides access to the "Load Excel" button. */
    public Button getLoadButton() {
        return loadButton;
    }

    /** Provides access to the "Visualize" button. */
    public Button getVisualizeButton() {
        return visualizeButton;
    }

    /** Returns the summary text area shown on the left. */
    public TextArea getSummaryArea() {
        return summaryArea;
    }

    /** Returns the canvas pane used for drawing the route. */
    public MapCanvasPane getMapPane() {
        return mapPane;
    }
}

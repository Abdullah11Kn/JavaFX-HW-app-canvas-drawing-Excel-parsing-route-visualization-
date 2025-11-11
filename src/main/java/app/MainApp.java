package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.MainController;
import ui.MainView;

public class MainApp extends Application {
    @Override
    /** Bootstraps the JavaFX UI and initializes controllers. */
    public void start(Stage primaryStage) {
        MainView view = new MainView();
        MainController controller = new MainController(view);
        controller.initialize();

        Scene scene = new Scene(view, 1200, 800);
        primaryStage.setTitle("Term Schedule Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Standard JavaFX entry point. */
    public static void main(String[] args) {
        launch(args);
    }
}

package game;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Main Application class which starts the JavaFX application
 *
 */
public class MainApplication extends Application {

    public class Controller {
        /* The view of our application */
        private View view;
        public Controller(View view) {
            this.view = view;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * This is where the JavaFX windows starts and stage is passed as argument
     * 
     * @param primaryStage
     *          The primary stage of the JavaFX application
     */

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Block World");
        primaryStage.setResizable(false);
        View view = new View(primaryStage);
        new Controller(view);
        primaryStage.setScene(view.getScene());
        primaryStage.show();
        view.welcomeScreen();
    }
}

package ca.utoronto.utm.assignment2.paint;


import ca.utoronto.utm.assignment2.paint.app.PaintModel;
import ca.utoronto.utm.assignment2.paint.app.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class Paint extends Application {

    PaintModel model; // Model
    View view; // View + Controller

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        this.model = new PaintModel();

        // View + Controller
        this.view = new View(model, stage);
        stage.setWidth(700);
        stage.setHeight(500);
    }
}

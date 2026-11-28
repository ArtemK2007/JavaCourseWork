package ua.sumdu.oop;

import javafx.application.Application;
import javafx.stage.Stage;
import ua.sumdu.oop.ui.LoginView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new LoginView().show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
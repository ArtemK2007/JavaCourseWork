package ua.sumdu.oop.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ua.sumdu.oop.dao.DatabaseConnection;

public class LoginView {

    public void show(Stage stage) {
        Label titleLabel = new Label("Підключення до бази даних PostgreSQL");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField urlField = new TextField(DatabaseConnection.getEnv("DB_URL", "jdbc:postgresql://localhost:5433/company_db"));
        urlField.setPrefWidth(280);

        TextField userField = new TextField(DatabaseConnection.getEnv("POSTGRES_USER", ""));
        userField.setPromptText("Логін користувача БД");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль користувача БД");

        Button connectButton = new Button("Підключитися");
        connectButton.setDefaultButton(true);
        connectButton.setPrefWidth(150);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("URL бази даних:"), 0, 0);
        grid.add(urlField, 1, 0);
        grid.add(new Label("Користувач:"), 0, 1);
        grid.add(userField, 1, 1);
        grid.add(new Label("Пароль:"), 0, 2);
        grid.add(passwordField, 1, 2);

        connectButton.setOnAction(e -> {
            String url = urlField.getText().trim();
            String user = userField.getText().trim();
            String pass = passwordField.getText();

            if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Помилка валідації", "Будь ласка, заповніть усі поля підключення.");
                return;
            }

            if (DatabaseConnection.testConnection(url, user, pass)) {
                new MainView().show(stage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Помилка підключення",
                        "Не вдалося підключитися до PostgreSQL.\nПеревірте URL, логін, пароль або стан контейнера Docker.");
            }
        });

        VBox root = new VBox(18, titleLabel, grid, connectButton);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        stage.setTitle("Авторизація — Курсова робота (Варіант 5)");
        stage.setScene(new Scene(root, 460, 260));
        stage.setResizable(false);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
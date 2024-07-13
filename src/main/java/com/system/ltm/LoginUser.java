package com.system.ltm;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginUser extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        primaryStage.setScene(createLoginScene());
        primaryStage.show();
    }

    public Scene createLoginScene() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        grid.add(usernameLabel, 0, 0);

        usernameField = new TextField();
        grid.add(usernameField, 1, 0);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 1);

        passwordField = new PasswordField();
        grid.add(passwordField, 1, 1);

        loginButton = new Button("Login");
        grid.add(loginButton, 1, 2);

        loginButton.setOnAction(e -> handleLogin());

        return new Scene(grid, 300, 200);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = DatabaseConnection.authenticateUser(username, password);
            if (user != null) {
                if ("ADMIN".equals(user.getRole())) {
                    AdminClient adminClient = new AdminClient(user);
                    adminClient.start(new Stage());
                } else {
                    UserClient userClient = new UserClient(user);
                    userClient.start(new Stage());
                }
                ((Stage) loginButton.getScene().getWindow()).close();
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "An error occurred while logging in.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
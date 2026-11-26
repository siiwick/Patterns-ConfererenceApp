package patterns.lia.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;
import patterns.lia.service.AuthService;
import patterns.lia.utilities.UserSession;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService(DatabaseManager.getDefault());

    @FXML
    private void initialize() {
        clearError();
    }

    @FXML
    public void login() throws IOException {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        User user;
        try {
            user = authService.login(email, password);
        } catch (DataAccessException e) {
            showError("The data could not be saved or read. Please try again.");
            return;
        }

        if (user == null) {
            passwordField.clear();
            showError("Incorrect email or password.");
            return;
        }

        UserSession.login(user);

        String fxml = user.getRole() == Role.ADMIN ? "/admin UI/adminDashboard.fxml" : "/participant UI/participantDashboard.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Conference Manager");
        stage.centerOnScreen();
    }

    @FXML
    public void openCreateAccount() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/createAccount.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Create Account");
        stage.centerOnScreen();
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}

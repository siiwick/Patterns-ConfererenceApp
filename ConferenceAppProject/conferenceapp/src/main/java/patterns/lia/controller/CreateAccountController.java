package patterns.lia.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.exceptions.DuplicateEmailException;
import patterns.lia.model.User;
import patterns.lia.service.AuthService;
import patterns.lia.utilities.UserSession;

public class CreateAccountController {

    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 120;
    private static final int MIN_PASSWORD_LENGTH = 6;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Spinner<Integer> ageSpinner;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService(DatabaseManager.getDefault());

    @FXML
    private void initialize() {
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_AGE, MAX_AGE, 18));
        clearError();
    }

    @FXML
    public void createAccount() throws IOException {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Integer age = readAge();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in every field.");
            return;
        }
        if (age == null) {
            showError("Age must be a number between " + MIN_AGE + " and " + MAX_AGE + ".");
            return;
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            showError("Please enter a valid email address.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.clear();
            showError("Passwords do not match.");
            return;
        }

        User user;
        try {
            user = authService.register(firstName + " " + lastName, email, age, password);
        } catch (DuplicateEmailException e) {
            showError("An account with this email already exists.");
            return;
        } catch (DataAccessException e) {
            showError("The data could not be saved or read. Please try again.");
            return;
        }

        UserSession.login(user);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/participant UI/participantDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Conference Manager");
        stage.centerOnScreen();
    }

    @FXML
    public void backToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root, 450, 520));
        stage.setTitle("Login");
        stage.centerOnScreen();
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    private Integer readAge() {
        try {
            int age = Integer.parseInt(ageSpinner.getEditor().getText().trim());
            return (age >= MIN_AGE && age <= MAX_AGE) ? age : null;
        } catch (NumberFormatException e) {
            return null;
        }
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

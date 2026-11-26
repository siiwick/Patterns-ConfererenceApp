package patterns.lia.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ResourceBundle;

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
import patterns.lia.utilities.LanguageListener;
import patterns.lia.utilities.LanguageManager;
import patterns.lia.utilities.UserSession;

public class CreateAccountController implements LanguageListener {

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
    private final LanguageManager lang = LanguageManager.get();

    @FXML
    private void initialize() {
        lang.addListener(this);
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
            showError(lang.get("validation.required"));
            return;
        }
        if (age == null) {
            showError(lang.format("validation.age.range", MIN_AGE, MAX_AGE));
            return;
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            showError(lang.get("validation.email.invalid"));
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(lang.format("validation.password.short", MIN_PASSWORD_LENGTH));
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.clear();
            showError(lang.get("validation.password.mismatch"));
            return;
        }

        User user;
        try {
            user = authService.register(firstName + " " + lastName, email, age, password);
        } catch (DuplicateEmailException | DataAccessException e) {
            showError(lang.get(e.getMessage()));
            return;
        }

        UserSession.login(user);
        lang.removeListener(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/participant UI/participantDashboard.fxml"), lang.getBundle());
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(lang.get("app.title"));
        stage.centerOnScreen();
    }

    @FXML
    public void backToLogin() throws IOException {
        lang.removeListener(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), lang.getBundle());
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root, 450, 520));
        stage.setTitle(lang.get("login.title"));
        stage.centerOnScreen();
    }

    @FXML
    public void switchLanguage() {
        lang.toggle();
    }

    //Observer: reload this screen in the new language and copy over what the user already typed
    @Override
    public void onLanguageChanged(ResourceBundle bundle) {
        lang.removeListener(this);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/createAccount.fxml"), bundle);
            Parent root = loader.load();
            CreateAccountController newController = loader.getController();
            newController.firstNameField.setText(firstNameField.getText());
            newController.lastNameField.setText(lastNameField.getText());
            newController.ageSpinner.getEditor().setText(ageSpinner.getEditor().getText());
            newController.emailField.setText(emailField.getText());
            newController.passwordField.setText(passwordField.getText());
            newController.confirmPasswordField.setText(confirmPasswordField.getText());

            //new scene so the window resizes to fit the longer or shorter text
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle(bundle.getString("createAccount.title"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

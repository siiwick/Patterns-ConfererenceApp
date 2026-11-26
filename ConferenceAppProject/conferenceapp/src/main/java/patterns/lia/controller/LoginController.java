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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;
import patterns.lia.service.AuthService;
import patterns.lia.utilities.LanguageListener;
import patterns.lia.utilities.LanguageManager;
import patterns.lia.utilities.UserSession;

public class LoginController implements LanguageListener {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService(DatabaseManager.getDefault());
    private final LanguageManager lang = LanguageManager.get();

    @FXML
    private void initialize() {
        lang.addListener(this);
        clearError();
    }

    @FXML
    public void login() throws IOException {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError(lang.get("validation.login.empty"));
            return;
        }

        User user;
        try {
            user = authService.login(email, password);
        } catch (DataAccessException e) {
            showError(lang.get(e.getMessage()));
            return;
        }

        if (user == null) {
            passwordField.clear();
            showError(lang.get("validation.login.invalid"));
            return;
        }

        UserSession.login(user);
        lang.removeListener(this);

        String fxml = user.getRole() == Role.ADMIN ? "/admin UI/adminDashboard.fxml" : "/participant UI/participantDashboard.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), lang.getBundle());
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(lang.get("app.title"));
        stage.centerOnScreen();
    }

    @FXML
    public void openCreateAccount() throws IOException {
        lang.removeListener(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/createAccount.fxml"), lang.getBundle());
        Parent root = loader.load();
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(lang.get("createAccount.title"));
        stage.centerOnScreen();
    }

    @FXML
    public void switchLanguage() {
        lang.toggle();
    }

    //Observer: reload this screen in the new language and keep the typed email
    @Override
    public void onLanguageChanged(ResourceBundle bundle) {
        lang.removeListener(this);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), bundle);
            Parent root = loader.load();
            LoginController newController = loader.getController();
            newController.emailField.setText(emailField.getText());

            emailField.getScene().setRoot(root);
            ((Stage) root.getScene().getWindow()).setTitle(bundle.getString("login.title"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

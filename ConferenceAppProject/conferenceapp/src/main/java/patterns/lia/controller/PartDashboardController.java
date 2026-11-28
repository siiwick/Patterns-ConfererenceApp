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
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.AccessDeniedException;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.exceptions.DuplicateEmailException;
import patterns.lia.exceptions.ValidationException;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;
import patterns.lia.service.UserService;
import patterns.lia.utilities.LanguageListener;
import patterns.lia.utilities.LanguageManager;
import patterns.lia.utilities.UserSession;

public class PartDashboardController implements LanguageListener {

    private static final String SUCCESS_STYLE = "-fx-text-fill: #2e7d32;";
    private static final String ERROR_STYLE = "-fx-text-fill: #c62828;";

    @FXML
    private Label usernameLabel;

    @FXML
    private TabPane tabPane;

    //profile tab
    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private Spinner<Integer> ageSpinner;

    @FXML
    private Label profileFeedbackLabel;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordFeedbackLabel;

    private final LanguageManager lang = LanguageManager.get();
    private final UserService userService = new UserService(DatabaseManager.getDefault());

    @FXML
    private void initialize() {
        //only participants
        User user = UserSession.requireRole(Role.PARTICIPANT);
        usernameLabel.setText(user.getFullName());

        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(UserService.MIN_AGE, UserService.MAX_AGE, user.getAge()));
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());

        lang.addListener(this);
    }

    @FXML
    public void saveProfile() {
        try {
            User updated = userService.updateProfile(fullNameField.getText(), emailField.getText(), readAge());
            usernameLabel.setText(updated.getFullName());
            fullNameField.setText(updated.getFullName());
            emailField.setText(updated.getEmail());
            showFeedback(profileFeedbackLabel, lang.get("profile.saved"), false);
        } catch (ValidationException e) {
            showFeedback(profileFeedbackLabel, message(e), true);
        } catch (DuplicateEmailException | DataAccessException | AccessDeniedException e) {
            showFeedback(profileFeedbackLabel, lang.get(e.getMessage()), true);
        }
    }

    @FXML
    public void changePassword() {
        try {
            userService.changePassword(currentPasswordField.getText(), newPasswordField.getText(), confirmPasswordField.getText());
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            showFeedback(passwordFeedbackLabel, lang.get("profile.password.changed"), false);
        } catch (ValidationException e) {
            showFeedback(passwordFeedbackLabel, message(e), true);
        } catch (DataAccessException | AccessDeniedException e) {
            showFeedback(passwordFeedbackLabel, lang.get(e.getMessage()), true);
        }
    }

    private Integer readAge() {
        try {
            return Integer.parseInt(ageSpinner.getEditor().getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    //only messages with values go through MessageFormat
    private String message(ValidationException e) {
        return e.getArgs().length == 0 ? lang.get(e.getMessage()) : lang.format(e.getMessage(), e.getArgs());
    }

    private void showFeedback(Label label, String text, boolean error) {
        label.setStyle(error ? ERROR_STYLE : SUCCESS_STYLE);
        label.setText(text);
    }

    @FXML
    public void logout() throws IOException {
        UserSession.logout();
        lang.removeListener(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"), lang.getBundle());
        Parent root = loader.load();
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.setScene(new Scene(root, 450, 520));
        stage.setTitle(lang.get("login.title"));
        stage.centerOnScreen();
    }

    @FXML
    public void switchLanguage() {
        lang.toggle();
    }

    //Observer: reload the dashboard in the new language, stay on the same tab and keep unsaved profile edits
    @Override
    public void onLanguageChanged(ResourceBundle bundle) {
        lang.removeListener(this);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/participant UI/participantDashboard.fxml"), bundle);
            Parent root = loader.load();
            PartDashboardController newController = loader.getController();
            newController.tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex());
            newController.fullNameField.setText(fullNameField.getText());
            newController.emailField.setText(emailField.getText());
            newController.ageSpinner.getEditor().setText(ageSpinner.getEditor().getText());
            newController.currentPasswordField.setText(currentPasswordField.getText());
            newController.newPasswordField.setText(newPasswordField.getText());
            newController.confirmPasswordField.setText(confirmPasswordField.getText());

            usernameLabel.getScene().setRoot(root);
            ((Stage) root.getScene().getWindow()).setTitle(bundle.getString("app.title"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @FXML
    public void exit() {
        Platform.exit();
    }
}

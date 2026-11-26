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
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;
import patterns.lia.utilities.LanguageListener;
import patterns.lia.utilities.LanguageManager;
import patterns.lia.utilities.UserSession;

public class PartDashboardController implements LanguageListener {

    @FXML
    private Label usernameLabel;

    @FXML
    private TabPane tabPane;

    private final LanguageManager lang = LanguageManager.get();

    @FXML
    private void initialize() {
        //only participants
        User user = UserSession.requireRole(Role.PARTICIPANT);
        usernameLabel.setText(user.getFullName());
        lang.addListener(this);
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

    //Observer: reload the dashboard in the new language and stay on the same tab
    @Override
    public void onLanguageChanged(ResourceBundle bundle) {
        lang.removeListener(this);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/participant UI/participantDashboard.fxml"), bundle);
            Parent root = loader.load();
            PartDashboardController newController = loader.getController();
            newController.tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex());

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

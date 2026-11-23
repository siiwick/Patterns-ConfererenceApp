package patterns.lia.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import patterns.lia.model.User;
import patterns.lia.utilities.UserSession;

public class PartDashboardController {

    @FXML
    private Label usernameLabel;

    @FXML
    private void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getFullName());
        }
    }

    @FXML
    public void logout() throws IOException {
        UserSession.logout();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.setScene(new Scene(root, 450, 520));
        stage.setTitle("Login");
        stage.centerOnScreen();
    }

    @FXML
    public void exit() {
        Platform.exit();
    }
}

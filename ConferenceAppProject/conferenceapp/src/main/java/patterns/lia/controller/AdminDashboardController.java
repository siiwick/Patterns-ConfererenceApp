package patterns.lia.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;
import patterns.lia.utilities.LanguageListener;
import patterns.lia.utilities.LanguageManager;
import patterns.lia.utilities.UserSession;

public class AdminDashboardController implements LanguageListener {

    //message keys of the six reports, in the order of the listView
    private static final List<String> REPORT_KEYS = List.of(
            "report.conferenceOverview",
            "report.conferenceParticipants",
            "report.waitingList",
            "report.participantRegistrations",
            "report.occupancy",
            "report.unregisteredConferences");

    @FXML
    private Label usernameLabel;

    @FXML
    private TabPane tabPane;

    @FXML
    private ListView<String> reportList;

    @FXML
    private Label reportDetailsLabel;

    @FXML
    private Label feedbackLabel;

    private final LanguageManager lang = LanguageManager.get();

    @FXML
    private void initialize() {
        //only administrators
        User user = UserSession.requireRole(Role.ADMIN);
        usernameLabel.setText(user.getFullName());

        reportList.getItems().setAll(REPORT_KEYS.stream().map(lang::get).toList());
        reportList.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> showReport(newIndex.intValue()));

        lang.addListener(this);
    }

    //for now only shows which report was picked. the report content will come later
    private void showReport(int index) {
        if (index < 0) {
            reportDetailsLabel.setText(lang.get("reports.details.placeholder"));
            feedbackLabel.setText("");
            return;
        }
        String reportName = lang.get(REPORT_KEYS.get(index));
        reportDetailsLabel.setText(reportName);
        feedbackLabel.setText(lang.format("reports.feedback.selected", reportName));
    }

    //goes back to the first tab and clears the selected report
    @FXML
    public void backToMenu() {
        reportList.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().selectFirst();
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

    //Observer: reload the dashboard in the new language and stay on the same tab and report
    @Override
    public void onLanguageChanged(ResourceBundle bundle) {
        lang.removeListener(this);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin UI/adminDashboard.fxml"), bundle);
            Parent root = loader.load();
            AdminDashboardController newController = loader.getController();
            newController.tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex());
            int selectedReport = reportList.getSelectionModel().getSelectedIndex();
            if (selectedReport >= 0) {
                newController.reportList.getSelectionModel().select(selectedReport);
            }

            usernameLabel.getScene().setRoot(root);
            ((Stage) root.getScene().getWindow()).setTitle(bundle.getString("admin.title"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @FXML
    public void exit() {
        Platform.exit();
    }
}

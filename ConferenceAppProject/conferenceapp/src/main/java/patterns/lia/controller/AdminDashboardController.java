package patterns.lia.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import patterns.lia.db.DatabaseManager;
import patterns.lia.exceptions.AccessDeniedException;
import patterns.lia.exceptions.DataAccessException;
import patterns.lia.model.User;
import patterns.lia.model.enums.ReportInput;
import patterns.lia.model.enums.Role;
import patterns.lia.model.report.ReportOption;
import patterns.lia.model.report.ReportTable;
import patterns.lia.service.ReportService;
import patterns.lia.service.report.ReportStrategy;
import patterns.lia.utilities.LanguageListener;
import patterns.lia.utilities.LanguageManager;
import patterns.lia.utilities.UserSession;

public class AdminDashboardController implements LanguageListener {

    @FXML
    private Label usernameLabel;

    @FXML
    private TabPane tabPane;

    @FXML
    private ListView<String> reportList;

    @FXML
    private Label reportTitleLabel;

    @FXML
    private HBox selectorBox;

    @FXML
    private Label selectorLabel;

    @FXML
    private ComboBox<ReportOption> optionBox;

    @FXML
    private TableView<List<String>> reportTable;

    @FXML
    private Label feedbackLabel;

    private final LanguageManager lang = LanguageManager.get();
    private final ReportService reportService = new ReportService(DatabaseManager.getDefault());

    //the reports in the order of the listView
    private List<ReportStrategy> reports;

    @FXML
    private void initialize() {
        //only administrators
        User user = UserSession.requireRole(Role.ADMIN);
        usernameLabel.setText(user.getFullName());

        reports = reportService.getReports();
        reportList.getItems().setAll(reports.stream().map(r -> lang.get(r.getNameKey())).toList());
        reportList.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> showReport(newIndex.intValue()));

        reportTable.setPlaceholder(new Label(lang.get("reports.details.placeholder")));

        lang.addListener(this);
    }

    //called when a report is picked in the list
    private void showReport(int index) {
        clearTable(lang.get("reports.details.placeholder"));
        if (index < 0) {
            reportTitleLabel.setText("");
            showSelector(false);
            feedbackLabel.setText("");
            return;
        }

        ReportStrategy report = reports.get(index);
        reportTitleLabel.setText(lang.get(report.getNameKey()));

        if (report.getInput() == ReportInput.NONE) {
            showSelector(false);
            runReport(report, null);
            return;
        }

        //this report needs a conference or participant first
        String inputKey = report.getInput() == ReportInput.CONFERENCE ? "reports.select.conference" : "reports.select.participant";
        selectorLabel.setText(lang.get(inputKey));
        optionBox.setValue(null);
        try {
            optionBox.getItems().setAll(reportService.getOptions(report.getInput()));
        } catch (DataAccessException | AccessDeniedException e) {
            showSelector(false);
            feedbackLabel.setText(lang.get(e.getMessage()));
            return;
        }
        showSelector(true);
        clearTable(lang.get(inputKey));
        feedbackLabel.setText("");
    }

    //called when a conference or participant is picked in the dropdown
    @FXML
    public void runSelectedOption() {
        int index = reportList.getSelectionModel().getSelectedIndex();
        ReportOption option = optionBox.getValue();
        if (index < 0 || option == null) {
            return;
        }
        runReport(reports.get(index), option.getId());
    }

    private void runReport(ReportStrategy report, Long selectedId) {
        ReportTable table;
        try {
            table = reportService.generate(report, selectedId);
        } catch (DataAccessException | AccessDeniedException e) {
            clearTable("");
            feedbackLabel.setText(lang.get(e.getMessage()));
            return;
        }
        showTable(table);
        feedbackLabel.setText(lang.format("reports.feedback.rows", table.getRows().size()));
    }

    //builds one column per header; each row is a list of cells
    private void showTable(ReportTable table) {
        reportTable.getColumns().clear();
        for (int i = 0; i < table.getColumns().size(); i++) {
            int column = i;
            TableColumn<List<String>, String> tableColumn = new TableColumn<>(table.getColumns().get(i));
            tableColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().get(column)));
            reportTable.getColumns().add(tableColumn);
        }
        reportTable.setPlaceholder(new Label(lang.get("reports.empty")));
        reportTable.setItems(FXCollections.observableArrayList(table.getRows()));
    }

    private void clearTable(String placeholder) {
        reportTable.getColumns().clear();
        reportTable.getItems().clear();
        reportTable.setPlaceholder(new Label(placeholder));
    }

    private void showSelector(boolean show) {
        if (!show) {
            optionBox.getItems().clear();
        }
        selectorBox.setVisible(show);
        selectorBox.setManaged(show);
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

    //Observer: reload the dashboard in the new language, keeping the tab, report and selection
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
                ReportOption option = optionBox.getValue();
                if (option != null) {
                    newController.optionBox.getItems().stream()
                            .filter(o -> o.getId() == option.getId())
                            .findFirst()
                            .ifPresent(newController.optionBox::setValue);
                }
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

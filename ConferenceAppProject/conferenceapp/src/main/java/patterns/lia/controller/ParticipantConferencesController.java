package patterns.lia.controller;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import patterns.lia.dao.RegistrationDAO;
import patterns.lia.db.DatabaseManager;
import patterns.lia.model.Conference;
import patterns.lia.model.User;
import patterns.lia.model.enums.Role;
import patterns.lia.utilities.UserSession;

public class ParticipantConferencesController{
    private RegistrationDAO registrationDao=new RegistrationDAO(DatabaseManager.getDefault());
    @FXML 
    public TableView<Conference> conferencesTable;
    @FXML 
    public Label feedbackLabel;
    @FXML 
    private void initialize(){
        System.out.println(DatabaseManager.getDefault().getUrl());
        User user = UserSession.requireRole(Role.PARTICIPANT);
        List<Conference> conferences=registrationDao.getConferencesNotRegistered(user);
        if(conferences!=null){
            ObservableList<Conference> tableItems=FXCollections.observableArrayList(conferences);
            conferencesTable.setItems(tableItems);
            TableColumn<Conference,Integer> idColumn=new TableColumn<>("ID");
            idColumn.setMinWidth(50);
            idColumn.setCellValueFactory(new PropertyValueFactory<>("conferenceId"));
            TableColumn<Conference,String> titleColumn=new TableColumn<>("Title");
            titleColumn.setMinWidth(200);
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            conferencesTable.getColumns().addAll(idColumn,titleColumn);
        }
        else feedbackLabel.setText("Error retrieving items from database");
    }
}

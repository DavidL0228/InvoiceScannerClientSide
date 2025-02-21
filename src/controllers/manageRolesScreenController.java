package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;

public class manageRolesScreenController {

    @FXML
    private Button backButton;

    @FXML
    private Button createNewRoleButton;

    @FXML
    private Button editRoleButton;

    @FXML
    private TableColumn<Role, ArrayList<String>> permissionsColumn;

    @FXML
    private TableColumn<Role, String> roleColumn;

    @FXML
    private TableView<Role> roleTableView;

    private ObservableList<Role> rolesAndPermissions = FXCollections.observableArrayList();

    @FXML
    void backButtonClicked(ActionEvent event) {

        try {
            // Load the home screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch(Exception e){
            System.out.println("Error loading page");
        }
    }

    @FXML
    void createNewRoleButtonClicked(ActionEvent event) {

        // indicates no role selected, so new role will be created
        Role.setSelectedRole(null);

        // open editRoleScreen on top of current screen
        // while temporarily freezing current screen
        showEditRoleScreen(event);
    }

    @FXML
    void editRoleButtonClicked(ActionEvent event) {

        // open editRoleScreen on top of current screen
        // while temporarily freezing current screen
        showEditRoleScreen(event);
    }

    private void showEditRoleScreen(ActionEvent event){

        // open editRoleScreen on top of current screen
        // while temporarily freezing current screen
        try{
            Stage secondStage = new Stage();
            secondStage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/editRoleScreen.fxml")))));
            secondStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            secondStage.initModality(Modality.WINDOW_MODAL);    // current window will freeze while second one is still showing
            secondStage.setResizable(false);
            secondStage.showAndWait();

        } catch(Exception e){
            System.out.println("Error loading editRoleScreen");
        }

        updateRoleTable();
        editRoleButton.setVisible(false);
    }

    public void initialize() {

        // initialize columns in roleTableView
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        permissionsColumn.setCellValueFactory(new PropertyValueFactory<>("permissions"));

        updateRoleTable();

        // create listener for when role is selected in roleTableView
        roleTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    Role.setSelectedRole(newValue);
                    editRoleButton.setVisible(true);
                }
        );
    }

    private void updateRoleTable(){

        rolesAndPermissions.clear();

        // not sure yet how we will get the list of roles with their permissions
        // but those records will be added to the tableview here.



        // for testing purposes only: *****************************************

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add("Invoice - Read");
        permissions.add("Invoice - Upload");
        permissions.add("Invoice - Approve");
        permissions.add("Payment - Read");
        permissions.add("Payment - Create");
        permissions.add("Payment - Approve");

        // creates one Role item, adds to observable list: rolesAndPermissions,
        rolesAndPermissions.add(new Role("Manager-Test Role", permissions));

        // roleTableView made up of observable list: rolesAndPermissions
        roleTableView.setItems(rolesAndPermissions);

        // ********************************************************************
    }
}

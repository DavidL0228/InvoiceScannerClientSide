package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;

public class editRoleScreenController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    @FXML
    private CheckBox invoiceApproveCheckBox;

    @FXML
    private CheckBox invoiceReadCheckBox;

    @FXML
    private CheckBox invoiceUploadCheckBox;

    @FXML
    private CheckBox paymentApproveCheckBox;

    @FXML
    private CheckBox paymentCreateCheckBox;

    @FXML
    private CheckBox paymentReadCheckBox;

    @FXML
    private TextField roleTextField;

    @FXML
    private Button saveButton;

    @FXML
    void cancelButtonClicked(ActionEvent event) {
        // close editRoleScreen
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void deleteButtonClicked(ActionEvent event) {

        if(Role.getSelectedRole() != null) {
            // send request to server to delete the role
        }

        // then close editRoleScreen
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void saveButtonClicked(ActionEvent event) {

        Role saveThisRole = extractRoleAndPermissions();

        if(saveThisRole == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Role name required");
            alert.setHeaderText("WARNING");
            alert.showAndWait();
            return;
        }

        if(Role.getSelectedRole() != null) {
            // send request to server to save changes to the role
            // note: Role.getSelectedRole() is the old Role,
            //       saveThisRole is the updated Role
        }
        else {
            // send request to server to save NEW role
            // save saveThisRole as new role.
        }

        // then close editRoleScreen
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML private void initialize() {

        if(Role.getSelectedRole() == null)
            deleteButton.setVisible(false);
        else
            initializeRoleData();
    }

    private void initializeRoleData() {
        roleTextField.setText(Role.getSelectedRole().getRole());
        ArrayList<String> permissions = Role.getSelectedRole().getPermissions();

        // no clue how we're keeping track of permission list yet so
        // just for now there's this
        if(permissions.contains("Invoice - Read"))
            invoiceReadCheckBox.setSelected(true);
        if(permissions.contains("Invoice - Upload"))
            invoiceUploadCheckBox.setSelected(true);
        if(permissions.contains("Invoice - Approve"))
            invoiceApproveCheckBox.setSelected(true);

        if(permissions.contains("Payment - Read"))
            paymentReadCheckBox.setSelected(true);
        if(permissions.contains("Payment - Create"))
            paymentCreateCheckBox.setSelected(true);
        if(permissions.contains("Payment - Approve"))
            paymentApproveCheckBox.setSelected(true);
    }

    private Role extractRoleAndPermissions() {

        String roleName = roleTextField.getText();
        if(roleName.trim().isEmpty())
            return null;

        ArrayList<String> permissions = new ArrayList<>();

        // no clue how we're keeping track of permission list yet so
        // just for now there's this
        if(invoiceReadCheckBox.isSelected())
            permissions.add("Invoice - Read");
        if(invoiceUploadCheckBox.isSelected())
            permissions.add("Invoice - Upload");
        if(invoiceApproveCheckBox.isSelected())
            permissions.add("Invoice - Approve");

        if(paymentReadCheckBox.isSelected())
            permissions.add("Payment - Read");
        if(paymentCreateCheckBox.isSelected())
            permissions.add("Payment - Create");
        if(paymentApproveCheckBox.isSelected())
            permissions.add("Payment - Approve");

        return new Role(roleName, permissions);
    }
}

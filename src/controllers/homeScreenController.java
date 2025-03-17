package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.Objects;

public class homeScreenController {

    @FXML
    private Button addPaymentButton;

    @FXML
    private Button approvalsButton;

    @FXML
    private Button dataButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button uploadInvoiceButton;

    @FXML
    private Button viewInvoicesButton;

    @FXML
    private Button manageRolesButton;

    @FXML
    void addPaymentButtonClicked(ActionEvent event) {

        // load payment screen
        //loadScreen(event, "/fxml/paymentScreen.fxml");
    }

    @FXML
    void approvalsButtonClicked(ActionEvent event) {

        // load approvals screen
        //loadScreen(event, "/fxml/approvalsScreen.fxml");
    }

    @FXML
    void dataButtonClicked(ActionEvent event) {

        // load data screen
        loadScreen(event, "/fxml/dataScreen.fxml");
    }

    @FXML
    void logoutButtonClicked(ActionEvent event) {

        // logout

        // load login screen
        loadScreen(event, "/fxml/loginScreen.fxml");
    }

    @FXML
    void uploadInvoiceButtonClicked(ActionEvent event) {

        // load upload invoice screen
        loadScreen(event, "/fxml/uploadInvoiceScreen.fxml");
    }

    @FXML
    void viewInvoicesButtonClicked(ActionEvent event) {

        // load invoice list screen
        loadScreen(event, "/fxml/invoiceListScreen.fxml");
    }

    @FXML
    void manageRolesButtonClicked(ActionEvent event){

        // load manage roles screen
        loadScreen(event, "/fxml/manageRolesScreen.fxml");
    }

    void loadScreen(ActionEvent event, String page){
        try {
            // Load the next screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(page)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch(Exception e){
            System.out.println("Error loading page");
        }
    }
}

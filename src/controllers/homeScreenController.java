package controllers;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Objects;

public class homeScreenController {

    public Button mangeUsersButton;
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
    private Label errorLabel;


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
        try {
            // Build logout request manually
            JsonObject request = new JsonObject();
            request.addProperty("type", "LOGOUT");

            // Attach session token if available
            JsonObject data = new JsonObject();
            data.addProperty("token", client.getToken()); // move token into data
            request.add("data", data);

            // Send to server
            JsonObject response = client.sendJsonMessage(request);
            System.out.println("Logout response: " + response);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Clear session token from client
        client.clearToken();

        // Load login screen
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
    void manageUsersButtonClicked(ActionEvent event) {
        JsonObject request = new JsonObject();
        request.addProperty("type", "CHECK_ROLE");


        JsonObject data = new JsonObject();
        data.addProperty("role", "system_admin");  // The role we're checking for
        data.addProperty("token", client.getToken());
        request.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(request);
            if (response.has("authorized") && response.get("authorized").getAsBoolean()) {
                // Hide error if it was shown
                errorLabel.setVisible(false);
                loadScreen(event, "/fxml/manageUsersScreen.fxml");
            } else {
                errorLabel.setText("ERROR: you are not a system_admin");
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("ERROR: Unable to check role.");
            errorLabel.setVisible(true);
        }
    }


    void loadScreen(ActionEvent event, String page){
        try {
            // Load the next screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(page)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();


            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        }
        catch(Exception e){
            System.out.println("Error loading page");
            e.printStackTrace();
        }
    }

}

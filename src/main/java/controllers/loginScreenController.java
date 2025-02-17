package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;

public class loginScreenController extends guiController {

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    void login(ActionEvent event) throws IOException, InterruptedException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username or password field is empty.");
            return;
        }

        // Send login request to the server
        String response = theClient.sendLogin(username, password);

        // Process the response (basic parsing for now)
        JSONObject jsonResponse = new JSONObject(response);
        String status = jsonResponse.optString("status", "failure");

        if (status.equals("success")) {
            System.out.println("Login successful! Redirecting...");

            // Load the next screen (invoice list)
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } else {
            System.out.println("Login failed: " + jsonResponse.optString("message", "Unknown error"));
        }
    }

    public void initialize() {
        theClient = new client();
    }
}

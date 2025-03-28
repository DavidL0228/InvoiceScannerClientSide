package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Objects;

public class loginScreenController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Text errorText;

    @FXML
    private Hyperlink signupHyperlink;

    @FXML
    void login(ActionEvent event) throws IOException, InterruptedException {
        JsonObject jsonObject = new JsonObject();

        // Get data from input fields
        jsonObject.addProperty("type", "LOGIN");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        data.addProperty("username", usernameField.getText().trim());
        data.addProperty("password", passwordField.getText().trim());

        jsonObject.add("data", data);


        // Send JSON to server
        JsonObject jsonResponse = client.sendJsonMessage(jsonObject);

        // Extract status field
        String status = jsonResponse.has("status") ? jsonResponse.get("status").getAsString() : "failure";

        if (status.equals("success")) {
            System.out.println("Login successful! Redirecting...");

            if (jsonResponse.has("token")) {
                String token = jsonResponse.get("token").getAsString();
                client.setToken(token);  // Store it globally
                System.out.println("Session token: " + token);
            }

            // Load the next screen (home screen)
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        } else {
            String message = jsonResponse.has("message") ? jsonResponse.get("message").getAsString() : "Unknown error";
            System.out.println("Login failed: " + message);
            errorText.setVisible(true);
        }
    }

    @FXML
    void signupHyperlinkPressed(ActionEvent event) {
        try {
            // Load the next screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/signupScreen.fxml")));
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

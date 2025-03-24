package controllers;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class signupScreenController {

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private Text errorText;

    @FXML
    private Hyperlink loginHyperlink;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signupButton;

    @FXML
    private TextField usernameField;

    int charMin = 7;
    int charMax = 22;

    @FXML
    void loginHyperlinkPressed(ActionEvent event) {
        try {
            // Load the next screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/loginScreen.fxml")));
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
    void signupButtonPressed(ActionEvent event) throws IOException, InterruptedException {

        firstNameField.setStyle(null);
        lastNameField.setStyle(null);
        usernameField.setStyle(null);
        emailField.setStyle(null);
        passwordField.setStyle(null);
        confirmPasswordField.setStyle(null);

        String errors = checkFieldErrors();

        // show alert with errors if any fields contain errors
        if(!errors.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING,errors, ButtonType.OK);
            alert.setHeaderText("ERROR CREATING ACCOUNT");
            alert.showAndWait();
            return;
        }
        // otherwise send message to server to create account


        JsonObject jsonObject = new JsonObject();

        // Get data from input fields
        jsonObject.addProperty("type", "CREATE_ACCOUNT");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        data.addProperty("first_name", firstNameField.getText().trim());
        data.addProperty("last_name", lastNameField.getText().trim());
        data.addProperty("username", usernameField.getText().trim());
        data.addProperty("email", emailField.getText().trim());
        data.addProperty("password", passwordField.getText().trim());

        jsonObject.add("data", data);

        // Send JSON to server
        JsonObject jsonResponse = client.sendJsonMessage(jsonObject);

        // Extract status field
        String status = jsonResponse.has("status") ? jsonResponse.get("status").getAsString() : "failure";

        if (status.equals("success")) {
            System.out.println("Account creation successful! Redirecting...");

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
            System.out.println("Account creation failed: " + message);

            Alert alert = new Alert(Alert.AlertType.ERROR,"Please try again.", ButtonType.OK);
            alert.setHeaderText("ERROR CREATING ACCOUNT");
            alert.showAndWait();
        }

    }

    // check all fields for errors, and combine into an error message
    String checkFieldErrors(){
        String errors = "";

        if(!checkName(firstNameField))
            errors = errors + "Incorrect first name format.\n";

        if(!checkName(lastNameField))
            errors = errors + "Incorrect last name format.\n";

        if(!checkUsername())
            errors = errors + "Incorrect username format.\n";

        if(!emailFormat())
            errors = errors + "Incorrect email format.\n";

        errors = errors + checkPassword();

        return errors;
    }

    // check name: size <= 22, no spaces, no numbers
    boolean checkName(TextField t){

        String s = t.getText().trim();

        if(s.isEmpty()){
            t.setStyle("-fx-border-color: red");
            return false;
        }
        if(s.length()>charMax || s.contains(" ") || s.matches(".*\\d.*")) {
            t.setStyle("-fx-border-color: red");
            return false;
        }
        return true;
    }

    // check username: size <= 22, no spaces
    boolean checkUsername() {

        String s = usernameField.getText().trim();

        if(s.isEmpty()){
            usernameField.setStyle("-fx-border-color: red");
            return false;
        }
        if(s.length()>charMax || s.contains(" ")) {
            usernameField.setStyle("-fx-border-color: red");
            return false;
        }
        return true;
    }

    // check password: 7 <= size <= 22, no spaces, contains at least 1 number
    String checkPassword() {

        String s = passwordField.getText().trim();

        if(s.isEmpty()){
            passwordField.setStyle("-fx-border-color: red");
            confirmPasswordField.setStyle("-fx-border-color: red");
            return "Password field cannot be empty.";
        }
        if(s.length()<charMin || s.length()>charMax || s.contains(" ") || !s.matches(".*\\d.*")) {
            passwordField.setStyle("-fx-border-color: red");
            confirmPasswordField.setStyle("-fx-border-color: red");
            return "Password must be 7 characters and contain at least 1 number.";
        }

        if(s.equals(confirmPasswordField.getText().trim()))
            return "";

        // if password field does not match confirm password field
        passwordField.setStyle("-fx-border-color: red");
        confirmPasswordField.setStyle("-fx-border-color: red");
        return "Passwords do not match.";
    }

    // check email format
    boolean emailFormat() {

        String s = emailField.getText().trim();

        if (s.matches("^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"))
            return true;

        emailField.setStyle("-fx-border-color: red");
        return false;
    }
}

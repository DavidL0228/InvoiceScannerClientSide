package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class manageUsersScreenController {

    @FXML
    private Button backButton;

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private ChoiceBox<?> roleChoiceBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private TableColumn<User, String> userColumn;

    @FXML
    private TableView<User> userTableView;

    @FXML
    private TextField usernameField;

    int charMax = 22;

    private final ObservableList<User> allUsers = FXCollections.observableArrayList();

    private final ObservableList<User> roles = FXCollections.observableArrayList();

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
    void saveButtonClicked(ActionEvent event) {

        if(userTableView.getSelectionModel().isEmpty())
            return;

        String errors = checkFieldErrors();

        // show alert with errors if any fields contain errors
        if(!errors.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING,errors, ButtonType.OK);
            alert.setHeaderText("ERROR SAVING USER INFO");
            alert.showAndWait();
            return;
        }
        // otherwise send message to server to update account


    }

    @FXML
    void deleteUserButtonClicked(ActionEvent event) throws Exception {

        if(userTableView.getSelectionModel().isEmpty())
            return;

        String selected = userTableView.getSelectionModel().selectedItemProperty().getValue().toString();

        Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.CANCEL, ButtonType.YES);
        alert.setHeaderText("Delete user '" + selected + "' ?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.CANCEL) //CANCEL selected
            return;


        JsonObject requestJson = new JsonObject();

        // Get data from input fields
        requestJson.addProperty("type", "ADMIN_DELETE_ACCOUNT");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        data.addProperty("username", userTableView.getSelectionModel().selectedItemProperty().getValue().getUsername());
        requestJson.add("data", data);

        // Send JSON to server
        String status = client.sendJsonMessage(requestJson).toString();

        if(status.equals("success")){
            firstNameField.clear();
            lastNameField.clear();
            usernameField.clear();
            emailField.clear();
            userTableView.getSelectionModel().clearSelection();

            setTable();

            return;
        }

        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("ERROR DELETING USER");
        errorAlert.showAndWait();

    }

    public void initialize()throws IOException, InterruptedException {
        setTable();
        getRoles();

        userTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(newValue != null) {
                        firstNameField.setText(newValue.getFirstname());
                        lastNameField.setText(newValue.getLastname());
                        usernameField.setText(newValue.getUsername());
                        emailField.setText(newValue.getEmail());
                    }
                }
        );
    }

    void setTable() throws IOException, InterruptedException {

        allUsers.clear();

        JsonObject requestJson = new JsonObject();

        // Get data from input fields
        requestJson.addProperty("type", "GET_ALL_USERS");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        requestJson.add("data", data);

        // Send JSON to server
        JsonObject jsonResponse = client.sendJsonMessage(requestJson);

        try{
            jsonResponse.get("error");

        } catch (Exception e) {
            JsonArray jsonArray = jsonResponse.getAsJsonArray("users");

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonUser = jsonArray.get(i).getAsJsonObject();

                User user = new User(
                        jsonUser.get("first_name").getAsString(),
                        jsonUser.get("last_name").getAsString(),
                        jsonUser.get("username").getAsString(),
                        jsonUser.get("email").getAsString()
                );
                allUsers.add(user);
            }
            userTableView.setItems(allUsers);
        }

        System.out.println("No users in table.");
    }

    void getRoles(){

        roles.clear();

        // get list from server of all roles from server to populate roleChoiceBox
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
            errors = errors + "Incorrect email format.";

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

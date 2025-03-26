package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class manageUsersScreenController {

    @FXML private Button backButton;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private Button saveButton;
    @FXML private Button deleteUserButton;
    @FXML private TableColumn<User, String> userColumn;
    @FXML private TableView<User> userTableView;
    @FXML private ListView<String> roleListView;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private Button addRoleButton;

    private final ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final ObservableList<String> availableRoles = FXCollections.observableArrayList();

    private int charMax = 22;

    @FXML
    public void initialize() throws IOException, InterruptedException {
        roleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setTable();
        getAvailableRoles();
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        //addRoleButton.setOnAction(e -> addRoleButtonClicked());

        userTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        firstNameField.setText(newVal.getFirstname());
                        lastNameField.setText(newVal.getLastname());
                        usernameField.setText(newVal.getUsername());
                        emailField.setText(newVal.getEmail());

                        // Update current roles in ListView
                        roleListView.getSelectionModel().clearSelection();
                        roleListView.setItems(FXCollections.observableArrayList(newVal.getRoles()));

                        roleListView.getItems().setAll(newVal.getRoles());
                        updateRoleChoiceBox(newVal);

                        // Compute roles user does NOT have and update ChoiceBox
                        List<String> userRoles = newVal.getRoles();
                        List<String> rolesNotAssigned = new ArrayList<>();
                        for (String role : availableRoles) {
                            if (!userRoles.contains(role)) {
                                rolesNotAssigned.add(role);
                            }
                        }
                        roleChoiceBox.setItems(FXCollections.observableArrayList(rolesNotAssigned));
                        roleChoiceBox.getSelectionModel().clearSelection();
                    }
                }
        );
        setTable();

    }

    private void updateRoleChoiceBox(User user) {
        Set<String> assigned = new HashSet<>(user.getRoles());
        List<String> unassignedRoles = availableRoles.filtered(role -> !assigned.contains(role));
        roleChoiceBox.setItems(FXCollections.observableArrayList(unassignedRoles));
        roleChoiceBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void addNewRole() {
        if (userTableView.getSelectionModel().isEmpty()) {
            System.out.println("No user selected.");
            return;
        }

        String selectedRole = roleChoiceBox.getValue();
        if (selectedRole == null || selectedRole.isEmpty()) {
            System.out.println("No role selected.");
            return;
        }

        User user = userTableView.getSelectionModel().getSelectedItem();

        JsonObject request = new JsonObject();
        request.addProperty("type", "ADD_ROLE_TO_USER");

        JsonObject data = new JsonObject();
        data.addProperty("username", user.getUsername());
        data.addProperty("role", selectedRole);

        request.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(request);
            System.out.println("Add Role Response: " + response);

            if (response.has("status") && response.get("status").getAsString().equalsIgnoreCase("success")) {
                // update UI
                user.getRoles().add(selectedRole);
                roleListView.getItems().add(selectedRole);
                updateRoleChoiceBox(user);  // re-filter available roles
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to add role.").showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    void setTable() throws IOException, InterruptedException {
        allUsers.clear();

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "GET_ALL_USERS");
        requestJson.add("data", new JsonObject());

        JsonObject response = client.sendJsonMessage(requestJson);

        if (!response.has("error")) {
            JsonArray jsonArray = response.getAsJsonArray("users");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject userJson = jsonArray.get(i).getAsJsonObject();
                JsonArray rolesArray = userJson.has("roles") && userJson.get("roles").isJsonArray()
                        ? userJson.getAsJsonArray("roles")
                        : new JsonArray(); // fallback to empty list
                List<String> roleList = new ArrayList<>();
                for (int j = 0; j < rolesArray.size(); j++) {
                    roleList.add(rolesArray.get(j).getAsString());
                }

                User user = new User(
                        userJson.get("first_name").getAsString(),
                        userJson.get("last_name").getAsString(),
                        userJson.get("username").getAsString(),
                        userJson.get("email").getAsString(),
                        roleList
                );
                allUsers.add(user);
            }
            userTableView.setItems(allUsers);
        }
    }

    void getAvailableRoles() throws IOException, InterruptedException {
        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_AVAILABLE_ROLES");
        request.add("data", new JsonObject());

        JsonObject response = client.sendJsonMessage(request);
        if (!response.has("error")) {
            JsonArray rolesArray = response.getAsJsonArray("roles");
            for (int i = 0; i < rolesArray.size(); i++) {
                availableRoles.add(rolesArray.get(i).getAsString());
            }

        }
    }

    @FXML
    void saveButtonClicked(ActionEvent event) throws IOException, InterruptedException {
        if (userTableView.getSelectionModel().isEmpty()) return;

        String errors = checkFieldErrors();
        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, errors, ButtonType.OK);
            alert.setHeaderText("ERROR SAVING USER INFO");
            alert.showAndWait();
            return;
        }

        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        List<String> selectedRoles = roleListView.getSelectionModel().getSelectedItems();

        JsonObject request = new JsonObject();
        request.addProperty("type", "UPDATE_USER_ROLES");

        JsonObject data = new JsonObject();
        data.addProperty("username", selectedUser.getUsername());

        JsonArray rolesArray = new JsonArray();
        for (String role : selectedRoles) rolesArray.add(role);
        data.add("roles", rolesArray);

        request.add("data", data);

        JsonObject response = client.sendJsonMessage(request);
        if (response.has("status") && response.get("status").getAsString().equals("success")) {
            System.out.println("User roles updated successfully.");
            setTable();
        } else {
            System.out.println("Failed to update user roles.");
        }
    }

    @FXML
    void deleteUserButtonClicked(ActionEvent event) throws Exception {
        if (userTableView.getSelectionModel().isEmpty()) return;

        User selectedUser = userTableView.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete user '" + selectedUser.getUsername() + "'?", ButtonType.YES, ButtonType.CANCEL);
        alert.setHeaderText("Confirm User Deletion");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            JsonObject request = new JsonObject();
            request.addProperty("type", "ADMIN_DELETE_ACCOUNT");

            JsonObject data = new JsonObject();
            data.addProperty("username", selectedUser.getUsername());
            request.add("data", data);

            JsonObject response = client.sendJsonMessage(request);
            if (response.has("status") && response.get("status").getAsString().equals("success")) {
                setTable();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error deleting user.").showAndWait();
            }
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        roleListView.getSelectionModel().clearSelection();
    }

    String checkFieldErrors() {
        StringBuilder errors = new StringBuilder();
        if (!checkName(firstNameField)) errors.append("Incorrect first name format.\n");
        if (!checkName(lastNameField)) errors.append("Incorrect last name format.\n");
        if (!checkUsername()) errors.append("Incorrect username format.\n");
        if (!emailFormat()) errors.append("Incorrect email format.");
        return errors.toString();
    }

    boolean checkName(TextField t) {
        String s = t.getText().trim();
        if (s.isEmpty() || s.length() > charMax || s.contains(" ") || s.matches(".*\\d.*")) {
            t.setStyle("-fx-border-color: red");
            return false;
        }
        return true;
    }

    boolean checkUsername() {
        String s = usernameField.getText().trim();
        if (s.isEmpty() || s.length() > charMax || s.contains(" ")) {
            usernameField.setStyle("-fx-border-color: red");
            return false;
        }
        return true;
    }

    boolean emailFormat() {
        String s = emailField.getText().trim();
        if (s.matches("^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"))
            return true;
        emailField.setStyle("-fx-border-color: red");
        return false;
    }

    @FXML
    void backButtonClicked(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();


            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

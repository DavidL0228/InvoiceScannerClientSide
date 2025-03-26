package controllers;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class verificationScreenController {

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField DueBox;

    @FXML
    private TextField IssueDateBox;

    @FXML
    private TextField SubTotalBox;

    @FXML
    private TextField invoiceNumBox;

    @FXML
    private TextField taxBox;

    @FXML
    private TextField totalBox;

    @FXML
    private Button uploadDataButton;


    @FXML
    private ComboBox<String> vendorDropdown;

    @FXML
    private TextField vendorEmailBox;

    @FXML
    private TextField vendorNameBox;

    @FXML
    private TextField vendorAddressBox;

    @FXML
    private TextField vendorGLBox;

    @FXML
    private StackPane imageStackPane;

    private String storedFileName;
    private String storedVendorName;
    private String storedEmail;
    private String storedAddress;
    private String storedGL;

    private List<vendor> loadedVendors = new ArrayList<>();


    @FXML
    public void initialize() {
        // Bind the ImageView dimensions to dynamically respond to the container size
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty().subtract(20));
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty().subtract(20));

        loadVendorsFromServer();

        vendorDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            if (newVal.equals("Add New Vendor")) {
                vendorNameBox.setText(storedVendorName != null ? storedVendorName : "");
                vendorEmailBox.setText(storedEmail != null ? storedEmail : "");
                vendorAddressBox.setText(storedAddress != null ? storedAddress : "");
                vendorGLBox.setText(storedGL != null ? storedGL : "");
            } else {
                for (vendor tempVendor : loadedVendors) {
                    if (tempVendor.getName().equalsIgnoreCase(newVal)) {
                        vendorNameBox.setText(tempVendor.getName());
                        vendorEmailBox.setText(tempVendor.getEmail() != null ? tempVendor.getEmail() : "");
                        vendorAddressBox.setText(tempVendor.getAddress() != null ? tempVendor.getAddress() : "");
                        vendorGLBox.setText(tempVendor.getDefaultGL() != null ? tempVendor.getDefaultGL() : "");
                        break;
                    }
                }
            }
        });



        System.out.println("invoiceNumBox: " + invoiceNumBox);
        System.out.println("taxBox: " + taxBox);
        System.out.println("totalBox: " + totalBox);
        System.out.println("vendorNameBox: " + vendorNameBox);
        System.out.println("vendorAddressBox: " + vendorAddressBox);
        System.out.println("vendorGLBox: " + vendorGLBox);
        System.out.println("vendorEmailBox: " + vendorEmailBox);
        System.out.println("IssueDateBox: " + IssueDateBox);
        System.out.println("DueBox: " + DueBox);
        System.out.println("SubTotalBox: " + SubTotalBox);
    }


    private void attemptAutoSelectVendor() {
        if (storedVendorName == null) {
            vendorDropdown.getSelectionModel().select("Add New Vendor");
            return;
        }

        // Try to find the matching vendor by name (case-insensitive match recommended)
        for (vendor tempVendor : loadedVendors) {
            if (tempVendor.getName().trim().equalsIgnoreCase(storedVendorName.trim())) {
                System.out.println("Auto-selected vendor from OCR: " + storedVendorName);
                vendorDropdown.getSelectionModel().select(tempVendor.getName());

                vendorNameBox.setText(tempVendor.getName());
                vendorEmailBox.setText(tempVendor.getEmail() != null ? tempVendor.getEmail() : "");
                vendorAddressBox.setText(tempVendor.getAddress() != null ? tempVendor.getAddress() : "");
                vendorGLBox.setText(tempVendor.getDefaultGL() != null ? tempVendor.getDefaultGL() : "");
                return;
            }
        }


        // If no match found
        System.out.println("Vendor not found in list, selecting 'Add New Vendor'");
        vendorDropdown.getSelectionModel().select("Add New Vendor");

        if (storedVendorName != null) vendorNameBox.setText(storedVendorName);
        if (storedEmail != null) vendorEmailBox.setText(storedEmail);
        if (storedAddress != null) vendorAddressBox.setText(storedAddress);
        if (storedGL != null) vendorGLBox.setText(storedGL);
    }

    private void loadVendorsFromServer() {
        try {
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("type", "GET_ALL_VENDORS");

            JsonObject response = client.sendJsonMessage(requestJson);

            if (response.has("vendors")) {
                vendorDropdown.getItems().clear();
                loadedVendors.clear();

                for (var element : response.getAsJsonArray("vendors")) {
                    JsonObject vendorJson = element.getAsJsonObject();

                    int id = vendorJson.get("vendor_id").getAsInt();
                    String name = vendorJson.get("name").getAsString();
                    String email = vendorJson.has("email") && !vendorJson.get("email").isJsonNull() ? vendorJson.get("email").getAsString() : "";
                    String address = vendorJson.has("address") && !vendorJson.get("address").isJsonNull() ? vendorJson.get("address").getAsString() : "";
                    String gl = vendorJson.has("gl") && !vendorJson.get("gl").isJsonNull() ? vendorJson.get("gl").getAsString() : "";

                    vendor tempVendor = new vendor(id, name, email, address, gl);
                    loadedVendors.add(tempVendor);
                    vendorDropdown.getItems().add(String.valueOf(tempVendor));
                }

                // Add "Add New Vendor" option
                vendorDropdown.getItems().add(String.valueOf(new vendor(-1, "Add New Vendor", "", "", "")));
                attemptAutoSelectVendor();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading vendors from server.");
            printError("Error loading vendors from server.");
        }
    }


    @FXML
    void uploadData(ActionEvent event) {

        if(invoiceNumBox.getText().isEmpty() || taxBox.getText().isEmpty()
                || totalBox.getText().isEmpty() || vendorNameBox.getText().isEmpty()
                || vendorGLBox.getText().isEmpty()
                || IssueDateBox.getText().isEmpty()
                || DueBox.getText().isEmpty() || SubTotalBox.getText().isEmpty()) {
            System.out.print("Missing Required Field, Please fill out all required fields.");
            printError("Missing Required Field, Please fill out all required fields.");
            return;
        }

        // Basic field validation
        String invoiceNum = invoiceNumBox.getText().trim();
        String vendorName = vendorNameBox.getText().trim();
        String vendorGL = vendorGLBox.getText().trim();
        String issueDate = IssueDateBox.getText().trim();
        String dueDate = DueBox.getText().trim();
        String subTotalStr = SubTotalBox.getText().trim();
        String taxStr = taxBox.getText().trim();
        String totalStr = totalBox.getText().trim();

        String vendorEmail = vendorEmailBox.getText().trim();
        String vendorAddress = vendorAddressBox.getText().trim();

        // Check required fields
        if (invoiceNum.isEmpty() || vendorName.isEmpty() || vendorGL.isEmpty() ||
                issueDate.isEmpty() || dueDate.isEmpty() || subTotalStr.isEmpty() ||
                taxStr.isEmpty() || totalStr.isEmpty()) {
            System.out.print("Missing Required Field, Please fill out all required fields.");
            printError("Missing Required Field, Please fill out all required fields.");
            return;
        }

        // Validate numeric values
        double subTotal, tax, total;
        try {
            subTotal = Double.parseDouble(subTotalStr);
            tax = Double.parseDouble(taxStr);
            total = Double.parseDouble(totalStr);
        } catch (NumberFormatException e) {
            System.out.print("Invalid Number, Subtotal, tax, and total must be valid numbers.");
            printError("Invalid Number, Subtotal, tax, and total must be valid numbers.");
            return;
        }

        // Validate date formats (yyyy-MM-dd)
        //if (!isValidDate(issueDate) || !isValidDate(dueDate)) {
        //    showAlert("Invalid Date", "Dates must be in the format yyyy-MM-dd.");
        //    return;
        //}


        JsonObject invoiceData = new JsonObject();

        // Vendor Name
        String selectedVendor = vendorDropdown.getSelectionModel().getSelectedItem();
        // Prepare invoice JSON

        invoiceData.addProperty("invoiceNum", invoiceNum);
        invoiceData.addProperty("vendor", vendorName);
        invoiceData.addProperty("email", vendorEmail.isEmpty() ? null : vendorEmail);
        invoiceData.addProperty("GL", vendorGL);
        invoiceData.addProperty("issueDate", issueDate);
        invoiceData.addProperty("due", dueDate);
        invoiceData.addProperty("subTotal", subTotal);
        invoiceData.addProperty("tax", tax);
        invoiceData.addProperty("total", total);
        invoiceData.addProperty("datePaid", "NULL");
        invoiceData.addProperty("status", "awaiting approval");
        invoiceData.addProperty("description", "NULL");
        invoiceData.addProperty("tempFilename", storedFileName);

        try {
            // 1. Handle vendor logic
            JsonObject vendorData = new JsonObject();
            vendorData.addProperty("name", vendorName);
            vendorData.addProperty("gl", vendorGLBox.getText());
            vendorData.addProperty("payment", "N/A");
            vendorData.addProperty("address", vendorAddressBox.getText());
            vendorData.addProperty("email", vendorEmailBox.getText());

            JsonObject vendorRequest = new JsonObject();

            if ("Add New Vendor".equals(selectedVendor)) {
                vendorRequest.addProperty("type", "ADD_VENDOR");
            } else {
                vendorRequest.addProperty("type", "UPDATE_VENDOR");
            }

            vendorRequest.add("data", vendorData);
            JsonObject vendorResponse = client.sendJsonMessage(vendorRequest);

            // Make sure we got a vendor_id back
            if (!vendorResponse.has("vendor_id") || vendorResponse.get("vendor_id").isJsonNull()) {
                System.out.println("Vendor error: Vendor ID not returned from server.");
                return;
            }
            int vendorId = vendorResponse.get("vendor_id").getAsInt();

            // 2. Upload invoice
            invoiceData.addProperty("vendor_id", vendorId);
            JsonObject invoiceRequest = new JsonObject();
            invoiceRequest.addProperty("type", "ADD_INVOICE");
            invoiceRequest.add("data", invoiceData);

            JsonObject invoiceResponse = client.sendJsonMessage(invoiceRequest);

            if (invoiceResponse.get("status").getAsString().equals("success")) {
                System.out.println("Invoice added successfully.");
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml")));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
                stage.setScene(scene);
                stage.show();
            } else {
                System.out.println("Invoice error: " + invoiceResponse.get("message").getAsString());
                printError("Invoice error: " + invoiceResponse.get("message").getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setData(JsonObject jsonObject, String filePath) {

        File file = new File(filePath);
        String fileName = file.getName();

        storedFileName = fileName;


        try {
            if (file.exists()) {
                String fileUrl = file.toURI().toString();
                System.out.println("File URL: " + fileUrl); // Debugging

                Image newImage = new Image(fileUrl);
                imageView.setImage(null); // Force refresh
                imageView.setImage(newImage);
                System.out.println("Image successfully loaded!");
            } else {
                System.out.println("Error: Image file does not exist!");
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }

        if (jsonObject.has("due")){
            DueBox.setText(jsonObject.get("due").getAsString());
        }
        if (jsonObject.has("date")){
            IssueDateBox.setText(jsonObject.get("date").getAsString());
        }
        if (jsonObject.has("subTotal")){
            SubTotalBox.setText(jsonObject.get("subTotal").getAsString());
        }
        if (jsonObject.has("invoice_num")){
            invoiceNumBox.setText(jsonObject.get("invoice_num").getAsString());
        }
        if (jsonObject.has("tax")){
            taxBox.setText(jsonObject.get("tax").getAsString());
        }
        if (jsonObject.has("total")){
            totalBox.setText(jsonObject.get("total").getAsString());
        }
        if (jsonObject.has("vendor")){
            //vendorNameBox.setText(jsonObject.get("vendor").getAsString());
            storedVendorName = jsonObject.get("vendor").getAsString();
            vendorNameBox.setText(storedVendorName);
        }
        if (jsonObject.has("email")){
            storedEmail = jsonObject.get("email").getAsString();
            vendorEmailBox.setText(storedEmail);
        }
        if (jsonObject.has("GL")){
            storedGL = jsonObject.get("GL").getAsString();
            vendorGLBox.setText(storedGL);
        }
        attemptAutoSelectVendor();
    }


    private void printError(String error){
        errorLabel.setVisible(true);
        errorLabel.setText(error);
    }

    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();


        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }
}
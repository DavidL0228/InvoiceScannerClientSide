package controllers;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class verificationScreenController {

    @FXML
    private ImageView imageView;

    @FXML
    private TextField DueBox;

    @FXML
    private TextField GLBox;

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
    private TextField vendorBox;

    @FXML
    private TextField emailBox;

    @FXML private StackPane imageStackPane;

    private String storedFileName;


    @FXML
    public void initialize() {
        // Bind the ImageView dimensions to dynamically respond to the container size
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty().subtract(20));
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty().subtract(20));
    }

    @FXML
    void uploadData(ActionEvent event) {
        JsonObject invoiceData = new JsonObject();

        // Retrieve and format the data from text fields
        invoiceData.addProperty("invoiceNum", invoiceNumBox.getText());
        invoiceData.addProperty("vendor", vendorBox.getText());
        invoiceData.addProperty("email", emailBox.getText());
        invoiceData.addProperty("GL", GLBox.getText());
        invoiceData.addProperty("issueDate", IssueDateBox.getText());
        invoiceData.addProperty("due", DueBox.getText());
        invoiceData.addProperty("subTotal", SubTotalBox.getText().isEmpty() ? "NULL" : SubTotalBox.getText());
        invoiceData.addProperty("tax", taxBox.getText().isEmpty() ? "NULL" : taxBox.getText());
        invoiceData.addProperty("total", totalBox.getText());
        invoiceData.addProperty("datePaid", "NULL"); // Initially NULL
        invoiceData.addProperty("status", "awaiting approval");
        invoiceData.addProperty("description", "NULL"); // Default description

        // Add tempFilename
        invoiceData.addProperty("tempFilename", storedFileName);

        try {
            // Send JSON to server (type ADD_INVOICE)
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("type", "ADD_INVOICE");
            requestJson.add("data", invoiceData);

            JsonObject response = client.sendJsonMessage(requestJson);

            if (response.get("status").getAsString().equals("success")) {
                System.out.println("Invoice added successfully.");

                // Redirect to invoice list screen
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml")));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                System.out.println("Error: " + response.get("message").getAsString());
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
        if (jsonObject.has("GL")){
            GLBox.setText(jsonObject.get("GL").getAsString());
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
            vendorBox.setText(jsonObject.get("vendor").getAsString());
        }
        if (jsonObject.has("email")){
            emailBox.setText(jsonObject.get("email").getAsString());
        }

    }
}

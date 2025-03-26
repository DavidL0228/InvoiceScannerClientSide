package controllers;

import com.google.gson.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class editScreenController {

    @FXML private Button backButton;
    @FXML private TextField invoiceNumBox, emailBox, GLBox, IssueDateBox, DueBox, SubTotalBox, taxBox, totalBox;
    @FXML private TextField vendorNameBox, vendorEmailBox, vendorAddressBox, vendorGLBox;
    @FXML private TextField internalIdBox;
    @FXML private ImageView imageView;
    @FXML private StackPane imageStackPane;
    @FXML private Button uploadDataButton;
    @FXML private Label errorLabel;

    private Invoice currentInvoice;

    @FXML
    public void initialize() {
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty().subtract(20));
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty().subtract(20));
        errorLabel.setVisible(false);
    }

    public void loadInvoiceData(Invoice invoice) {
        this.currentInvoice = invoice;

        invoiceNumBox.setText(invoice.getInvoiceNumber());
        IssueDateBox.setText(invoice.getIssueDate());
        DueBox.setText(invoice.getDueDate());
        SubTotalBox.setText(String.valueOf(invoice.getSubtotal()));
        taxBox.setText(String.valueOf(invoice.getTax()));
        totalBox.setText(String.valueOf(invoice.getTotalAmount()));

        loadInvoiceImage(invoice.getInternalId());
        fetchVendorInfo(invoice.getCompany());
    }

    private void fetchVendorInfo(String vendorNameToMatch) {
        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_ALL_VENDORS");
        request.add("data", new JsonObject());

        try {
            JsonObject response = client.sendJsonMessage(request);
            if (response.has("vendors")) {
                JsonArray vendorsArray = response.getAsJsonArray("vendors");

                for (JsonElement element : vendorsArray) {
                    JsonObject vendor = element.getAsJsonObject();
                    String name = vendor.get("name").getAsString();

                    if (name.equalsIgnoreCase(vendorNameToMatch)) {
                        vendorNameBox.setText(name);
                        vendorEmailBox.setText(vendor.get("email").getAsString());
                        vendorAddressBox.setText(vendor.get("address").getAsString());
                        vendorGLBox.setText(vendor.get("gl").getAsString());
                        return;
                    }
                }

                // If no vendor found
                vendorNameBox.setText(vendorNameToMatch);
                vendorEmailBox.setText("Unknown");
                vendorAddressBox.setText("Unknown");
                vendorGLBox.setText("Unknown");
            }
        } catch (IOException | InterruptedException e) {
            errorLabel.setText("Failed to load vendor data.");
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    private void loadInvoiceImage(String invoiceId) {
        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_INVOICE_IMAGE");

        JsonObject data = new JsonObject();
        data.addProperty("invoiceId", invoiceId);
        request.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(request);
            if ("success".equals(response.get("status").getAsString())) {
                String base64Image = response.get("imageData").getAsString();
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imageView.setImage(image);
            } else {
                errorLabel.setText("Failed to load invoice image.");
                errorLabel.setVisible(true);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading image.");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    void uploadData(ActionEvent event) {
        JsonObject invoiceData = new JsonObject();
        invoiceData.addProperty("invoice_number", invoiceNumBox.getText());
        invoiceData.addProperty("issue_date", IssueDateBox.getText());
        invoiceData.addProperty("due_date", DueBox.getText());
        invoiceData.addProperty("subtotal", SubTotalBox.getText());
        invoiceData.addProperty("tax", taxBox.getText());
        invoiceData.addProperty("total", totalBox.getText());
        invoiceData.addProperty("gl_account", GLBox.getText());
        invoiceData.addProperty("vendor_name", vendorNameBox.getText());

        JsonObject request = new JsonObject();
        request.addProperty("type", "UPDATE_INVOICE");
        request.add("data", invoiceData);

        try {
            JsonObject response = client.sendJsonMessage(request);
            if ("success".equals(response.get("status").getAsString())) {
                errorLabel.setVisible(false);
                goBack(event);
            } else {
                errorLabel.setText("Failed to update invoice: " + response.get("message").getAsString());
                errorLabel.setVisible(true);
            }
        } catch (IOException | InterruptedException e) {
            errorLabel.setText("Communication error during update.");
            errorLabel.setVisible(true);
        }
    }

    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        stage.show();
    }
}

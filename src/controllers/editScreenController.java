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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

public class editScreenController {


    @FXML
    private ImageView imageView;

    @FXML
    private TextField internalIdBox;

    @FXML
    private TextField invoiceNumBox;

    @FXML
    private TextField vendorBox;

    @FXML
    private TextField emailBox;

    @FXML
    private TextField GLBox;

    @FXML
    private TextField IssueDateBox;

    @FXML
    private TextField DueBox;

    @FXML
    private TextField SubTotalBox;

    @FXML
    private TextField taxBox;

    @FXML
    private TextField totalBox;

    @FXML
    Button submitEditButton;

    @FXML
    private StackPane imageStackPane;

    private Invoice currentInvoice;

    @FXML
    public void initialize() {
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty().subtract(20));
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty().subtract(20));
    }

    // This method fetches data from server/database instead of OCR
    public void loadInvoiceData(Invoice invoice) {
        this.currentInvoice = invoice;

        internalIdBox.setText(invoice.getInternalId());
        invoiceNumBox.setText(invoice.getInvoiceNumber());
        vendorBox.setText(invoice.getCompany());
        emailBox.setText(invoice.getEmail());
        GLBox.setText(invoice.getGlAccount());
        IssueDateBox.setText(invoice.getIssueDate());
        DueBox.setText(invoice.getDueDate());
        SubTotalBox.setText(String.valueOf(invoice.getSubtotal()));
        taxBox.setText(String.valueOf(invoice.getTax()));
        totalBox.setText(String.valueOf(invoice.getTotalAmount()));

        // Load image from server using internal id
        loadInvoiceImage(invoice.getInternalId());
    }

    private void loadInvoiceImage(String invoiceId) {
        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_INVOICE_IMAGE");

        JsonObject data = new JsonObject();
        data.addProperty("invoiceId", invoiceId);
        request.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(request);

            if (response.has("status") && response.get("status").getAsString().equals("success")) {
                String base64Image = response.get("imageData").getAsString();
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imageView.setImage(image);
            } else {
                System.out.println("Failed to load image: " + response.get("message").getAsString());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void uploadData(ActionEvent event) throws IOException {
        /*
        JsonObject invoiceData = new JsonObject();

        invoiceData.addProperty("invoiceId", currentInvoice.getInternalId());
        invoiceData.addProperty("invoiceNum", invoiceNumBox.getText());
        invoiceData.addProperty("vendor", vendorBox.getText());
        invoiceData.addProperty("email", emailBox.getText());
        invoiceData.addProperty("GL", GLBox.getText());
        invoiceData.addProperty("issueDate", IssueDateBox.getText());
        invoiceData.addProperty("due", DueBox.getText());
        invoiceData.addProperty("subTotal", SubTotalBox.getText());
        invoiceData.addProperty("tax", taxBox.getText());
        invoiceData.addProperty("total", totalBox.getText());

        JsonObject request = new JsonObject();
        request.addProperty("type", "UPDATE_INVOICE");
        request.add("data", invoiceData);

        try {
            JsonObject response = client.sendJsonMessage(request);

            if (response.get("status").getAsString().equals("success")) {
                System.out.println("Invoice updated successfully.");
                goBackToInvoiceList(invoiceNumBox.getScene());
            } else {
                System.out.println("Failed to update invoice: " + response.get("message").getAsString());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

         */
        goBackToInvoiceList(invoiceNumBox.getScene());
    }

    private void goBackToInvoiceList(Scene scene) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml")));
        scene.setRoot(root);
    }

}

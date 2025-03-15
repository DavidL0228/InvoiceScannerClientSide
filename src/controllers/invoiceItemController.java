package controllers;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class invoiceItemController {

    @FXML
    private Label invoiceNumberLabel;

    @FXML
    private Label issueDateLabel;

    @FXML
    private Label dueDateLabel;

    @FXML
    private Label senderLabel;

    @FXML
    private Label amountLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label paymentDateLabel;

    @FXML
    private Button payApproveButton, viewEditButton;

    private Invoice currentInvoice;

    public void setInvoiceData(Invoice invoice) {
        this.currentInvoice = invoice;


        invoiceNumberLabel.setText("Invoice #: " + invoice.getInvoiceId());
        issueDateLabel.setText("Issue Date: " + invoice.getIssueDate());
        dueDateLabel.setText("Due Date: " + invoice.getDueDate());
        senderLabel.setText("Sender: " + invoice.getSender());
        amountLabel.setText("Total: $" + invoice.getTotalAmount());
        statusLabel.setText(invoice.getStatus());

        // If status is "Paid", show payment date and disable the button
        if ("Paid".equalsIgnoreCase(invoice.getStatus())) {
            paymentDateLabel.setText("Paid on: " + invoice.getPaymentDate());
            paymentDateLabel.setVisible(true);
            payApproveButton.setDisable(true); // Disable button for paid invoices
        } else {
            paymentDateLabel.setVisible(false);
            payApproveButton.setDisable(false);
        }

        // Set colour of status label
        switch (invoice.getStatus().toLowerCase()) {
            case "paid":
                statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                break;
            case "awaiting approval":
                statusLabel.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
                break;
            case "awaiting payment":
            case "unpaid":
                statusLabel.setStyle("-fx-text-fill: #FF5722; -fx-font-weight: bold;");
                break;
            default:
                statusLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        }

        // Change button behavior based on status
        if (invoice.getStatus().equalsIgnoreCase("awaiting Approval")) {
            payApproveButton.setText("Approve");
        } else {
            payApproveButton.setText("Pay");
        }
    }


    // Handles invoice action
    @FXML
    private void handleInvoiceAction(ActionEvent event) {
        JsonObject requestJson = new JsonObject();

        if (currentInvoice.getStatus().equalsIgnoreCase("awaiting Approval")) {
            requestJson.addProperty("type", "APPROVE_INVOICE");
        } else {
            requestJson.addProperty("type", "PAY_INVOICE");
        }

        JsonObject data = new JsonObject();
        data.addProperty("invoiceId", currentInvoice.getInvoiceId());
        requestJson.add("data", data);

        try {
            JsonObject responseJson = client.sendJsonMessage(requestJson);
            System.out.println("Server Response: " + responseJson);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewEditInvoice(ActionEvent event) throws IOException {

    }
}

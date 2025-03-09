package controllers;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private Button payButton;

    private Invoice invoice;

    public void setInvoiceData(Invoice invoice) {
        this.invoice = invoice;

        invoiceNumberLabel.setText("Invoice #: " + invoice.getInvoiceId());
        issueDateLabel.setText("Issue Date: " + invoice.getIssueDate());
        dueDateLabel.setText("Due Date: " + invoice.getDueDate());
        senderLabel.setText("Sender: " + invoice.getSender());
        amountLabel.setText("Total: $" + invoice.getTotalAmount());
        statusLabel.setText("Status: " + invoice.getStatus());

        // If status is "Paid", show payment date and disable the button
        if ("Paid".equalsIgnoreCase(invoice.getStatus())) {
            paymentDateLabel.setText("Paid on: " + invoice.getPaymentDate());
            paymentDateLabel.setVisible(true);
            payButton.setDisable(true); // Disable button for paid invoices
        } else {
            paymentDateLabel.setVisible(false);
            payButton.setDisable(false);
        }
    }


    // Temp function, need to add actual payment screen
    @FXML
    private void payInvoice() {
        try {
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("type", "PAY_INVOICE");

            JsonObject data = new JsonObject();
            data.addProperty("invoiceId", invoice.getInvoiceId());
            requestJson.add("data", data);

            // Send payment request to the server
            JsonObject responseJson = client.sendJsonMessage(requestJson);

            // Check response and update UI
            if (responseJson.has("status") && responseJson.get("status").getAsString().equals("success")) {
                statusLabel.setText("Status: Paid");
                paymentDateLabel.setText("Paid on: " + responseJson.get("paymentDate").getAsString());
                paymentDateLabel.setVisible(true);
                payButton.setDisable(true); // Disable button after payment
            } else {
                System.out.println("Payment failed: " + responseJson.get("message").getAsString());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

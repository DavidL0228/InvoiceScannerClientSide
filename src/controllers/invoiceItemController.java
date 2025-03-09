package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


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

    public void setInvoiceData(Invoice invoice) {
        invoiceNumberLabel.setText("Invoice #: " + invoice.getInvoiceId());
        issueDateLabel.setText("Issue Date: " + invoice.getIssueDate());
        dueDateLabel.setText("Due Date: " + invoice.getDueDate());
        senderLabel.setText("Sender: " + invoice.getSender());
        amountLabel.setText("Total: $" + invoice.getAmountDue());
    }
}

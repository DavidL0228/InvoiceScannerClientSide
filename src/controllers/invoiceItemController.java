package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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

    @FXML
    private CheckBox selectCheckBox;

    private invoiceListScreenController parentController;

    private Invoice currentInvoice;

    public void setParentController(invoiceListScreenController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSelection(ActionEvent event) {
        if (selectCheckBox.isSelected()) {
            currentInvoice.setSelected(true);
            System.out.println("Invoice " + currentInvoice.getInvoiceNumber() + " selected");
        } else {
            currentInvoice.setSelected(false);
        }
    }

    public void setInvoiceData(Invoice invoice) {
        this.currentInvoice = invoice;


        invoiceNumberLabel.setText("Invoice #: " + invoice.getInvoiceNumber());
        issueDateLabel.setText("Issue Date: " + invoice.getIssueDate());
        dueDateLabel.setText("Due Date: " + invoice.getDueDate());
        senderLabel.setText("Sender: " + invoice.getCompany());
        amountLabel.setText("Total: $" + invoice.getTotalAmount());
        statusLabel.setText(invoice.getStatus());

        // If status is "Paid", show payment date and disable the button
        if ("Paid".equalsIgnoreCase(invoice.getStatus())) {
            paymentDateLabel.setText("Paid on: " + invoice.getDatePaid());
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

        payApproveButton.setStyle(
                "-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-font-weight: bold;"
        );
        viewEditButton.setStyle(
                "-fx-background-color: white;" + "-fx-background-radius: 20;" + "-fx-font-weight: bold;"
        );
    }


    @FXML
    private void handleInvoiceAction(ActionEvent event) {
        if (currentInvoice.getStatus().equalsIgnoreCase("awaiting Approval")) {
            approveInvoice(); // Keeps "Approve" functionality unchanged
        } else {
            openPaymentScreen(event);
        }
    }

    // Opens the payment screen and passes selected invoice
    private void openPaymentScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/paymentScreen.fxml"));
            Parent root = loader.load();

            paymentScreenController controller = loader.getController();
            controller.setSelectedInvoices(List.of(currentInvoice)); // Send only this invoice

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

    // Handles approval action separately
    private void approveInvoice() {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "APPROVE_INVOICES");

        JsonArray invoiceIdsArray = new JsonArray();
        invoiceIdsArray.add(currentInvoice.getInternalId());

        JsonObject data = new JsonObject();
        data.add("invoiceIds", invoiceIdsArray);
        data.addProperty("token", client.getToken());
        requestJson.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(requestJson);

            if (response.has("status") && response.get("status").getAsString().equalsIgnoreCase("success")) {
                System.out.println("Invoices approved: " + response.get("message").getAsString());
            } else {
                System.out.println("Approval failed: " + response.get("message").getAsString());
                parentController.errorLabel.setText(response.get("message").getAsString());
                parentController.errorLabel.setVisible(true);

            }

            parentController.fetchInvoicesFromServer();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewEditInvoice(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editScreen.fxml"));
        Parent root = loader.load();

        editScreenController controller = loader.getController();
        controller.loadInvoiceData(currentInvoice);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();


        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }

    public void setCheckBoxSelected(boolean isSelected) {
        selectCheckBox.setSelected(isSelected);
    }

}

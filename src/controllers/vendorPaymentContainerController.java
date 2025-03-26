package controllers;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class vendorPaymentContainerController {

    @FXML
    private Label vendorNameLabel;

    @FXML
    private TableView<Invoice> invoiceTable;

    @FXML
    private TableColumn<Invoice, String> invoiceIdColumn;

    @FXML
    private TableColumn<Invoice, Double> subtotalColumn;

    @FXML
    private TableColumn<Invoice, Double> taxColumn;

    @FXML
    private TableColumn<Invoice, Double> totalColumn;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Button chequeButton;

    @FXML
    private Button paypalButton;

    @FXML
    private VBox containerVBox;

    @FXML
    private Label confirmationLabel;

    private String selectedPaymentMethod = null;

    private String vendorName;
    private double totalAmount;
    private ObservableList<Invoice> vendorInvoices = FXCollections.observableArrayList();

    public void initialize() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        invoiceTable.setItems(vendorInvoices);

        //chequeButton.setOnAction(e -> handlePaymentSelection("Cheque"));
        //.setOnAction(e -> handlePaymentSelection("PayPal"));
    }

    public void setVendorInvoices(int vendorId, List<Invoice> invoices) {
        this.vendorInvoices.setAll(invoices);

        vendor vendorObj = fetchVendorById(vendorId);
        if (vendorObj != null) {
            this.vendorName = vendorObj.getName();  // Used for payment logic and labels
            vendorNameLabel.setText(vendorObj.getName());
        } else {
            this.vendorName = "Unknown Vendor";
            vendorNameLabel.setText("Unknown Vendor");
        }

        updateTotalLabel();
    }


    private void updateTotalLabel() {
        this.totalAmount = vendorInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
        totalAmountLabel.setText(String.format("Total: $%.2f", this.totalAmount));
    }

    private void handlePaymentSelection(String method) {
        selectedPaymentMethod = method;
        chequeButton.setDisable(true);
        paypalButton.setDisable(true);
        System.out.println("Payment method selected for " + vendorName + ": " + method);

    }

    @FXML
    private void payVendorWithCheque() {
        try {
            List<Invoice> invoices = getVendorInvoices();
            if (invoices.isEmpty()) {
                System.out.println("No invoices to pay.");
                return;
            }

            // Total amount calculation
            //double totalAmount = invoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
            String amountNumber = String.format("$%.2f", totalAmount);
            String amountText = String.format("%.2f dollars", totalAmount); // Convert total amount to string
            String payeeName = invoices.get(0).getCompany(); // get vendor name from first vendor
            String memo = "Invoice Payment";
            String chequeNumber = String.valueOf(System.currentTimeMillis()); // Use timestamp to generate cheque number(may need diff logic?)
            String date = java.time.LocalDate.now().toString();
            String chequeTemplatePath = "resources/cheque-template.png";

            // Open file chooser to select save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Cheque PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("cheque_" + payeeName + "_" + chequeNumber + ".pdf");

            Stage stage = (Stage) chequeButton.getScene().getWindow(); // Get current window
            File saveFile = fileChooser.showSaveDialog(stage);

            if (saveFile != null) {
                chequeGenerator.generateCheque(
                        saveFile.getAbsolutePath(),
                        chequeTemplatePath,
                        payeeName,
                        amountText,
                        amountNumber,
                        date,
                        memo,
                        chequeNumber
                );

                // Notify server to mark invoices as paid
                JsonObject requestJson = new JsonObject();

                requestJson.addProperty("type", "MARK_INVOICES_PAID");

                JsonArray invoiceIds = new JsonArray();
                for (Invoice invoice : invoices) {
                    invoiceIds.add(invoice.getInternalId());
                }

                JsonObject data = new JsonObject();
                data.add("invoiceIds", invoiceIds);
                data.addProperty("amount", amountNumber);
                data.addProperty("paymentMethod", "cheque");
                data.addProperty("vendor", "payeeName");
                data.addProperty("paymentNumber", chequeNumber);


                requestJson.add("data", data);

                JsonObject response = client.sendJsonMessage(requestJson);
                System.out.println("Server response: " + response);

                showSuccessMessage();
            } else {
                System.out.println("Cheque generation canceled.");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    @FXML
    private void payVendorWithPaypal() {
        if (vendorInvoices.isEmpty()) {
            System.out.println("No invoices to pay.");
            return;
        }

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "PAY_WITH_PAYPAL");

        JsonObject data = new JsonObject();
        data.addProperty("vendor", vendorName);

        JsonArray invoiceIds = new JsonArray();
        for (Invoice invoice : vendorInvoices) {
            invoiceIds.add(invoice.getInternalId());
        }
        data.add("invoiceIds", invoiceIds);

        requestJson.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(requestJson);
            System.out.println("PayPal payment response: " + response);

            if (response.has("status") && response.get("status").getAsString().equalsIgnoreCase("success")) {
                System.out.println("Payment succeeded.");

                // Handle receipt
                if (response.has("receiptData") && response.has("receiptFilename")) {
                    String base64Receipt = response.get("receiptData").getAsString();
                    String receiptFilename = response.get("receiptFilename").getAsString();

                    saveReceiptToFile(base64Receipt, receiptFilename);
                }

                showSuccessMessage();
            } else {
                System.out.println("Payment failed or returned unexpected response.");
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void saveReceiptToFile(String base64Data, String suggestedFileName) {
        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Data);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Payment Receipt");
            fileChooser.setInitialFileName(suggestedFileName);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File saveFile = fileChooser.showSaveDialog(paypalButton.getScene().getWindow());

            if (saveFile != null) {
                java.nio.file.Files.write(saveFile.toPath(), decodedBytes);
                System.out.println("Receipt saved to: " + saveFile.getAbsolutePath());
            } else {
                System.out.println("Receipt save cancelled.");
            }

        } catch (IOException e) {
            System.err.println("Failed to save receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private vendor fetchVendorById(int vendorId) {
        JsonObject request = new JsonObject();
        request.addProperty("type", "GET_VENDOR_BY_ID");

        JsonObject data = new JsonObject();
        data.addProperty("vendor_id", vendorId);
        request.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(request);

            if (response.has("status") && response.get("status").getAsString().equalsIgnoreCase("success")) {
                JsonObject v = response.getAsJsonObject("vendor");
                return new vendor(
                        v.get("id").getAsInt(),
                        v.get("name").getAsString(),
                        v.get("email").getAsString(),
                        v.get("address").getAsString(),
                        v.get("defaultGL").getAsString()
                );
            } else {
                System.out.println("Failed to fetch vendor: " + response.get("message").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public void showSuccessMessage(){
        chequeButton.setDisable(true);
        paypalButton.setDisable(true);

        String message = String.format("Invoice successfully paid to %s for $%.2f", vendorName, totalAmount);

        Platform.runLater(() -> {
            containerVBox.getChildren().clear();
            confirmationLabel.setText(message);
            confirmationLabel.setVisible(true);
            containerVBox.getChildren().add(confirmationLabel);
        });
    }

    public String getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }

    public List<Invoice> getVendorInvoices() {
        return vendorInvoices;
    }

    public String getVendorName() {
        return vendorName;
    }
}

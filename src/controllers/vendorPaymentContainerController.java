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

    public void setVendorInvoices(String vendor, List<Invoice> invoices) {
        this.vendorName = vendor;
        vendorNameLabel.setText(vendor);
        vendorInvoices.setAll(invoices);
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
            double totalAmount = invoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
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
                requestJson.add("data", data);

                JsonObject response = client.sendJsonMessage(requestJson);
                System.out.println("Server response: " + response);

                chequeButton.setDisable(true);
                paypalButton.setDisable(true);

                String message = String.format("Invoice successfully paid to %s for $%.2f", vendorName, totalAmount);

                Platform.runLater(() -> {
                    containerVBox.getChildren().clear();
                    confirmationLabel.setText(message);
                    confirmationLabel.setVisible(true);
                    containerVBox.getChildren().add(confirmationLabel);
                });

            } else {
                System.out.println("Cheque generation canceled.");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    @FXML
    private void payVendorWithPaypal() {}

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

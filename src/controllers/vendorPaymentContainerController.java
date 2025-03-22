package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

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

    private String selectedPaymentMethod = null;

    private String vendorName;
    private ObservableList<Invoice> vendorInvoices = FXCollections.observableArrayList();

    public void initialize() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        invoiceTable.setItems(vendorInvoices);

        chequeButton.setOnAction(e -> handlePaymentSelection("Cheque"));
        paypalButton.setOnAction(e -> handlePaymentSelection("PayPal"));
    }

    public void setVendorInvoices(String vendor, List<Invoice> invoices) {
        this.vendorName = vendor;
        vendorNameLabel.setText(vendor);
        vendorInvoices.setAll(invoices);
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        double total = vendorInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
        totalAmountLabel.setText(String.format("Total: $%.2f", total));
    }

    private void handlePaymentSelection(String method) {
        selectedPaymentMethod = method;
        chequeButton.setDisable(true);
        paypalButton.setDisable(true);
        System.out.println("Payment method selected for " + vendorName + ": " + method);
        // You can add a callback or pass this info back to the main controller if needed
    }

    @FXML
    private void payVendorWithCheque() {}

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

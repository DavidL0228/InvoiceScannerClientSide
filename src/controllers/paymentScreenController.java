package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.List;

public class paymentScreenController {

    @FXML
    private TableView<Invoice> invoiceTable;

    @FXML
    private TableColumn<Invoice, String> invoiceIdColumn;

    @FXML
    private TableColumn<Invoice, String> emailColumn;

    @FXML
    private TableColumn<Invoice, Double> subtotalColumn;

    @FXML
    private TableColumn<Invoice, Double> taxColumn;

    @FXML
    private TableColumn<Invoice, Double> totalColumn;

    @FXML
    private TextField totalAmountField;

    @FXML
    private Button paypalButton, creditCardButton, chequeButton;

    private ObservableList<Invoice> selectedInvoices = FXCollections.observableArrayList();

    public void initialize() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        taxColumn.setCellValueFactory(new PropertyValueFactory<>("tax"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        invoiceTable.setItems(selectedInvoices);
        updateTotalAmount();
    }



    public void setSelectedInvoices(List<Invoice> invoices) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "GET_INVOICES_BY_IDS");

        JsonArray idArray = new JsonArray();
        for (Invoice invoice : invoices) {
            idArray.add(invoice.getInternalId());
        }

        JsonObject data = new JsonObject();
        data.add("invoiceIds", idArray);
        requestJson.add("data", data);

        try {
            JsonObject responseJson = client.sendJsonMessage(requestJson);
            if (responseJson.has("invoices")) {
                JsonArray jsonArray = responseJson.getAsJsonArray("invoices");
                ObservableList<Invoice> refreshedInvoices = FXCollections.observableArrayList();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonInvoice = jsonArray.get(i).getAsJsonObject();
                    Invoice invoice = new Invoice(
                            jsonInvoice.get("internal_id").getAsString(),
                            jsonInvoice.get("invoice_number").getAsString(),
                            jsonInvoice.get("company").getAsString(),
                            jsonInvoice.get("subtotal").getAsDouble(),
                            jsonInvoice.get("tax").getAsDouble(),
                            jsonInvoice.get("total").getAsDouble(),
                            jsonInvoice.get("gl_account").getAsString(),
                            jsonInvoice.get("email").getAsString(),
                            jsonInvoice.get("issue_date").getAsString(),
                            jsonInvoice.get("due_date").getAsString(),
                            jsonInvoice.get("date_paid").isJsonNull() ? null : jsonInvoice.get("date_paid").getAsString(),
                            jsonInvoice.get("status").getAsString(),
                            jsonInvoice.get("description").getAsString()
                    );
                    refreshedInvoices.add(invoice);
                }

                selectedInvoices.setAll(refreshedInvoices);
                invoiceTable.setItems(selectedInvoices);
                updateTotalAmount();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateTotalAmount() {
        double total = selectedInvoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
        totalAmountField.setText("$" + String.format("%.2f", total));
    }

    @FXML
    private void payWithPayPal(ActionEvent event) {
        processBatchPayment("PayPal");
    }

    @FXML
    private void payWithCreditCard(ActionEvent event) {
        processBatchPayment("Credit Card");
    }

    @FXML
    private void payWithCheque(ActionEvent event) {
        processBatchPayment("Cheque");
    }

    private void processBatchPayment(String paymentMethod) {
        if (selectedInvoices.isEmpty()) {
            System.out.println("No invoices selected for payment.");
            return;
        }

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "BATCH_PAYMENT");

        JsonObject data = new JsonObject();
        data.addProperty("paymentMethod", paymentMethod);

        JsonArray invoiceIds = new JsonArray();
        for (Invoice invoice : selectedInvoices) {
            invoiceIds.add(invoice.getInternalId());
        }
        data.add("invoiceIds", invoiceIds);

        requestJson.add("data", data);

        try {
            JsonObject responseJson = client.sendJsonMessage(requestJson);
            System.out.println("Server Response: " + responseJson);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceListScreen.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();


        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }
}

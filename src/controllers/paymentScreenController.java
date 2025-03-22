package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class paymentScreenController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox vendorPaymentContainer;

    // This will hold all updated invoices
    private final ObservableList<Invoice> allSelectedInvoices = FXCollections.observableArrayList();

    // Called from invoice list screen to provide selected invoices
    public void setSelectedInvoices(List<Invoice> selected) {
        fetchUpdatedInvoicesFromServer(selected);
    }

    private void fetchUpdatedInvoicesFromServer(List<Invoice> selected) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("type", "GET_INVOICES_BY_IDS");

        JsonArray idArray = new JsonArray();
        for (Invoice invoice : selected) {
            idArray.add(invoice.getInternalId());
        }

        JsonObject data = new JsonObject();
        data.add("invoiceIds", idArray);
        requestJson.add("data", data);

        try {
            JsonObject response = client.sendJsonMessage(requestJson);
            if (response.has("invoices")) {
                JsonArray jsonArray = response.getAsJsonArray("invoices");
                List<Invoice> refreshed = new ArrayList<>();

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
                    refreshed.add(invoice);
                }

                allSelectedInvoices.setAll(refreshed);
                populateGroupedVendorViews();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void populateGroupedVendorViews() {
        vendorPaymentContainer.getChildren().clear();

        Map<String, List<Invoice>> grouped = allSelectedInvoices.stream()
                .filter(inv -> "awaiting payment".equalsIgnoreCase(inv.getStatus()))
                .collect(Collectors.groupingBy(Invoice::getCompany));

        for (Map.Entry<String, List<Invoice>> entry : grouped.entrySet()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vendorPaymentContainer.fxml"));
                Parent vendorBox = loader.load();

                vendorPaymentContainerController controller = loader.getController();
                controller.setVendorInvoices(entry.getKey(), entry.getValue());

                vendorPaymentContainer.getChildren().add(vendorBox);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceListScreen.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
        stage.show();
    }
}

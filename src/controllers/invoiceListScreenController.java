package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class invoiceListScreenController {

    @FXML
    private VBox invoiceListContainer; // VBox inside ScrollPane for invoices

    public void initialize() {
        loadInvoicesFromJson();
    }

    private void loadInvoicesFromJson() {
        try {
            // Mock JSON response from the server (Replace with actual API call later)
            String jsonResponse = "["
                    + "{ \"invoiceId\": \"12345\", \"issueDate\": \"2024-03-08\", \"dueDate\": \"2024-04-08\", \"sender\": \"Company XYZ\", \"amountDue\": 500.00 },"
                    + "{ \"invoiceId\": \"67890\", \"issueDate\": \"2024-03-01\", \"dueDate\": \"2024-04-01\", \"sender\": \"Company ABC\", \"amountDue\": 350.50 }"
                    + "]";

            // Parse JSON string into a JsonArray
            JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
            List<controllers.Invoice> invoices = new ArrayList<>();

            // Convert JSON to Invoice objects
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonInvoice = jsonArray.get(i).getAsJsonObject();
                controllers.Invoice invoice = new controllers.Invoice(
                        jsonInvoice.get("invoiceId").getAsString(),
                        jsonInvoice.get("issueDate").getAsString(),
                        jsonInvoice.get("dueDate").getAsString(),
                        jsonInvoice.get("sender").getAsString(),
                        jsonInvoice.get("amountDue").getAsDouble()
                );
                invoices.add(invoice);
            }

            // Load invoices into the VBox
            for (controllers.Invoice invoice : invoices) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceItem.fxml"));
                Parent invoiceItem = loader.load();

                invoiceItemController controller = loader.getController();
                controller.setInvoiceData(invoice);

                invoiceListContainer.getChildren().add(invoiceItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

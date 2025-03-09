package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
        fetchInvoicesFromServer(1, 10, "issue_date", "desc");  // Default parameters
    }

    private void fetchInvoicesFromServer(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        try {
            // Create request JSON object
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("type", "GET_INVOICES");

            // Create "data" object with pagination & sorting params
            JsonObject data = new JsonObject();
            data.addProperty("pageNumber", pageNumber);
            data.addProperty("pageSize", pageSize);
            data.addProperty("sortBy", sortBy);
            data.addProperty("sortOrder", sortOrder);
            requestJson.add("data", data);

            // Send request to server
            JsonObject responseJson = client.sendJsonMessage(requestJson);

            // Ensure the response contains an array of invoices
            if (responseJson.has("invoices")) {
                JsonArray jsonArray = responseJson.getAsJsonArray("invoices");
                List<controllers.Invoice> invoices = new ArrayList<>();

                // Convert JSON to Invoice objects
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonInvoice = jsonArray.get(i).getAsJsonObject();
                    controllers.Invoice invoice = new controllers.Invoice(
                            jsonInvoice.get("invoiceId").getAsString(),
                            jsonInvoice.get("issueDate").getAsString(),
                            jsonInvoice.get("paymentDate").getAsString(),
                            jsonInvoice.get("sender").getAsString(),
                            jsonInvoice.get("totalAmount").getAsDouble(),
                            jsonInvoice.get("status").getAsString(),
                            jsonInvoice.get("paymentDate").getAsString()
                    );
                    invoices.add(invoice);
                }

                // Populate invoices into the VBox
                invoiceListContainer.getChildren().clear();  // Clear previous items
                for (controllers.Invoice invoice : invoices) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceItem.fxml"));
                    Parent invoiceItem = loader.load();

                    invoiceItemController controller = loader.getController();
                    controller.setInvoiceData(invoice);

                    invoiceListContainer.getChildren().add(invoiceItem);
                }
            } else {
                System.out.println("No invoices found in response.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

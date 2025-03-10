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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class invoiceListScreenController {

    @FXML
    private VBox invoiceListContainer; // VBox inside ScrollPane for invoices

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortOrderComboBox;

    @FXML
    private Button backButton;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageLabel;

    private int currentPage = 1;
    private int totalPages = 5;
    private final int pageSize = 5; // Number of invoices per page
    private String selectedSortBy = "issue_date"; // Default sort by issue date
    private String selectedSortOrder = "desc"; // Default descending order

    public void initialize() {
        setupSortingOptions();
        fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder);
    }

    private void setupSortingOptions() {
        // Define sorting criteria
        ObservableList<String> sortByOptions = FXCollections.observableArrayList(
                "Sender", "Due Date", "Issue Date", "Total Due", "Status", "Payment Date"
        );
        sortByComboBox.setItems(sortByOptions);
        sortByComboBox.setValue("Issue Date"); // Default selection

        // Define sorting order
        ObservableList<String> sortOrderOptions = FXCollections.observableArrayList("Ascending", "Descending");
        sortOrderComboBox.setItems(sortOrderOptions);
        sortOrderComboBox.setValue("Descending"); // Default selection

        // Listeners for sorting changes
        sortByComboBox.setOnAction(event -> updateSortBy());
        sortOrderComboBox.setOnAction(event -> updateSortOrder());
    }

    private void updateSortBy() {
        String selected = sortByComboBox.getValue();
        switch (selected) {
            case "Sender":
                selectedSortBy = "company";
                break;
            case "Due Date":
                selectedSortBy = "due_date";
                break;
            case "Issue Date":
                selectedSortBy = "issue_date";
                break;
            case "Total Due":
                selectedSortBy = "total";
                break;
            case "Status":
                selectedSortBy = "status";
                break;
            case "Payment Date":
                selectedSortBy = "date_paid";
                break;
            default:
                selectedSortBy = "issue_date";
        }
    }

    private void updateSortOrder() {
        selectedSortOrder = sortOrderComboBox.getValue().equals("Ascending") ? "asc" : "desc";
    }

    @FXML
    private void applySorting() {
        currentPage = 1;
        fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder);
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
                List<Invoice> invoices = new ArrayList<>();

                // Convert JSON to Invoice objects
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonInvoice = jsonArray.get(i).getAsJsonObject();
                    Invoice invoice = new Invoice(
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

                totalPages = responseJson.get("totalPages").getAsInt();
                updatePageControls();
                pageLabel.setText("Page " + currentPage + " of " + totalPages);


                // Populate invoices into the VBox
                invoiceListContainer.getChildren().clear();
                for (Invoice invoice : invoices) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceItem.fxml"));
                    Parent invoiceItem = loader.load();

                    invoiceItemController controller = loader.getController();
                    controller.setInvoiceData(invoice);

                    invoiceListContainer.getChildren().add(invoiceItem);
                }
            } else {
                System.out.println("No invoices found in response.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updatePageControls() {
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder);
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder);
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

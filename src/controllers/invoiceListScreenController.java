package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class invoiceListScreenController {

    @FXML
    private VBox invoiceListContainer;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortOrderComboBox;

    @FXML
    private ComboBox<Integer> pageSizeComboBox;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> selectionFilterComboBox;
    @FXML

    private ComboBox<String> vendorSelectionComboBox;

    @FXML
    private Button backButton;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Button toggleViewButton;

    @FXML
    private Button printSelectedButton;

    @FXML
    private Label pageLabel;

    @FXML
    private StackPane viewStackPane;

    @FXML
    private ScrollPane invoiceScrollPane;

    @FXML
    private ScrollPane itemViewPane;

    @FXML
    private TableView<Invoice> invoiceTableView;

    @FXML
    private TableColumn<Invoice, Boolean> selectColumn;

    @FXML
    private TableColumn<Invoice, String> invoiceIdColumn;

    @FXML
    private TableColumn<Invoice, String> vendorColumn;

    @FXML
    private TableColumn<Invoice, Double> subtotalColumn;

    @FXML
    private TableColumn<Invoice, Double> taxColumn;

    @FXML
    private TableColumn<Invoice, Double> totalColumn;

    @FXML
    private TableColumn<Invoice, String> statusColumn;

    private final ObservableList<Invoice> masterInvoiceList = FXCollections.observableArrayList();

    private boolean isTableView = false; // Default view is VBox/List
    private int currentPage = 1;
    private int totalPages = 5;
    private int pageSize = 10; // Default Number of invoices per page
    private String selectedSortBy = "issue_date"; // Default sort by issue date
    private String selectedSortOrder = "desc"; // Default descending order
    private String selectedStatusFilter = "Unpaid Invoices"; // Default filter

    // Setup UI components, get data from server
    public void initialize() {
        setupSortingOptions();
        setupPageSizeOptions();
        setupStatusFilter();
        setupInvoiceTable();
        setupSelectionDropdown();
        itemViewPane.setVisible(true);
        invoiceTableView.setVisible(false);
        fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
    }

    private void setupSelectionDropdown() {
        ObservableList<String> selectionOptions = FXCollections.observableArrayList(
                "Select All", "Select Upcoming Payments", "Select All of Vendor"
        );
        selectionFilterComboBox.setItems(selectionOptions);

        // Hide vendor selection by default
        vendorSelectionComboBox.setVisible(false);

        // Populate vendor dropdown
        ObservableList<String> vendorList = FXCollections.observableArrayList();
        for (Invoice invoice : masterInvoiceList) {
            if (!vendorList.contains(invoice.getCompany())) {
                vendorList.add(invoice.getCompany());
            }
        }
        vendorSelectionComboBox.setItems(vendorList);
    }

    @FXML
    private void handleSelectionFilter(ActionEvent event) throws IOException {
        String selectedOption = selectionFilterComboBox.getValue();

        if (selectedOption == null) {
            return; // Do nothing if no selection is made
        }

        for (Invoice invoice : masterInvoiceList) {
            invoice.setSelected(false);
        }

        switch (selectedOption) {
            case "Select All":
                //selects all invoices
                for (Invoice invoice : masterInvoiceList) {
                    invoice.setSelected(true);
                }
                vendorSelectionComboBox.setVisible(false);
                break;
            case "Select Upcoming Payments":
                //selects all upcoming due payments
                for (Invoice invoice : masterInvoiceList) {
                    if (isUpcoming(invoice)) {
                        invoice.setSelected(true);
                    }
                }
                vendorSelectionComboBox.setVisible(false);
                break;
            case "Select All of Vendor":
                populateVendorDropdown();
                vendorSelectionComboBox.setVisible(true);
                break;
        }

        updateInvoiceListView(masterInvoiceList);
        updateInvoiceTableView(masterInvoiceList);

    }

    private void populateVendorDropdown() {
        Set<String> vendors = invoiceTableView.getItems().stream()
                .map(Invoice::getCompany)
                .collect(Collectors.toSet());

        vendorSelectionComboBox.setItems(FXCollections.observableArrayList(vendors));
        vendorSelectionComboBox.setValue("Select a Vendor");
    }

    @FXML
    private void handleVendorSelection(ActionEvent event) throws IOException {
        for (Invoice invoice : masterInvoiceList) {
            invoice.setSelected(false);
        }

        String selectedVendor = vendorSelectionComboBox.getValue();
        if (!selectedVendor.equals("Select a Vendor")) {
            selectVendorInvoices(selectedVendor);
        }

        updateInvoiceListView(masterInvoiceList);
        updateInvoiceTableView(masterInvoiceList);

    }


    private void selectVendorInvoices(String vendor) {
        for (Invoice invoice : masterInvoiceList) {
            if (invoice.getCompany().equalsIgnoreCase(vendor)) {
                invoice.setSelected(true);
            }
        }
    }

    private boolean isUpcoming(Invoice invoice) {
        try {
            // Parse the due date from the invoice
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dueDate = LocalDate.parse(invoice.getDueDate(), formatter);
            LocalDate today = LocalDate.now();

            // Check if the due date is in the current month and year
            return dueDate.getYear() == today.getYear() && dueDate.getMonth() == today.getMonth();
        } catch (Exception e) {
            System.out.println("Error parsing due date for invoice " + invoice.getInvoiceNumber());
            return false;
        }
    }

    private void setupInvoiceTable() {

        selectColumn.setCellFactory(tc -> new CheckBoxTableCell<>());

        selectColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            BooleanProperty property = invoice.selectedProperty();

            // Ensure the checkbox reflects the invoice's selected state
            property.set(invoice.isSelected());

            property.addListener((obs, oldVal, newVal) -> {
                invoice.setSelected(newVal);
                System.out.println("Invoice " + invoice.getInvoiceNumber() + (newVal ? " selected" : " deselected"));

            });

            return property;
        });

        // Data columns
        invoiceIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        vendorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompany()));
        subtotalColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSubtotal()).asObject());
        taxColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTax()).asObject());
        totalColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
    }

    private void refreshInvoiceListView() {
        invoiceListContainer.getChildren().clear(); // Clear previous items

        for (Invoice invoice : invoiceTableView.getItems()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceItem.fxml"));
                Parent invoiceItem = loader.load();

                invoiceItemController controller = loader.getController();
                controller.setInvoiceData(invoice);

                invoiceListContainer.getChildren().add(invoiceItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void toggleView() {
        isTableView = !isTableView; // Toggle the state

        itemViewPane.setVisible(!isTableView);
        invoiceTableView.setVisible(isTableView);

        // Ensure the table allows editing
        invoiceTableView.setEditable(isTableView);

        // Ensure only the active view is interactive
        itemViewPane.setMouseTransparent(isTableView);
        invoiceTableView.setMouseTransparent(!isTableView);

        //clear selected invoices
        for (Invoice invoice : invoiceTableView.getItems()) {
            invoice.setSelected(false);
        }

        //refresh list from database
        fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);

        // Reset the drop-down menu so the same option can be selected again
        selectionFilterComboBox.getSelectionModel().clearSelection();
        selectionFilterComboBox.setValue(null); // Clear selection

        vendorSelectionComboBox.getSelectionModel().clearSelection();
        vendorSelectionComboBox.setValue(null); // Clear selection

        if (isTableView) {
            toggleViewButton.setText("Switch to List View");
        } else {
            toggleViewButton.setText("Switch to Table View");
            refreshInvoiceListView();
        }
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
        sortByComboBox.setOnAction(event -> {
            updateSortBy();
            currentPage = 1; // Reset to first page
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
        });
        sortOrderComboBox.setOnAction(event -> {
            updateSortOrder();
            currentPage = 1; // Reset to first page
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);

        });
    }

    private void setupPageSizeOptions() {
        ObservableList<Integer> pageSizeOptions = FXCollections.observableArrayList(5, 10, 20, 50);
        pageSizeComboBox.setItems(pageSizeOptions);
        pageSizeComboBox.setValue(10); // Default page size

        // Update page size when selection changes
        pageSizeComboBox.setOnAction(event -> {
            pageSize = pageSizeComboBox.getValue();
            currentPage = 1; // Reset to first page
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
        });
    }

    private void setupStatusFilter() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Paid Invoices", "Waiting for Approval", "Waiting for Payment", "Unpaid Invoices", "All Invoices"
        );
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.setValue("Unpaid Invoices"); // Default selection

        // Update filter when selection changes
        statusFilterComboBox.setOnAction(event -> {
            selectedStatusFilter = statusFilterComboBox.getValue();
            currentPage = 1; // Reset to first page
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
        });
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
        fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
    }


    public ObservableList<Invoice> getSelectedInvoices() {
        ObservableList<Invoice> selectedList = FXCollections.observableArrayList();
        for (Invoice invoice : invoiceTableView.getItems()) {
            if (invoice.isSelected()) {
                selectedList.add(invoice);
            }
        }
        return selectedList;
    }

    @FXML
    private void exportSelectedInvoicesAsCSV() {
        ObservableList<Invoice> selectedInvoices = getSelectedInvoices();

        if (selectedInvoices.isEmpty()) {
            System.out.println("No invoices selected to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Invoices CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("invoices.csv");
        File file = fileChooser.showSaveDialog(viewStackPane.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write CSV header
                writer.println("Invoice Number,Vendor,Subtotal,Tax,Total,Status");

                // Write each selected invoice
                for (Invoice invoice : selectedInvoices) {
                    writer.printf("%s,%s,%.2f,%.2f,%.2f,%s%n",
                            invoice.getInvoiceNumber(),
                            invoice.getCompany(),
                            invoice.getSubtotal(),
                            invoice.getTax(),
                            invoice.getTotalAmount(),
                            invoice.getStatus()
                    );
                }

                System.out.println("Invoices exported successfully to: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void fetchInvoicesFromServer(int pageNumber, int pageSize, String sortBy, String sortOrder, String statusFilter) {
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
            data.addProperty("statusFilter", statusFilter);
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
                    invoices.add(invoice);
                }

                masterInvoiceList.setAll(invoices);

                if (responseJson.has("totalPages")) {
                    totalPages = responseJson.get("totalPages").getAsInt();
                }


                updatePageControls();
                pageLabel.setText("Page " + currentPage + " of " + totalPages);


                updateInvoiceListView(masterInvoiceList);
                updateInvoiceTableView(masterInvoiceList);
            } else {
                System.out.println("No invoices found in response.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Update existing list population method to pass the parent reference
    private void updateInvoiceListView(List<Invoice> invoices) throws IOException {
        invoiceListContainer.getChildren().clear();
        for (Invoice invoice : invoices) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invoiceItem.fxml"));
                Parent invoiceItem = loader.load();

                invoiceItemController controller = loader.getController();
                controller.setInvoiceData(invoice);
                controller.setParentController(this);

                if (invoice.isSelected()) {
                    controller.setCheckBoxSelected(true);
                }

                invoiceListContainer.getChildren().add(invoiceItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Populate TableView
    private void updateInvoiceTableView(List<Invoice> invoices) {
        invoiceTableView.setItems(FXCollections.observableArrayList(invoices));
    }

    private void updatePageControls() {
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            fetchInvoicesFromServer(currentPage, pageSize, selectedSortBy, selectedSortOrder, selectedStatusFilter);
        }
    }


    @FXML
    void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();


        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void openPaymentScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/paymentScreen.fxml"));
            Parent root = loader.load();

            paymentScreenController controller = loader.getController();

            // Filter selected invoices for "awaiting payment"
            List<Invoice> awaitingPaymentInvoices = getSelectedInvoices().stream()
                    .filter(invoice -> "awaiting payment".equalsIgnoreCase(invoice.getStatus()))
                    .toList();

            controller.setSelectedInvoices(awaitingPaymentInvoices);

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

}

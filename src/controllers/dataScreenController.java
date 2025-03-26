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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class dataScreenController {

    @FXML
    private SplitMenuButton accountMenu;

    @FXML
    private TableColumn<Map, Integer> actualColumn;

    @FXML
    private TableColumn<Map, Integer> budgetColumn;

    @FXML
    private TableView<Object> budgetTableView;

    @FXML
    private TableView<Object> actualsTableView;

    @FXML
    private TableColumn<Map, Integer> amountColumn;

    @FXML
    private TableColumn<Map, String> monthColumn;

    @FXML
    private Button applyButton;

    @FXML
    private Button backButton;

    @FXML
    private TableColumn<Map, String> dateColumn;

    @FXML
    private LineChart<String, Integer> lineChart;

    @FXML
    private Text monthYearText;

    @FXML
    private Text monthYearText2;

    @FXML
    private TableColumn<Map, String> vendorColumn;

    @FXML
    private SplitMenuButton yearMenu;

    private int year;
    private int yearMenuSelection;

    private String account;
    private String accountMenuSelection;

    private int oldestYear;

    ArrayList<String> months = new ArrayList<>(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
    ArrayList<Integer> monthTotals = new ArrayList<>();

    ArrayList<Integer> budgets = new ArrayList<>(Arrays.asList(400, 300, 400, 500, 700, 450, 600, 380, 200, 480, 690, 530));

    @FXML
    void applyButtonClicked(ActionEvent event) throws IOException, InterruptedException {

        if(accountMenu.textProperty().get().equals("Account") || yearMenu.textProperty().get().equals("Year"))
            return;

        if(!Objects.equals(accountMenuSelection, account) || yearMenuSelection!=year) {
            setLineChart(accountMenuSelection, yearMenuSelection);
            setMonthInfo(accountMenuSelection, yearMenuSelection);
        }
    }

    @FXML
    void backButtonClicked(ActionEvent event) {
        try {
            // Load the home screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch(Exception e){
            System.out.println("Error loading page");
        }
    }

    public void initialize() throws IOException, InterruptedException {

        // get year of oldest payment record from server

        JsonObject requestJson = new JsonObject();

        // Get data from input fields
        requestJson.addProperty("type", "GET_OLDEST_YEAR");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        // and add roles
        requestJson.add("data", data);

        // Send JSON to server
        String jsonResponse = client.sendJsonMessage(requestJson).get("date").getAsString();
        oldestYear = Integer.parseInt(jsonResponse.substring(0,4));

        // add years as menu items
        for(int i=oldestYear; i<=LocalDate.now().getYear(); i++)
            yearMenu.getItems().add(new MenuItem(String.valueOf(i)));

        ObservableList<MenuItem> yearMenuItems = yearMenu.getItems();

        // create listeners for menu items to obtain selected value
        for(MenuItem yearMenuItem : yearMenuItems){
            yearMenuItem.setOnAction((e)->{
                yearMenu.textProperty().set(yearMenuItem.textProperty().getValue());
                yearMenuSelection = Integer.parseInt(yearMenuItem.textProperty().getValue());
            });
        }

        // get gl accounts from server

        JsonObject requestJson2 = new JsonObject();

        // Get data from input fields
        requestJson2.addProperty("type", "GET_GL_ACCOUNTS");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data2 = new JsonObject();
        // and add roles
        requestJson2.add("data", data2);

        // Send JSON to server
        JsonArray jsonGL = client.sendJsonMessage(requestJson2).get("gl_accounts").getAsJsonArray();

        for(int i=0; i< jsonGL.size(); i++){
            accountMenu.getItems().add(new MenuItem(jsonGL.get(i).getAsString()));
        }

        ObservableList<MenuItem> accountMenuItems = accountMenu.getItems();
        accountMenuSelection = String.valueOf(accountMenuItems.get(0));

        // create listeners for menu items to obtain selected value
        for(MenuItem accountMenuItem : accountMenuItems){
            accountMenuItem.setOnAction((e)->{
                accountMenu.textProperty().set(accountMenuItem.textProperty().getValue());
                accountMenuSelection = String.valueOf(accountMenuItem.textProperty().getValue());
            });
        }
    }

    private void setLineChart(String a, int yr) throws IOException, InterruptedException {

        lineChart.getData().clear();

        // get amount for month of year

        JsonObject requestJson = new JsonObject();

        // Get data from input fields
        requestJson.addProperty("type", "GET_PAYMENT_AMOUNT_PER_MONTH");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        data.addProperty("account", a);
        data.addProperty("year", yr);
        requestJson.add("data", data);

        // Send JSON to server
        JsonObject jsonResponse = client.sendJsonMessage(requestJson);

        try{
            JsonArray jsonArray = jsonResponse.getAsJsonArray("data");
            jsonArray.size();
        } catch (Exception e) {
            System.out.println("No data.");
            return;
        }

        ArrayList<Integer> actuals = new ArrayList<>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        ));

        JsonArray jsonArray = jsonResponse.getAsJsonArray("data");

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonAmount = jsonArray.get(i).getAsJsonObject();
            int mnth = jsonAmount.get("month").getAsInt();
            int amnt = jsonAmount.get("total").getAsInt();
            System.out.println(mnth+" "+i);
            actuals.remove(mnth-1);
            actuals.add(mnth-1, amnt);
        }
        System.out.println(actuals);
        monthTotals = actuals;

        XYChart.Series actualsSeries = new XYChart.Series();
        actualsSeries.setName("Actuals");

        for(int i=0; i<12; i++) {
            actualsSeries.getData().add(new XYChart.Data(months.get(i), actuals.get(i)));
        }
        lineChart.getData().add(actualsSeries);


        // get budget values
        Collections.shuffle(budgets);

        XYChart.Series budgetsSeries = new XYChart.Series();
        budgetsSeries.setName("Budget");

        for(int i=0; i<12; i++) {
            budgetsSeries.getData().add(new XYChart.Data(months.get(i), budgets.get(i)));
        }
        lineChart.getData().add(budgetsSeries);

    }

    private void setMonthInfo(String a, int yr) throws IOException, InterruptedException {

        actualsTableView.getItems().clear();
        budgetTableView.getItems().clear();

        account = a;
        year = yr;

        monthYearText.setText(yr + " - " + a);
        monthYearText2.setText(yr + " - " + a);

        // get summary for table

        JsonObject requestJson = new JsonObject();

        // Get data from input fields
        requestJson.addProperty("type", "GET_PAYMENT_SUMMARY");  // Specify the message type

        // Adds data in a nested json object
        JsonObject data = new JsonObject();
        data.addProperty("account", a);
        data.addProperty("year", yr);
        requestJson.add("data", data);

        // Send JSON to server
        JsonObject jsonResponse = client.sendJsonMessage(requestJson);

        try{
            JsonArray jsonArray = jsonResponse.getAsJsonArray("data");
            jsonArray.size();
        } catch (Exception e) {
            System.out.println("No data.");
            return;
        }

        dateColumn.setCellValueFactory(new MapValueFactory<>("date"));
        vendorColumn.setCellValueFactory(new MapValueFactory<>("vendor"));
        amountColumn.setCellValueFactory(new MapValueFactory<>("amount"));

        ObservableList<Map<String, Object>> tableItems = FXCollections.observableArrayList();

        JsonArray jsonArray = jsonResponse.getAsJsonArray("data");

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonAmount = jsonArray.get(i).getAsJsonObject();
            Map<String, Object> item = new HashMap<>();
            item.put("date", jsonAmount.get("date").getAsString());
            item.put("vendor", jsonAmount.get("vendor").getAsString());
            item.put("amount", jsonAmount.get("amount").getAsInt());

            tableItems.add(item);
        }

        actualsTableView.getItems().addAll(tableItems);


        //set budget table

        monthColumn.setCellValueFactory(new MapValueFactory<>("month"));
        budgetColumn.setCellValueFactory(new MapValueFactory<>("budget"));
        actualColumn.setCellValueFactory(new MapValueFactory<>("actual"));

        ObservableList<Map<String, Object>> tableItems2 = FXCollections.observableArrayList();

        for (int i = 0; i < monthTotals.size(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", months.get(i));
            item.put("budget", budgets.get(i));
            item.put("actual", monthTotals.get(i));

            tableItems2.add(item);
        }

        budgetTableView.getItems().addAll(tableItems2);

    }

}

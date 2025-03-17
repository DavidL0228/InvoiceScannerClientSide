package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

public class dataScreenController {

    @FXML
    private LineChart<Number, Number> allTimeLineChart;

    @FXML
    private Tab allTimeTab;

    @FXML
    private Button applyButton;

    @FXML
    private Button backButton;

    @FXML
    private Button currentMonthButton;

    @FXML
    private Button currentYearButton;

    @FXML
    private LineChart<String, Number> monthLineChart;

    @FXML
    private SplitMenuButton monthMenu;

    @FXML
    private Tab monthTab;

    @FXML
    private Text monthlySpendingText;

    @FXML
    private Button nextMonthButton;

    @FXML
    private Button nextYearButton;

    @FXML
    private Button previousMonthButton;

    @FXML
    private Button previousYearButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private LineChart<Number, Number> yearLineChart;

    @FXML
    private SplitMenuButton yearMenu;

    @FXML
    private Tab yearTab;

    @FXML
    private Text yearText;

    @FXML
    private Text yearlySpendingText;

    private String month;
    private String monthMenuSelection;

    private int year;
    private int yearMenuSelection;

    private String selectedTab = "Month";

    private int oldestYear;

    @FXML
    void applyButtonClicked(ActionEvent event) {

        if(yearMenu.textProperty().get().equals("Year"))
            return;

        if(selectedTab.equals("Month")) {
            if(monthMenu.textProperty().get().equals("Month"))
                return;
            setMonthLineChart(yearMenuSelection, monthMenuSelection);
        }
        else if(selectedTab.equals("Year"))
            setYearLineChart(yearMenuSelection);

        if(yearText.toString().equals(String.valueOf(yearMenuSelection)))
            return;


        // get average monthly spending for the selected year
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

    @FXML
    void currentMonthButtonClicked(ActionEvent event) {
        setMonthLineChart(LocalDate.now().getYear(), LocalDate.now().getMonth().toString());
    }

    @FXML
    void currentYearButtonClicked(ActionEvent event) {
        setYearLineChart(LocalDate.now().getYear());
    }

    @FXML
    void nextMonthButtonClicked(ActionEvent event) {
        month = String.valueOf(Month.valueOf(month).plus(1));
        if(month.equals("JANUARY"))
            year++;
        setMonthLineChart(year, month);
    }

    @FXML
    void nextYearButtonClicked(ActionEvent event) {
        year++;
        setYearLineChart(year);
    }

    @FXML
    void previousMonthButtonClicked(ActionEvent event) {
        month = String.valueOf(Month.valueOf(month).minus(1));
        if(month.equals("DECEMBER"))
            year--;
        setMonthLineChart(year, month);
    }

    @FXML
    void previousYearButtonClicked(ActionEvent event) {
        year--;
        setYearLineChart(year);
    }

    public void initialize(){

        // get current month and year as default values
        month = LocalDate.now().getMonth().toString();
        year = LocalDate.now().getYear();
        monthMenuSelection = month;
        yearMenuSelection = year;

        // create listener for selected tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedTab = newValue.getText();
            switch (selectedTab) {
                case "Month" -> monthMenu.setDisable(false);
                case "Year", "All Time" -> monthMenu.setDisable(true);
            }
        });

        ObservableList<MenuItem> monthMenuItems = monthMenu.getItems();

        // create listeners for menu items to obtain selected value
        for(MenuItem monthMenuItem : monthMenuItems){
            monthMenuItem.setOnAction((e)->{
                monthMenu.textProperty().set(monthMenuItem.textProperty().getValue());
                monthMenuSelection = monthMenuItem.textProperty().getValue();
            });
        }


        // get year of oldest payment record from server

        oldestYear = 2021; // for testing purposes only

        // add years as menu items
        for(int i=oldestYear; i<=year; i++)
            yearMenu.getItems().add(new MenuItem(String.valueOf(i)));

        ObservableList<MenuItem> yearMenuItems = yearMenu.getItems();

        // create listeners for menu items to obtain selected value
        for(MenuItem yearMenuItem : yearMenuItems){
            yearMenuItem.setOnAction((e)->{
                yearMenu.textProperty().set(yearMenuItem.textProperty().getValue());
                yearMenuSelection = Integer.parseInt(yearMenuItem.textProperty().getValue());
            });
        }

    }

    private void setMonthLineChart(int yr, String m){

        year = yr;
        month = m;

        monthLineChart.setTitle(m + " " + yr);

        previousMonthButton.setDisable(yr == oldestYear && m.equals("JANUARY"));

        nextMonthButton.setDisable(yr == LocalDate.now().getYear() && m.equals(LocalDate.now().getMonth().toString()));

        // get payment amount for each day of month

        // set month line chart with values
    }

    private void setYearLineChart(int yr){

        yearLineChart.setTitle(String.valueOf(yr));

        previousYearButton.setDisable(yr == oldestYear);

        nextYearButton.setDisable(yr == LocalDate.now().getYear());

        // get payment amount of each month in year

        // set year line chart with values
    }

    private void setAllTimeLineChart(){

        // get payment amount of each year

        // set all-time line chart with values
    }

}

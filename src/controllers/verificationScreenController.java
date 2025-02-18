package controllers;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class verificationScreenController {

    @FXML
    private TextField DueBox;

    @FXML
    private TextField GLBox;

    @FXML
    private TextField IssueDateBox;

    @FXML
    private TextField SubTotalBox;

    @FXML
    private TextField invoiceNumBox;

    @FXML
    private TextField taxBox;

    @FXML
    private TextField totalBox;

    @FXML
    private Button uploadDataButton;

    @FXML
    private TextField vendorBox;

    @FXML
    void uploadData(ActionEvent event) throws IOException, InterruptedException {
        JsonObject jsonObject = getJsonObject();


        //send to server
        client.sampleRequest(jsonObject, "/sample");

        //return to main menu
        Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml"))));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private JsonObject getJsonObject() {
        JsonObject jsonObject = new JsonObject();

        //get data from boxes and add to json object
        jsonObject.addProperty("due",DueBox.getText());
        jsonObject.addProperty("GL",GLBox.getText());
        jsonObject.addProperty("issueDate",IssueDateBox.getText());
        jsonObject.addProperty("subTotal",SubTotalBox.getText());
        jsonObject.addProperty("invoiceNum",invoiceNumBox.getText());
        jsonObject.addProperty("tax",taxBox.getText());
        jsonObject.addProperty("total",totalBox.getText());
        jsonObject.addProperty("vendor",vendorBox.getText());
        return jsonObject;
    }

}

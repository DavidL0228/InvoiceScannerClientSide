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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class verificationScreenController {

    @FXML
    private ImageView imageView;

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
        //client.sampleRequest(jsonObject, "/sample");

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

    public void setData(JsonObject jsonObject, String filePath) {

        File file = new File(filePath);
        if (file.exists()) {
            String fileUrl = file.toURI().toString(); // Convert to URI and then to String
            Image image = new Image(fileUrl);
            imageView.setImage(image);
        }

        if (jsonObject.has("due")){
            DueBox.setText(jsonObject.get("due").getAsString());
        }
        if (jsonObject.has("GL")){
            GLBox.setText(jsonObject.get("GL").getAsString());
        }
        if (jsonObject.has("date")){
            IssueDateBox.setText(jsonObject.get("eDate").getAsString());
        }
        if (jsonObject.has("subTotal")){
            SubTotalBox.setText(jsonObject.get("subTotal").getAsString());
        }
        if (jsonObject.has("invoice_num")){
            invoiceNumBox.setText(jsonObject.get("invoice_num").getAsString());
        }
        if (jsonObject.has("tax")){
            taxBox.setText(jsonObject.get("tax").getAsString());
        }
        if (jsonObject.has("total")){
            totalBox.setText(jsonObject.get("total").getAsString());
        }
        if (jsonObject.has("vendor")){
            vendorBox.setText(jsonObject.get("vendor").getAsString());
        }

    }
}

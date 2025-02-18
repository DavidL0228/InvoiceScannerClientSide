package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class uploadInvoiceScreenController {

    @FXML
    private Button backButton;

    @FXML
    private Button uploadButton;

    @FXML
    void goBackToMain(ActionEvent event) throws IOException, InterruptedException {
        Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getResource("/fxml/invoiceListScreen.fxml"))));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void uploadNewInvoice(ActionEvent event) throws IOException, InterruptedException {
        Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getResource("/fxml/verificationScreen.fxml"))));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}

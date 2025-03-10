package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import com.google.gson.JsonObject;

public class uploadInvoiceScreenController {

    @FXML
    private Button backButton;

    @FXML
    private Button uploadButton;

    @FXML
    void goBackToMain(ActionEvent event) throws IOException, InterruptedException {
        Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml"))));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void uploadNewInvoice(ActionEvent event) throws IOException, InterruptedException {

        // initialize file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        // freeze current window, show filechooser, get selected file
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (file == null) { // no file selected
            System.out.println("No file selected.");
            return;
        }

        System.out.println("File selected: " + file);

        // send file to server
        JsonObject data = client.sendFile(file.getAbsolutePath());

        // load verification screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/verificationScreen.fxml"));
        Parent root = loader.load();

        verificationScreenController controller = loader.getController();

        controller.setData(data, file.getAbsolutePath());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}

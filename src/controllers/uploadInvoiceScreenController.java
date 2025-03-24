package controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


import javax.imageio.ImageIO;


public class uploadInvoiceScreenController {

    public ProgressIndicator progressBar;
    public Label labels;
    @FXML
    private Button backButton;

    @FXML
    private Button uploadButton;

    @FXML
    void goBackToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getResource("/fxml/homeScreen.fxml"))));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();


        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }



    @FXML
    void uploadNewInvoice(ActionEvent event) throws IOException {

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

        if (file.getName().endsWith(".pdf")){
            PDDocument doc = Loader.loadPDF(file);
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            BufferedImage i = pdfRenderer.renderImage(0);
            String fileName = "resources/tmp/img.png";
            File file2 = new File(fileName);
            ImageIO.write(i, "PNG", file2);
            doc.close();
            file = new File("resources/tmp/img.png");
        }

        // send file to server
        AtomicReference<JsonObject> data = new AtomicReference<>(new JsonObject());
        File finalFile = file;
        Task<JsonObject> uploadTask = new Task<JsonObject>() {
            @Override
            protected JsonObject call() throws Exception {
                return client.sendFile(finalFile.getAbsolutePath());
            }
        };

        File finalFile1 = file;
        uploadTask.setOnSucceeded(event1 -> {
            progressBar.setVisible(false);
            data.set(uploadTask.getValue());
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            // load verification screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/verificationScreen.fxml"));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            verificationScreenController controller = loader.getController();

            controller.setData(data.get(), finalFile1.getAbsolutePath());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        });

        uploadTask.setOnFailed(event1 -> {
            progressBar.setVisible(false);
            Throwable exception = uploadTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Uplaod Error");
            alert.setHeaderText(null);
            alert.setContentText("Error: " + exception.getMessage());
            alert.showAndWait();
        });

        uploadButton.setVisible(false);
        labels.setText("   Uploading...");
        progressBar.setVisible(true);
        new Thread(uploadTask).start();
    }

}

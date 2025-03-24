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

import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.util.Objects;
import com.google.gson.JsonObject;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


import javax.imageio.ImageIO;


public class uploadInvoiceScreenController {

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
        JsonObject data = client.sendFile(file.getAbsolutePath());

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();

        // load verification screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/verificationScreen.fxml"));
        Parent root = loader.load();

        verificationScreenController controller = loader.getController();

        controller.setData(data, file.getAbsolutePath());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }

}

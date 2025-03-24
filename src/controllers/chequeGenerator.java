package controllers;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class chequeGenerator {

    public static void generateCheque(
            String outputPath,
            String chequeTemplatePath,
            String payeeName,
            String amountText,
            String amountNumber,
            String date,
            String memo,
            String chequeNumber
    ) throws IOException {

        // Initialize PDF writer
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        float[] imageSize = getImageSize("resources/cheque-template.png");
        System.out.printf("1: " + imageSize[0] + " 2: " + imageSize[1]);
        PageSize chequeSize = new PageSize(imageSize[0], imageSize[1]);
        pdfDoc.setDefaultPageSize(chequeSize);

        // Add background cheque image
        if (chequeTemplatePath != null && !chequeTemplatePath.isEmpty()) {
            ImageData bgImageData = ImageDataFactory.create(chequeTemplatePath);
            Image bgImage = new Image(bgImageData);
            bgImage.setFixedPosition(0, 0);
            bgImage.setAutoScale(false);
            document.add(bgImage);
        }

        // Overlay cheque data
        document.add(new Paragraph(payeeName)
                .setFixedPosition(140, 135, 400));
        document.add(new Paragraph(amountNumber)
                .setFixedPosition(440, 140, 150));
        document.add(new Paragraph(amountText)
                .setFixedPosition(140, 100, 500));
        document.add(new Paragraph(date)
                .setFixedPosition(470, 223, 150));
        document.add(new Paragraph(memo)
                .setFixedPosition(90, 45, 300));
        document.add(new Paragraph(chequeNumber)
                .setFixedPosition(80, 20, 150));

        // Close the document
        document.close();

        System.out.println("Cheque PDF generated at: " + outputPath);
    }

    public static float[] getImageSize(String imagePath) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
        float width = bufferedImage.getWidth();
        float height = bufferedImage.getHeight();
        return new float[]{width, height};
    }

    // test generation
    public static void main(String[] args) {
        try {
            generateCheque(
                    "cheque_output.pdf",
                    "resources/cheque-template.png", // path to your cheque background image
                    "John Doe",
                    "One Thousand Dollars",
                    "$1,000.00",
                    "2025-03-19",
                    "Invoice Payment",
                    "001"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

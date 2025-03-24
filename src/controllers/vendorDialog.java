package controllers;


import javafx.geometry.Insets;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;


import java.util.Optional;

public class vendorDialog{

    public static Optional<UserInfo> showVendorDialog(String vendorName) {
        // Create a custom dialog
        Dialog<UserInfo> dialog = new Dialog<>();
        dialog.setTitle("Create Vendor");
        dialog.setHeaderText("Enter Vendor information");

        // Set the button types (OK and Cancel)
        ButtonType okButtonType = new ButtonType("OK", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create labels and text fields for the custom form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setText(vendorName);

        TextField glField = new TextField();
        glField.setPromptText("Default GL account");

        TextField paymentField = new TextField();
        paymentField.setPromptText("Payment Info");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Default GL Account:"), 0, 1);
        grid.add(glField, 1, 1);
        grid.add(new Label("Payment Info:"), 0, 2);
        grid.add(paymentField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);

        // Add the grid to the dialog pane
        dialog.getDialogPane().setContent(grid);

        // Convert the result to a UserInfo object when the OK button is pressed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new UserInfo(nameField.getText(), glField.getText(), paymentField.getText(),addressField.getText(),emailField.getText());
            }
            return null;
        });

        // Show the dialog and capture the result
        return dialog.showAndWait();


    }

    // A simple class to store user information
    public static class UserInfo {
        private static String name;
        private static String gl;
        private static String payment;
        private static String address;
        private static String email;
        public UserInfo(String name, String gl, String payment, String address, String email) {
            UserInfo.name = name;
            UserInfo.gl = gl;
            UserInfo.payment = payment;
            UserInfo.address = address;
            UserInfo.email = email;
        }

        public static String getName() {
            return name;
        }
        public static String getGl() {
            return gl;
        }
        public static String getPayment() {
            return payment;
        }
        public static String getAddress() {
            return address;
        }
        public static String getEmail() {
            return email;
        }

    }
}

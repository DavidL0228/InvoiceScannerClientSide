package controllers;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class client {
    //127.0.0.1
    //10.100.154.32

    private static final String SERVER_URL = "http://127.0.0.1:8081/api";

    private static final HttpClient clientAddr = HttpClient.newHttpClient();

    public client(){
       //SERVER_URL = "http://127.0.0.1:8081/api";
    }


    //this method now uses the jsonObject to avoid needing to save files
    public static void sampleRequest(JsonObject jsonObject, String url) throws IOException, InterruptedException {
        // Build the request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();

        // Send the request and receive the response
        HttpResponse<String> response = clientAddr.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the server response
        System.out.println("Response: " + response.body());
    }

    // function to send img to server for ocr
    public static void makeRequest(HttpClient client, String filePath) throws IOException, InterruptedException {
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("File does not exist: " + filePath);
            return;
        }

        // Generate a boundary string for multipart
        String boundary = "Boundary-" + UUID.randomUUID().toString();

        // End boundary
        String endBoundary = "\r\n--" + boundary + "--\r\n";

        // Create the multipart/form-data body
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n")
                .append("Content-Type: ").append(Files.probeContentType(Paths.get(filePath))).append("\r\n\r\n");

        // Convert the file into a byte array to send it
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Combine everything into one body for the request
        byte[] requestBody = new byte[bodyBuilder.length() + fileContent.length + endBoundary.length()];
        System.arraycopy(bodyBuilder.toString().getBytes(), 0, requestBody, 0, bodyBuilder.length());
        System.arraycopy(fileContent, 0, requestBody, bodyBuilder.length(), fileContent.length);
        System.arraycopy(endBoundary.getBytes(), 0, requestBody, bodyBuilder.length() + fileContent.length, endBoundary.length());

        sendRequest(client, "/upload", boundary, requestBody);
    }

    public static void sendRequest(HttpClient client, String url, String boundary, byte[] requestBody) throws IOException, InterruptedException {
        // Build the request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody)) // Send the multipart form data as byte array
                .build();

        // Send the request and receive the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the server response (OCR result or error)
        System.out.println("Response: " + response.body());
    }
}

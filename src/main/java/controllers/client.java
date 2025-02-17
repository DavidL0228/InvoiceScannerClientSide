package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.UUID;

import org.json.JSONObject;

public class client {
    private static final String SERVER_URL = "http://127.0.0.1:8081/api";
    private final HttpClient httpClient;

    public client() {
        httpClient = HttpClient.newHttpClient();
    }

    /**
     * Sends a JSON message to the server.
     *
     * @param endpoint    the API endpoint (e.g., "/login", "/invoice", etc.)
     * @param jsonMessage the JSON object representing the message
     * @return the server’s response as a String
     */
    public String sendJsonMessage(String endpoint, JSONObject jsonMessage) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonMessage.toString()))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Sends a multipart message, for example to upload a file (such as an invoice scan).
     * Additional JSON data can be provided as form-data fields.
     */
    public String sendMultipartMessage(String endpoint, File file, JSONObject additionalData)
            throws IOException, InterruptedException {
        String boundary = "Boundary-" + UUID.randomUUID().toString();
        String LINE_FEED = "\r\n";

        // Build pre-file parts from additional JSON data (if any)
        StringBuilder multipartBodyBuilder = new StringBuilder();
        if (additionalData != null) {
            for (String key : additionalData.keySet()) {
                multipartBodyBuilder.append("--").append(boundary).append(LINE_FEED);
                multipartBodyBuilder.append("Content-Disposition: form-data; name=\"").append(key).append("\"")
                        .append(LINE_FEED);
                multipartBodyBuilder.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
                multipartBodyBuilder.append(LINE_FEED);
                multipartBodyBuilder.append(additionalData.get(key)).append(LINE_FEED);
            }
        }

        // Append file part
        multipartBodyBuilder.append("--").append(boundary).append(LINE_FEED);
        multipartBodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getName()).append("\"").append(LINE_FEED);
        String mimeType = Files.probeContentType(file.toPath());
        multipartBodyBuilder.append("Content-Type: ").append(mimeType != null ? mimeType : "application/octet-stream")
                .append(LINE_FEED);
        multipartBodyBuilder.append(LINE_FEED);
        byte[] preFileBytes = multipartBodyBuilder.toString().getBytes();

        // Read file bytes
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Append ending boundary
        String ending = LINE_FEED + "--" + boundary + "--" + LINE_FEED;
        byte[] postFileBytes = ending.getBytes();

        // Combine all parts into one request body
        byte[] requestBody = new byte[preFileBytes.length + fileBytes.length + postFileBytes.length];
        System.arraycopy(preFileBytes, 0, requestBody, 0, preFileBytes.length);
        System.arraycopy(fileBytes, 0, requestBody, preFileBytes.length, fileBytes.length);
        System.arraycopy(postFileBytes, 0, requestBody, preFileBytes.length + fileBytes.length, postFileBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + endpoint))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // ––– Convenience Methods for Specific Message Types –––

    public String sendLogin(String username, String password) throws IOException, InterruptedException {
        JSONObject json = new JSONObject();
        json.put("type", MessageType.LOGIN.toString());
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        json.put("data", data);
        return sendJsonMessage("/login", json);
    }

    public String sendInvoice(JSONObject invoiceData) throws IOException, InterruptedException {
        invoiceData.put("type", MessageType.SEND_INVOICE.toString());
        return sendJsonMessage("/invoice", invoiceData);
    }

    public String confirmInvoice(String invoiceId, boolean isConfirmed) throws IOException, InterruptedException {
        JSONObject json = new JSONObject();
        json.put("type", MessageType.CONFIRM_INVOICE.toString());
        JSONObject data = new JSONObject();
        data.put("invoiceId", invoiceId);
        data.put("confirmed", isConfirmed);
        json.put("data", data);
        return sendJsonMessage("/confirm", json);
    }

    // Additional convenience methods can be added here for other message types.
}

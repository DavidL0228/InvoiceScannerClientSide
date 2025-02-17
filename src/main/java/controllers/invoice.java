package controllers;

import org.json.JSONObject;

public class invoice {
    private String id;
    private String vendor;
    private double amount;

    public invoice(String id, String vendor, double amount) {
        this.id = id;
        this.vendor = vendor;
        this.amount = amount;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("invoiceId", id);
        json.put("vendor", vendor);
        json.put("amount", amount);
        return json;
    }

    // Add getters, setters, and additional fields as needed.
}

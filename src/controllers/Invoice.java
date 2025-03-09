package controllers;

public class Invoice {
    private final String invoiceId;
    private final String issueDate;
    private final String dueDate;
    private final String sender;
    private final String status;
    private final double amountDue;
    private final String paymentDate;

    public Invoice(String invoiceId, String issueDate, String dueDate, String sender, double amountDue, String status, String paymentDate) {
        this.invoiceId = invoiceId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.sender = sender;
        this.amountDue = amountDue;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    public String getInvoiceId() { return invoiceId; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }
    public String getSender() { return sender; }
    public double getTotalAmount() { return amountDue; }
    public String getStatus() { return status; }
    public String getPaymentDate() { return paymentDate; }
}

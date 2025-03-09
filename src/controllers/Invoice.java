package controllers;

public class Invoice {
    private final String invoiceId;
    private final String issueDate;
    private final String dueDate;
    private final String sender;
    private final double amountDue;

    public Invoice(String invoiceId, String issueDate, String dueDate, String sender, double amountDue) {
        this.invoiceId = invoiceId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.sender = sender;
        this.amountDue = amountDue;
    }

    public String getInvoiceId() { return invoiceId; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }
    public String getSender() { return sender; }
    public double getAmountDue() { return amountDue; }
}

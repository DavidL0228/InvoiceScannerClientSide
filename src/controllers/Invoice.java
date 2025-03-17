package controllers;

public class Invoice {
    private final String internalId;
    private final String invoiceNumber;
    private final String company;
    private final double subtotal;
    private final double tax;
    private final double totalAmount;
    private final String glAccount;
    private final String email;
    private final String issueDate;
    private final String dueDate;
    private final String datePaid;
    private final String status;
    private final String description;

    public Invoice(String internalId, String invoiceNumber, String company, double subtotal, double tax, double totalAmount,
                   String glAccount, String email, String issueDate, String dueDate, String datePaid, String status, String description) {
        this.internalId = internalId;
        this.invoiceNumber = invoiceNumber;
        this.company = company;
        this.subtotal = subtotal;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.glAccount = glAccount;
        this.email = email;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.datePaid = datePaid;
        this.status = status;
        this.description = description;
    }

    public String getInternalId() { return internalId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getCompany() { return company; }
    public double getSubtotal() { return subtotal; }
    public double getTax() { return tax; }
    public double getTotalAmount() { return totalAmount; }
    public String getGlAccount() { return glAccount; }
    public String getEmail() { return email; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }
    public String getDatePaid() { return datePaid; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
}

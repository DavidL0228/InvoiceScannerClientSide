package controllers;

public class vendor {
    private int id;
    private String name;
    private String email;
    private String address;
    private String defaultGL;

    public vendor(int id, String name, String email, String address, String defaultGL) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.defaultGL = defaultGL;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDefaultGL() { return defaultGL; }

    @Override
    public String toString() {
        return name; // Used by ComboBox display
    }
}

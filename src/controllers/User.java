package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class User {
    private final String firstname;
    private final String lastname;
    private final String username;
    private final String email;
    private final ObservableList<String> roles;

    // Constructor for users with multiple roles
    public User(String first, String last, String usr, String em, List<String> roleList) {
        this.firstname = first;
        this.lastname = last;
        this.username = usr;
        this.email = em;
        this.roles = FXCollections.observableArrayList(roleList);
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public ObservableList<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> newRoles) {
        roles.setAll(newRoles);
    }

    @Override
    public String toString() {
        return firstname + " " + lastname;
    }
}

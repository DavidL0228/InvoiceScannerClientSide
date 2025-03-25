package controllers;

public class User {
    private final String firstname;
    private final String lastname;
    private final String username;
    private final String email;

    public User(String first, String last, String usr, String em){
        firstname = first;
        lastname = last;
        username = usr;
        email = em;
    }

    public String getFirstname(){
        return firstname;
    }

    public String getLastname(){
        return lastname;
    }

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
    }

    @Override
    public String toString(){
        return firstname + " " + lastname;
    }
}

package controllers;

import java.util.ArrayList;

public class Role {

    private static Role selectedRole;
    private String role;

    // no clue how we're keeping track of permission list yet so
    // just for now there's this
    private ArrayList<String> permissions;

    public Role(String r, ArrayList<String> pm){
        role = r;
        permissions = pm;
    }

    public String getRole(){
        return role;
    }

    public ArrayList<String> getPermissions(){
        return permissions;
    }

    public static void setSelectedRole(Role r){
        Role.selectedRole = r;
    }

    public static Role getSelectedRole(){
        return selectedRole;
    }
}

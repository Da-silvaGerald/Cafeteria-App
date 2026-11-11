/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cafeteriaapp;

/**
 *
 * @author DELL
 */
public class User {
    private int id; // User ID 
    private String firstName;
    private String lastName;
    private String email;
    private float accountBalance;
    private String role; // Customer or Manager role

    // Constructor for creating a new user (before they have a Database ID)
    public User(String firstName, String lastName, String email, float accountBalance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accountBalance = accountBalance;
        this.role = "Customer"; // Default to Customer if not specified
    }

    // Constructor used by LoginController when fetching an existing user from the Database
    public User(int id, String firstName, String lastName, String email, float accountBalance, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accountBalance = accountBalance;
        this.role = role;
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(float accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}
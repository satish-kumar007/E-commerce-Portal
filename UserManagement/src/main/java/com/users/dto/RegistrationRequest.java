package com.users.dto;

import jakarta.validation.constraints.*;

public class RegistrationRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username; // optional during registration; can be generated from email

    @Email(message = "Invalid email format")
    private String email; // optional if phone provided

    @Pattern(regexp = "^$|^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber; // optional if email provided

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

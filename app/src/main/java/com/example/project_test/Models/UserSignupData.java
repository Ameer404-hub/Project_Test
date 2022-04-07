package com.example.project_test.Models;

public class UserSignupData {
    String FirstName, LastName, Phone;

    public UserSignupData() {
    }

    public UserSignupData(String firstName, String lastName, String phone) {
        FirstName = firstName;
        LastName = lastName;
        Phone = phone;
    }

    public UserSignupData(String firstName, String lastName) {
        FirstName = firstName;
        LastName = lastName;
    }

    public UserSignupData(String phone) {
        Phone = phone;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}

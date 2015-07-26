package com.teamvh.orbital.athena;

/**
 * Created by Ron on 09-Jul-15.
 */
public class ContactData {
    public String name;

    public String email;

    public String country;

    public String phone;

    public String emStatus = "Pending...";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmStatus() {
        return emStatus;
    }

    public void setEmStatus(String emStatus) {
        this.emStatus = emStatus;
    }
}

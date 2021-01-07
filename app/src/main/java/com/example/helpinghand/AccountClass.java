package com.example.helpinghand;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

import java.io.Serializable;

public class AccountClass implements Serializable {
    String id;
    String fullName;
    String address;
    String accountName;
    String phoneNumber;
    String image;

    public AccountClass(String id) {
        this.id = id;
    }

    public AccountClass(String id, String fullName, String address, String accountName, String phoneNumber, String image) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.accountName = accountName;
        this.phoneNumber = phoneNumber;
        this.image = image;
    }

    public AccountClass(String fullName, String address, String accountName, String phoneNumber, String image) {
        this.fullName = fullName;
        this.address = address;
        this.accountName = accountName;
        this.phoneNumber = phoneNumber;
        this.image = image;
    }

    public AccountClass() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
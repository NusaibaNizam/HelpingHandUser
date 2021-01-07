package com.example.helpinghand;

public class Notification {
    String id;
    String notification;

    public Notification(String id, String notification) {
        this.id = id;
        this.notification = notification;
    }

    public Notification() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }
}

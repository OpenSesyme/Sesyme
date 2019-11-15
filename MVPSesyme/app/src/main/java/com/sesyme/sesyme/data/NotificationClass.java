package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationClass {

    @ServerTimestamp
    private Date time;
    private String elementRef;
    private String notificationText;
    private String type;
    private String sender;
    private String receiver;
    private int seen;

    public NotificationClass() {
    }

    public NotificationClass(String elementRef, String notificationText, String sender,String type, String receiver) {
        this.elementRef = elementRef;
        this.notificationText = notificationText;
        this.sender = sender;
        this.type = type;
        this.time = null;
        this.receiver = receiver;
        this.seen = 0;
    }

    public String getElementRef() {
        return elementRef;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public Date getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getSeen() {
        return seen;
    }
}

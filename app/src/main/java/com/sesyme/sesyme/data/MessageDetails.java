package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MessageDetails {

    @ServerTimestamp
    private Date sentAt;
    private String messageBody;
    private String sender;
    private String attachmentUrl;
    private String attachmentUri;
    private String attType;
    private String messagePath;
    private int numLikes;

    //Default constructor do not delete
    public MessageDetails (){}

    public MessageDetails(String messageBody, String sender, String attachmentUri, String attType) {
        this.sentAt = null;
        this.messageBody = messageBody;
        this.sender = sender;
        this.attachmentUri = attachmentUri;
        this.attachmentUrl = "";
        this.attType = attType;
        this.numLikes = 0;
    }

    public MessageDetails(String messageBody, String sender) {
        this.sentAt = null;
        this.messageBody = messageBody;
        this.sender = sender;
        this.numLikes = 0;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getSender() {
        return sender;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public String getAttType() {
        return attType;
    }

    public String getAttachmentUri() {
        return attachmentUri;
    }

    public String getMessagePath() {
        return messagePath;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setMessagePath(String messagePath) {
        this.messagePath = messagePath;
    }
}

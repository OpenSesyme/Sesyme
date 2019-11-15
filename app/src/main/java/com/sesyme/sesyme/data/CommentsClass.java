package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommentsClass {

    @ServerTimestamp
    private Date sentAt;
    private String author;
    private String comment;
    private int numLikes;

    public CommentsClass() {
    }

    public CommentsClass(String author, String comment) {
        this.sentAt = null;
        this.author = author;
        this.comment = comment;
        this.numLikes = 0;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public String getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }
}

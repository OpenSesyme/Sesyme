package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class WritePostClass {

    @ServerTimestamp
    private Date dateTime;
    private List<String> category;
    private String title;
    private String description;
    private String imageUrl;
    private String attType;
    private String author;
    private String type;
    private String id;
    private Boolean accepted;
    private Boolean edited;
    private int numLikes;
    private int numComments;

    public WritePostClass(){
        //Required empty constructor
    }

    //Constructor for reply
    public WritePostClass(String email, Date date, String type, String description, String imageUrl) {
        this.dateTime = date;
        this.author = email;
        this.description = description;
        this.imageUrl = imageUrl;
        this.type = type;
        this.edited = false;
        this.numLikes = 0;
        this.numComments = 0;
        this.attType = null;
    }

    //Constructor for questions
    public WritePostClass(List<String> category, String title, String email,
                          String type, String description, String imageUrl) {
        this.category = category;
        this.dateTime = null;
        this.title = title;
        this.description = description;
        this.author = email;
        this.type = type;
        this.edited = false;
        this.imageUrl = imageUrl;
        this.numLikes = 0;
        this.numComments = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public int getNumComments() {
        return numComments;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public List<String> getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getType() {
        return type;
    }

    public String getAuthor() {
        return author;
    }

    public String getId(){return id;}

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getEdited() {
        return edited;
    }

    public String getAttType() {
        return attType;
    }
}

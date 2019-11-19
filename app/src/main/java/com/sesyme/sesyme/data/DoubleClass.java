package com.sesyme.sesyme.data;

import java.util.Date;

public class DoubleClass {

    private String creator;
    private String type;
    private String title;
    private String id;
    private Date duration;


    public DoubleClass() {
    }

    public DoubleClass(String creator, String type, String title, String id, Date duration) {
        this.creator = creator;
        this.type = type;
        this.title = title;
        this.id = id;
        this.duration = duration;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public String getCreator() {
        return creator;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }
}

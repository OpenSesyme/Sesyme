package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PollDetails {

    @ServerTimestamp
    private Date createdAt;
    private String clusterId;
    private String creator;
    private String title;
    private List<String> options;
    private List<String> option1;
    private List<String> option2;
    private List<String> option3;
    private List<String> option4;
    private Date duration;

    //Required constructor, Do not delete
    public PollDetails (){}

    public PollDetails(String clusterId, String creator, String title, List<String> options, Date duration) {
        this.createdAt = null;
        this.clusterId = clusterId;
        this.creator = creator;
        this.title = title;
        this.options = options;
        this.duration = duration;
        this.option1 = new ArrayList<>();
        this.option2 = new ArrayList<>();
        this.option3 = new ArrayList<>();
        this.option4 = new ArrayList<>();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getOptions() {
        return options;
    }

    public Date getDuration() {
        return duration;
    }

    public List<String> getOption1() {
        return option1;
    }

    public List<String> getOption2() {
        return option2;
    }

    public List<String> getOption3() {
        return option3;
    }

    public List<String> getOption4() {
        return option4;
    }
}

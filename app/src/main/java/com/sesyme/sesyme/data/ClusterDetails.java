package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClusterDetails {

    @ServerTimestamp
    private Date createdAt;
    private String clusterName;
    private String clusterIcon;
    private String description;
    private List<String> admins;
    private List<String> members;
    private List<String> requests;
    private String privacy;
    private Boolean paid;
    private String id;

    public ClusterDetails(){
        //Default Constructor
    }

    public ClusterDetails(String clusterName, String description, String clusterIcon, List<String> admins,
                          List<String> members, String privacy, String clusterId, Boolean paid) {
        this.createdAt = null;
        this.clusterName = clusterName;
        this.description = description;
        this.clusterIcon = clusterIcon;
        this.admins = admins;
        this.members = members;
        this.privacy = privacy;
        this.requests = new ArrayList<>();
        this.id = clusterId;
        this.paid = paid;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterIcon() {
        return clusterIcon;
    }

    public void setClusterIcon(String clusterIcon) {
        this.clusterIcon = clusterIcon;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPaid() {
        return paid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRequests() {
        return requests;
    }
}

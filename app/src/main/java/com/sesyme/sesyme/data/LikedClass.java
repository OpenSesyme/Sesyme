package com.sesyme.sesyme.data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class LikedClass {

    @ServerTimestamp
    private Date likedAt;
    private String postId;
    private String likedBy;

    public LikedClass(){
        // Required Constructor
    }

    public LikedClass(String postId, String likedBy) {
        this.likedAt = null;
        this.postId = postId;
        this.likedBy = likedBy;
    }

    public String getPostId() {
        return postId;
    }

    public String getLikedBy() {
        return likedBy;
    }

    public Date getLikedAt() {
        return likedAt;
    }
}

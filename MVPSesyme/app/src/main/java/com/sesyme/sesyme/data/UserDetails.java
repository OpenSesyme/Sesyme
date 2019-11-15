package com.sesyme.sesyme.data;

import javax.annotation.Nullable;

public class UserDetails {
    private String fullName;
    private String gender;
    private String course;
    private String dateOfBirth;
    private String profileUrl;
    private String coverUrl;
    private String uID;
    private String regToken;
    private String affiliation;
    private String university;
    private String staffNumber;
    private Boolean paid;

    public UserDetails() {//required empty constructor
    }

    public UserDetails(String fName, String gender, String course, String varsity, String dob,
                       String imageUrl, String coverPicUrl, String uid, String regToken, String affiliation,
                       @Nullable String staffNumber) {
        this.fullName = fName;
        this.gender = gender;
        this.course = course;
        this.dateOfBirth = dob;
        this.profileUrl = imageUrl;
        this.coverUrl = coverPicUrl;
        this.uID = uid;
        this.regToken = regToken;
        this.affiliation = affiliation;
        this.university = varsity;
        this.staffNumber = staffNumber;
        this.paid = false;
    }


    public String getGender() {
        return gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getCourse() {
        return course;
    }

    public String getuID() {
        return uID;
    }

    public String getRegToken() {
        return regToken;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getUniversity() {
        return university;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public Boolean getPaid() {
        return paid;
    }
}

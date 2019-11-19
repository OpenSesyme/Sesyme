package com.sesyme.sesyme.data;



public class ResultsClass {

    private String email;
    private String marks;

    public ResultsClass(String email, String marks) {
        this.email = email;
        this.marks = marks;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }
}

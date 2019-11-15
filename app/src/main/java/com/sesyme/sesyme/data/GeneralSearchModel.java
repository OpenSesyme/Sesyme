package com.sesyme.sesyme.data;

public class GeneralSearchModel {

    private String userName;
    private String content;
    private String path;

    public GeneralSearchModel(String userName, String content, String path) {
        this.userName = userName;
        this.content = content;
        this.path = path;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

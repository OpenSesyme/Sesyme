package com.sesyme.sesyme.data;

public class AddBook {
    private String shelfName;
    private String coverPageUrl;
    private String ISBN;

    public AddBook() {
        //required constructor
    }

    public AddBook(String shelfName, String coverPageUrl, String ISBN) {
        this.shelfName = shelfName;
        this.coverPageUrl = coverPageUrl;
        this.ISBN = ISBN;
    }

    public String getShelfName() {
        return shelfName;
    }

    public String getCoverPageUrl() {
        return coverPageUrl;
    }

    public String getISBN() {
        return ISBN;
    }
}

package com.example.myapplication;

public class UserContent {
    private String publisher;
    private String introduction;

    public UserContent(String publisher, String introduction) {
        this.publisher = publisher;
        this.introduction = introduction;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}

package com.example.myapplication;

import java.util.HashMap;

public class UserContent {
    private String publisher;
    private String introduction;
    private String imageurl;
    private boolean iffollow;

    public UserContent(String publisher, String introduction) {
        this.publisher = publisher;
        this.introduction = introduction;
    }

    public UserContent(HashMap<String, Object> hashMap) {
        this.publisher = (String) hashMap.get("name");
        this.introduction = (String) hashMap.get("description");
        this.imageurl = (String) hashMap.get("image_url");
        this.iffollow = (boolean) hashMap.get("iffollow");
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

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public boolean isIffollow() {
        return iffollow;
    }

    public void setIffollow(boolean iffollow) {
        this.iffollow = iffollow;
    }
}

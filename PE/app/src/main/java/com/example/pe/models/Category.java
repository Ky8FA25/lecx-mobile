package com.example.pe.models;

public class Category {
    private int id;

    String img_url;
    String name;
    String type;

    public Category(){

    }

    public Category(int id, String img_url, String name, String type) {
        this.id = id;
        this.img_url = img_url;
        this.name = name;
        this.type = type;
    }


    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.example.myapplication;

public class Plant {

    private String name;
    private String age;
    private String description;
    private boolean favorite;

    public Plant(String name, String age, String description) {
        this.name = name;
        this.age = age;
        this.description = description;
        this.favorite = false;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
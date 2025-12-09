package com.example.a5minutechallenge;

public class TitleContainer extends ContentContainer {
    private String title;

    public TitleContainer(int id) {
        super(id, Types.TITLE);
    }

    public TitleContainer setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }
}

package com.example.a5minutechallenge;

public class TextContainer extends ContentContainer {
    private String text;

    public TextContainer(int id) {
        super(id, Types.TEXT);
    }

    public TextContainer setText(String text) {
        this.text = text;
        return this;
    }

    public String getText() {
        return text;
    }
}

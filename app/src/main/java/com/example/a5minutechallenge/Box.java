package com.example.a5minutechallenge;

public class Box {
    private int id;
    private enum types{
        TEXT,
        VIDEO,
        QUIZ
    }
    private types type;

    public Box(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public types getType() {return type;}
}

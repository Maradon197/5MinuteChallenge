/** The main class for topics that stores all information every topic must have */
package com.example.a5minutechallenge;

import java.util.ArrayList;

public class Topic {
    private String title;
    private ArrayList<Challenge> challenges;

    public Topic(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String newTitle) {
        title = newTitle;
    }



}

package com.example.a5minutechallenge;

import java.util.ArrayList;

public class Subject {

    private Integer subjectId;
    private String title;
    private String description;
    private ArrayList<Topic> topics;

    public Subject(Integer id) {
        this.subjectId = id;
    }

    public Integer getSubjectId() {
        return subjectId;
    }
    public String getTitle() {
        if(title == null) {
            //Fetch from DB
        }
        return title;
    }

    public Subject setTitle(String newTitle) {
        title = newTitle;
        return this;
    }

    public String getDescription() {
        if(description == null) {
            //Fetch from DB
        }
        return description;
    }

    public Subject setDescription(String newDescription) {
        description = newDescription;
        return this;
    }

    public void setTopics(ArrayList<Topic> newtopics) {
        topics = newtopics;
    }

    public ArrayList<Topic> getTopics() {
        if (topics == null) {
            ArrayList<Topic> topics = new ArrayList<>();
            switch (subjectId) {                                            //but i dont see how it would be possible to initialize everything
                case 1:                                                     //within separate arraylists
                    topics.add(new Topic("Jetpack Compose"));           //anyway this is a list population, will later be prompt engineered
                    topics.add(new Topic("Kotlin"));
                    topics.add(new Topic("Coroutines"));
                    break;
                case 2:
                    topics.add(new Topic("ORDB"));
                    topics.add(new Topic("SQL - Statements"));
                    topics.add(new Topic("Why are you lurking in my code?"));
                    break;
                case 3:
                    topics.add(new Topic("HTML"));
                    topics.add(new Topic("CSS"));
                    topics.add(new Topic("JavaScript"));
                    break;
                default:
                    topics.add(new Topic("Topic 1"));
                    topics.add(new Topic("Topic 2"));
                    topics.add(new Topic("Topic 3"));           //if you can fix this, please do so
                    break;
            }
        }
        return topics;
    }
}

package com.example.a5minutechallenge;

import java.util.ArrayList;

public class Subject {

    private Integer subjectId;
    private String title;
    private String description;
    private ArrayList<Topic> topics;

    public Subject(Integer id, String title, String description) {
        this.subjectId = id;
        this.title = title;
        this.description = description;
    }

    public ArrayList<Topic> getTopics() {return topics;}
    public Integer getSubjectId() {return subjectId;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
}

package com.example.a5minutechallenge;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Subject> subjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        ////////////////////////////////////////////////////////////////////////////////////////////// Data population, prompt engine should change these
        subjectList = new ArrayList<>();
        subjectList.add(new Subject(1, "Jetbrains Academy", "Example Description 1"));
        subjectList.add(new Subject(2, "Databases", "Example Description 2"));
        subjectList.add(new Subject(3, "Programming Languages", "Example Description 3"));
        subjectList.add(new Subject(4, "Other", "Example Description 4"));
        subjectList.add(new Subject(5, "Other", "Example Description 5"));
        subjectList.add(new Subject(6, "Other", "Example Description 6"));
        subjectList.add(new Subject(7, "Jetbrains Academy", "Example Description 1"));
        subjectList.add(new Subject(8, "Databases", "Example Description 2"));
        subjectList.add(new Subject(9, "Jetbrains Academy", "Example Description 1"));
        subjectList.add(new Subject(10, "Databases", "Example Description 2"));
        subjectList.add(new Subject(11, "Programming Languages", "Example Description 3"));
        subjectList.add(new Subject(12, "Other", "Example Description 4"));
        subjectList.add(new Subject(13, "Other", "Example Description 5"));
        //////////////////////////////////////////////////////////////////////////////////////////////


        //subjects erstellen, bearbeiten und entfernen
        //container and extending classes
        //5 minute view erstellen

        //später: Speicherverwaltung
        
        RecyclerView subjectRecyclerView = findViewById(R.id.subject_recycler_view);
        SubjectListManager subjectListAdapter = new SubjectListManager(this, subjectList);
        subjectRecyclerView.setAdapter(subjectListAdapter);
    }
}
package com.example.a5minutechallenge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.common.client.GenerationConfig;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        /*// Use an ExecutorService for the background thread.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Get a Handler that is associated with the main UI thread's Looper.
        Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Client gemini = Client.builder().apiKey(getString(R.string.api_key)).build()) {

                    GenerateContentResponse response =
                            gemini.models.generateContent("gemini-2.5-flash", "How does AI work?", null);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, response.text(), Toast.LENGTH_LONG).show();
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start(); */

        //subjects erstellen, bearbeiten und entfernen
        //container and extending classes
        //5 minute view erstellen

        //später: Speicherverwaltung

        RecyclerView subjectRecyclerView = findViewById(R.id.subject_recycler_view);
        SubjectListManager subjectListAdapter = new SubjectListManager(this, subjectList);
        subjectRecyclerView.setAdapter(subjectListAdapter);
    }
}
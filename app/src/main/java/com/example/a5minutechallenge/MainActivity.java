package com.example.a5minutechallenge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.common.client.GenerationConfig;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Subject> subjectList;
    private SubjectListManager subjectListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        ////////////////////////////////////////////////////////////////////////////////////////////// Data population, prompt engine should change these
        subjectList = new ArrayList<>();
        subjectList.add(new Subject(0).setTitle("Jetbrains Academy").setDescription("Example Description 1"));
        subjectList.add(new Subject(2).setTitle("Databases").setDescription("Example Description 2")); // This was already chained
        subjectList.add(new Subject(3).setTitle("Programming Languages").setDescription("Example Description 3"));
        subjectList.add(new Subject(4).setTitle("Other").setDescription("Example Description 4"));
        subjectList.add(new Subject(5).setTitle("Other").setDescription("Example Description 5"));
        subjectList.add(new Subject(6).setTitle("Other").setDescription("Example Description 6"));
        subjectList.add(new Subject(7).setTitle("Jetbrains Academy").setDescription("Example Description 1"));
        subjectList.add(new Subject(8).setTitle("Databases").setDescription("Example Description 2"));
        subjectList.add(new Subject(9).setTitle("Jetbrains Academy").setDescription("Example Description 1"));
        subjectList.add(new Subject(10).setTitle("Databases").setDescription("Example Description 2"));
        subjectList.add(new Subject(11).setTitle("Programming Languages").setDescription("Example Description 3"));
        subjectList.add(new Subject(12).setTitle("Other").setDescription("Example Description 4"));
        subjectList.add(new Subject(13).setTitle("Other").setDescription("Example Description 5"));
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
        subjectListAdapter = new SubjectListManager(this, subjectList, this::showEditOptionsDialog);
        subjectRecyclerView.setAdapter(subjectListAdapter);

        FloatingActionButton addSubjectFab = findViewById(R.id.add_subject_fab);
        addSubjectFab.setOnClickListener(v -> showAddSubjectDialog());
    }

    private void showAddSubjectDialog() {
        showEditDialog("Add New Subject", "", "Add", (newName) -> {
            int newId = subjectList.size(); // Simple ID generation
            subjectList.add(new Subject(newId).setTitle(newName));
            subjectListAdapter.notifyDataSetChanged();
        });
    }

    private void showEditOptionsDialog(int position) {
        final CharSequence[] options = {"Rename", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Rename")) {
                showRenameDialog(position);
            } else if (options[item].equals("Delete")) {
                showDeleteConfirmationDialog(position);
            }
        });
        builder.show();
    }

    private void showRenameDialog(int position) {
        Subject subject = subjectList.get(position);
        showEditDialog("Rename Subject", subject.getTitle(), "Rename", (newName) -> {
            subject.setTitle(newName);
            subjectListAdapter.notifyItemChanged(position);
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Subject")
                .setMessage("Are you sure you want to delete this subject?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    subjectList.remove(position);
                    subjectListAdapter.notifyItemRemoved(position);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void showEditDialog(String title, String currentName, String positiveButtonText, OnNameEnteredListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        final EditText input = view.findViewById(R.id.edit_text_input);
        input.setText(currentName);
        builder.setView(view);

        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) {
                listener.onNameEntered(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    interface OnNameEnteredListener {
        void onNameEntered(String name);
    }
}
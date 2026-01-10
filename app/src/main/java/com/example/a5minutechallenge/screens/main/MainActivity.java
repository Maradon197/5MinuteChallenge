package com.example.a5minutechallenge.screens.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.ai.client.generativeai.common.client.GenerationConfig;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.genai.Client;
//import com.google.genai.types.GenerateContentResponse;

import java.util.ArrayList;
/*import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;*/

public class MainActivity extends AppCompatActivity {

    private ArrayList<Subject> subjectList;
    private SubjectListManager subjectListAdapter;

    /**
     * Initializes the MainActivity with subject list display and adds a FAB for creating new subjects.
     * Sets up the RecyclerView with SubjectListManager adapter and configures click listeners.
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        ////////////////////////////////////////////////////////////////////////////////////////////// Data population, prompt engine should change these
        subjectList = new ArrayList<>();
        subjectList.add(new Subject(0).setTitle("Jetbrains IDE").setDescription("Master the IDE"));
        subjectList.add(new Subject(2).setTitle("Databases").setDescription("Example Description 2")); // This was already chained
        subjectList.add(new Subject(3).setTitle("Programming Languages").setDescription("Example Description 3"));
        //////////////////////////////////////////////////////////////////////////////////////////////

        /* THIS HAS TO STAY FOR LATER; DO NOT DELETE!!!!!
        // Use an ExecutorService for the background thread.
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



        RecyclerView subjectRecyclerView = findViewById(R.id.subject_recycler_view);
        subjectListAdapter = new SubjectListManager(this, subjectList, this::showEditOptionsDialog);
        subjectRecyclerView.setAdapter(subjectListAdapter);

        FloatingActionButton addSubjectFab = findViewById(R.id.add_subject_button);
        addSubjectFab.setOnClickListener(v -> showAddSubjectDialog());
    }

    /**
     * Displays a dialog to add a new subject to the list.
     * Creates a new Subject with auto-generated ID and adds it to the subject list.
     */
    private void showAddSubjectDialog() {
        showEditDialog(getString(R.string.add_new_subject), "", getString(R.string.add), (newName) -> {
            int newId = subjectList.size(); // Simple ID generation
            subjectList.add(new Subject(newId).setTitle(newName));
            subjectListAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Displays a dialog with edit options (rename/delete) for a subject at the given position.
     * @param position The position of the subject in the list to edit
     */
    private void showEditOptionsDialog(int position) {
        final CharSequence[] options = {getString(R.string.rename), getString(R.string.delete)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_option));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.rename))) {
                showRenameDialog(position);
            } else if (options[item].equals(getString(R.string.delete))) {
                showDeleteConfirmationDialog(position);
            }
        });
        builder.show();
    }

    /**
     * Displays a dialog to rename a subject at the given position.
     * @param position The position of the subject to rename
     */
    private void showRenameDialog(int position) {
        Subject subject = subjectList.get(position);
        showEditDialog(getString(R.string.rename_subject), subject.getTitle(), getString(R.string.rename), (newName) -> {
            subject.setTitle(newName);
            subjectListAdapter.notifyItemChanged(position);
        });
    }

    /**
     * Displays a confirmation dialog before deleting a subject.
     * @param position The position of the subject to delete
     */
    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_subject))
                .setMessage(getString(R.string.confirm_delete_subject))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    subjectList.remove(position);
                    subjectListAdapter.notifyItemRemoved(position);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * Generic dialog for editing text input (add/rename operations).
     * @param title Dialog title
     * @param currentName Current text value to display in the input field
     * @param positiveButtonText Text for the positive action button
     * @param listener Callback listener invoked when user enters a valid name
     */
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
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    interface OnNameEnteredListener {
        void onNameEntered(String name);
    }
}

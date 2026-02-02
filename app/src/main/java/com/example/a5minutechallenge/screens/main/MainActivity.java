package com.example.a5minutechallenge.screens.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.ai.client.generativeai.common.client.GenerationConfig;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.service.SubjectGenerationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.genai.Client;
//import com.google.genai.types.GenerateContentResponse;

import java.util.ArrayList;
/*import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;*/

public class MainActivity extends AppCompatActivity {

    private ArrayList<Subject> subjectList;
    private ArrayList<Integer> subjectIds;
    private SubjectListManager subjectListAdapter;
    private EditText searchBar;
    private SwitchCompat lightModeSwitch;

    /**
     * Initializes the MainActivity with subject list display and adds a FAB for
     * creating new subjects.
     * Sets up the RecyclerView with SubjectListManager adapter and configures click
     * listeners.
     * 
     * @param savedInstanceState Bundle containing the activity's previously saved
     *                           state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_list);

        // Data Population
        subjectList = new ArrayList<>();

        ///
        SubjectGenerationService myService = new SubjectGenerationService();
        subjectIds = myService.getAllSubjectIDs(this);
        ///

        for (int id : subjectIds) {
            Subject subject = new Subject(id);
            subject.getTitle(this); // Load title from subject.json
            subjectList.add(subject);
        }

        RecyclerView subjectRecyclerView = findViewById(R.id.subject_recycler_view);
        subjectListAdapter = new SubjectListManager(this, subjectList, position -> showEditOptionsDialog(position));
        subjectRecyclerView.setAdapter(subjectListAdapter);

        // Setup search bar
        searchBar = findViewById(R.id.subject_search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                subjectListAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton addSubjectFab = findViewById(R.id.add_subject_button);
        addSubjectFab.setOnClickListener(v -> showAddSubjectDialog());

        // Setup light mode toggle (functionality not yet implemented)
        /*lightModeSwitch = findViewById(R.id.light_mode_switch);
        lightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Light mode functionality will be implemented in a future update
            Toast.makeText(this, "Light mode coming soon!", Toast.LENGTH_SHORT).show();
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh subject data when returning to this activity
        // Re-load topics to update gamification data (progress, scores, attempts)
        for (Subject subject : subjectList) {
            subject.reloadTopics(this); // Force reload topics from storage
        }
        subjectListAdapter.notifyDataSetChanged();
    }

    /**
     * Displays a dialog to add a new subject to the list.
     * Creates a new Subject with auto-generated ID and adds it to the subject list.
     */
    private void showAddSubjectDialog() {
        showEditDialog(getString(R.string.add_new_subject), "", getString(R.string.add), (newName) -> {
            int newId = subjectList.size(); // Simple ID generation
            Subject newSubject = new Subject(newId).setTitle(newName);
            // persist subject metadata to internal storage
            newSubject.saveMetaToStorage(MainActivity.this);
            subjectList.add(newSubject);
            subjectListAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Displays a dialog with edit options (rename/delete) for a subject at the
     * given position.
     * 
     * @param position The position of the subject in the list to edit
     */
    private void showEditOptionsDialog(int position) {
        final CharSequence[] options = { getString(R.string.rename), getString(R.string.delete) };

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
     * 
     * @param position The position of the subject to rename
     */
    private void showRenameDialog(int position) {
        Subject subject = subjectList.get(position);
        showEditDialog(getString(R.string.rename_subject), subject.getTitle(), getString(R.string.rename),
                (newName) -> {
                    subject.setTitle(newName);
                    // persist updated title
                    subject.saveMetaToStorage(MainActivity.this);
                    subjectListAdapter.notifyItemChanged(position);
                });
    }

    /**
     * Displays a confirmation dialog before deleting a subject.
     * 
     * @param position The position of the subject to delete
     */
    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_subject))
                .setMessage(getString(R.string.confirm_delete_subject))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    Subject subject = subjectList.get(position);
                    // delete folder and contents
                    subject.deleteSubjectStorage(MainActivity.this);
                    subjectList.remove(position);
                    subjectListAdapter.notifyItemRemoved(position);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * Generic dialog for editing text input (add/rename operations).
     * 
     * @param title              Dialog title
     * @param currentName        Current text value to display in the input field
     * @param positiveButtonText Text for the positive action button
     * @param listener           Callback listener invoked when user enters a valid
     *                           name
     */
    private void showEditDialog(String title, String currentName, String positiveButtonText,
            OnNameEnteredListener listener) {
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

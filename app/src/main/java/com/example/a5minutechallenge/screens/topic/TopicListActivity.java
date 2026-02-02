/**
 * Dis
 */
package com.example.a5minutechallenge.screens.topic;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.example.a5minutechallenge.screens.challenge.ChallengeListActivity;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.screens.storage.StorageActivity;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TopicListActivity extends AppCompatActivity {

    private ArrayList<Topic> topicList;
    private TopicListAdapter adapter;
    private Subject subject;
    private EditText searchBar;
    private FrameLayout emptyTopicsOverlay;

    /**
     * Initializes the TopicListActivity activity with a topic list for the selected subject.
     * Sets up the ListView with topics and configures FABs for adding topics and accessing storage.
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_list);

        String subjectTitle = getIntent().getStringExtra("SUBJECT_TITLE");
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);

        TextView titleTextView = findViewById(R.id.subject_screen_title);
        titleTextView.setText(subjectTitle);

        ListView topicListView = findViewById(R.id.topic_list_view);

        subject = new Subject(subjectId);

        /// ///////////////////////////////////////////////////////
        subject.reloadTopics(getApplicationContext());
        topicList = subject.getTopics(getApplicationContext());//this list contains items, which is validated

        if (topicList == null) {
            Toast.makeText(this, "No topics found for this subject.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Topics located successfully.", Toast.LENGTH_SHORT).show();
        }

        adapter = new TopicListAdapter(this, topicList);
        topicListView.setAdapter(adapter);

        if (topicListView.getAdapter() == null) {
            Toast.makeText(this, "Adapter not found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Adapter found", Toast.LENGTH_SHORT).show();
        }

        adapter.updateTopics(topicList);//maybe some data is lost here
        Toast.makeText(this, "List updated", Toast.LENGTH_SHORT).show();
        /// ///////////////////////////////////////////////////////


        searchBar = findViewById(R.id.topic_search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        topicListView.setOnItemClickListener((parent, view, position, id) -> {
            Topic selectedTopic = adapter.getItem(position);
            Intent intent = new Intent(TopicListActivity.this, ChallengeListActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.putExtra("TOPIC_NAME", selectedTopic.getTitle());
            startActivity(intent);
        });

        topicListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showEditOptionsDialog(position);
            return true;
        });

        FloatingActionButton storageFab = findViewById(R.id.storage_fab);
        storageFab.setOnClickListener(v -> {
            Intent intent = new Intent(TopicListActivity.this, StorageActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            startActivity(intent);
        });

        // Setup empty state overlay
        emptyTopicsOverlay = findViewById(R.id.empty_topics_overlay);
        updateIsEmptyOverlay();
    }

    /**
     * Shows or hides the empty state overlay based on whether there are topics.
     */
    private void updateIsEmptyOverlay() {
        if (topicList.isEmpty()) {//always seemed to return true; gone smh but i didnt fix it
            emptyTopicsOverlay.setVisibility(View.VISIBLE);
            //Toast.makeText(this, topicList.get(0).getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            emptyTopicsOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the topic list when returning to this activity
        // Use reloadTopics to force refresh from storage and get updated progress
        topicList.clear();
        topicList.addAll(subject.reloadTopics(getApplicationContext()));
        adapter.updateTopics(topicList);
        updateIsEmptyOverlay();
    }


    /**
     * Displays a dialog with edit options (rename/delete) for a topic at the given position.
     * @param position The position of the topic in the list to edit
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
     * Displays a dialog to rename a topic at the given position.
     * @param position The position of the topic to rename
     */
    private void showRenameDialog(int position) {
        Topic topic = adapter.getItem(position);
        showEditDialog(getString(R.string.rename_topic), topic.getTitle(), getString(R.string.rename), (newName) -> {
            topic.setTitle(newName);
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Displays a confirmation dialog before deleting a topic.
     * @param position The position of the topic to delete
     */
    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_topic))
                .setMessage(getString(R.string.confirm_delete_topic))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    adapter.remove(adapter.getItem(position));
                    adapter.notifyDataSetChanged();
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

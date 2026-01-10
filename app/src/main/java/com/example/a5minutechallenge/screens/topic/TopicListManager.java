/** Main class for the subject specific screen*/
package com.example.a5minutechallenge.screens.topic;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

public class TopicListManager extends AppCompatActivity {

    private ArrayList<Topic> topicList;
    private TopicListAdapter adapter;
    private Subject subject;

    /**
     * Initializes the TopicListManager activity with a topic list for the selected subject.
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
        topicList = subject.getTopics();
        adapter = new TopicListAdapter(this, topicList);
        topicListView.setAdapter(adapter);

        topicListView.setOnItemClickListener((parent, view, position, id) -> {
            Topic selectedTopic = topicList.get(position);
            Intent intent = new Intent(TopicListManager.this, ChallengeListActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.putExtra("TOPIC_NAME", selectedTopic.getTitle());
            startActivity(intent);
        });

        topicListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showEditOptionsDialog(position);
            return true;
        });

        FloatingActionButton addTopicFab = findViewById(R.id.add_topic_fab);
        addTopicFab.setOnClickListener(v -> showAddTopicDialog());

        FloatingActionButton storageFab = findViewById(R.id.storage_fab);
        storageFab.setOnClickListener(v -> {
            Intent intent = new Intent(TopicListManager.this, StorageActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            startActivity(intent);
        });
    }

    /**
     * Displays a dialog to add a new topic to the current subject.
     */
    private void showAddTopicDialog() {
        showEditDialog(getString(R.string.add_new_topic), "", getString(R.string.add), (newName) -> {
            subject.addTopic(newName);
            adapter.notifyDataSetChanged();
        });
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
        Topic topic = topicList.get(position);
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
                    topicList.remove(position);
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

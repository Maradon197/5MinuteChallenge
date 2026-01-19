package com.example.a5minutechallenge.screens.storage;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;
import com.example.a5minutechallenge.datawrapper.topic.StorageListItem;
import com.example.a5minutechallenge.screens.topic.TopicListActivity;
import com.example.a5minutechallenge.service.SubjectGenerationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.util.ArrayList;

public class StorageActivity extends AppCompatActivity {

    private ArrayList<StorageListItem> storageList;
    private StorageListManager storageListAdapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Subject subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_screen);

        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);
        subject = new Subject(subjectId);
        storageList = subject.getStorageItems();

        RecyclerView storageRecyclerView = findViewById(R.id.storage_recycler_view);
        storageListAdapter = new StorageListManager(this, storageList, this::showEditOptionsDialog);
        storageRecyclerView.setAdapter(storageListAdapter);

        ArrayList<SubjectFile> subjectfiles = subject.getFiles(getApplicationContext());
        for(SubjectFile currentFile: subjectfiles) {
            subject.addStorageItem(currentFile);
            storageListAdapter.notifyDataSetChanged();
        }

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String fileName = getFileName(uri);
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                if (inputStream != null) {
                                    SubjectFile savedFile = subject.saveFileToStorage(this, inputStream, fileName);
                                    if (savedFile != null) {
                                        subject.addStorageItem(savedFile);
                                        storageListAdapter.notifyDataSetChanged();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        FloatingActionButton addFileFab = findViewById(R.id.add_file_fab);
        addFileFab.setOnClickListener(v -> openFilePicker());

        FloatingActionButton genContentFab = findViewById(R.id.gen_content_fab);
        genContentFab.setOnClickListener(v -> {
            // Show a loading indicator and disable the button
            Toast.makeText(StorageActivity.this, "Generating content, this may take a moment...", Toast.LENGTH_LONG).show();
            genContentFab.setEnabled(false);

            // Instantiate and call the asynchronous service
            SubjectGenerationService generationService = new SubjectGenerationService();
            generationService.generateContent(subject, StorageActivity.this, new SubjectGenerationService.GenerationCallback() {
                @Override
                public void onGenerationSuccess(Subject updatedSubject) {
                    // This is executed on the main thread
                    genContentFab.setEnabled(true);
                    Toast.makeText(StorageActivity.this, "Content generated successfully!", Toast.LENGTH_LONG).show();

                    // Navigate to TopicListActivity to show the generated topics
                    Intent intent = new Intent(StorageActivity.this, TopicListActivity.class);
                    intent.putExtra("SUBJECT_ID", updatedSubject.getSubjectId());
                    intent.putExtra("SUBJECT_TITLE", updatedSubject.getTitle(StorageActivity.this));
                    startActivity(intent);
                    finish(); // Finish this activity
                }

                @Override
                public void onGenerationFailure(Exception e) {
                    // This is executed on the main thread
                    genContentFab.setEnabled(true);
                    Log.e("GenerationFailed", "Error generating content", e);
                    new AlertDialog.Builder(StorageActivity.this)
                            .setTitle("Generation Failed")
                            .setMessage("Could not generate content. Please check your connection and API key. Error: " + e.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            });
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

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

    private void showRenameDialog(int position) {
        StorageListItem item = storageList.get(position);
        showEditDialog(getString(R.string.rename_file), item.getTitle(), getString(R.string.rename), (newName) -> {
            ArrayList<SubjectFile> files = subject.getFiles(this);
            for (SubjectFile file : files) {
                if (file.getFileName().equals(item.getTitle())) {
                    if (subject.renameFile(file, newName)) {
                        item.setTitle(newName);
                        storageListAdapter.notifyItemChanged(position);
                    }
                    break;
                }
            }
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_file))
                .setMessage(getString(R.string.confirm_delete_file))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    StorageListItem item = storageList.get(position);
                    ArrayList<SubjectFile> files = subject.getFiles(this);
                    for (SubjectFile file : files) {
                        if (file.getFileName().equals(item.getTitle())) {
                            subject.deleteFile(file);
                            break;
                        }
                    }
                    storageList.remove(position);
                    storageListAdapter.notifyItemRemoved(position);
                })
                .setNegativeButton(getString(R.string.cancel), null)
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
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    interface OnNameEnteredListener {
        void onNameEntered(String name);
    }
}

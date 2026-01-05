package com.example.a5minutechallenge;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.util.ArrayList;

public class StorageActivity extends AppCompatActivity {

    private ArrayList<StorageItem> storageList;
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

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String fileName = getFileName(uri);
                            try {
                                // Get input stream from URI
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                if (inputStream != null) {
                                    // Save file to internal storage via Subject
                                    SubjectFile savedFile = subject.saveFileToStorage(this, inputStream, fileName);
                                    if (savedFile != null) {
                                        // File saved successfully and is accessible via subject.getFiles()
                                        // Add to display list
                                        subject.addStorageItem(fileName);
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
        StorageItem item = storageList.get(position);
        showEditDialog(getString(R.string.rename_file), item.getTitle(), getString(R.string.rename), (newName) -> {
            // Find and rename the corresponding file in storage
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
                    StorageItem item = storageList.get(position);
                    // Find and delete the corresponding file from storage
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

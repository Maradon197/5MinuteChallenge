package com.example.a5minutechallenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class StorageActivity extends AppCompatActivity {

    private ArrayList<StorageItem> storageList;
    private StorageListManager storageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_screen);

        storageList = new ArrayList<>();
        // Example data
        storageList.add(new StorageItem("File 1"));
        storageList.add(new StorageItem("File 2"));
        storageList.add(new StorageItem("File 3"));

        RecyclerView storageRecyclerView = findViewById(R.id.storage_recycler_view);
        storageListAdapter = new StorageListManager(this, storageList, this::showEditOptionsDialog);
        storageRecyclerView.setAdapter(storageListAdapter);

        FloatingActionButton addFileFab = findViewById(R.id.add_file_fab);
        addFileFab.setOnClickListener(v -> showAddFileDialog());
    }

    private void showAddFileDialog() {
        showEditDialog(getString(R.string.add_new_file), "", getString(R.string.add), (newName) -> {
            storageList.add(new StorageItem(newName));
            storageListAdapter.notifyDataSetChanged();
        });
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
            item.setTitle(newName);
            storageListAdapter.notifyItemChanged(position);
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_file))
                .setMessage(getString(R.string.confirm_delete_file))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
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

/** RecyclerView that displays Subjects in subject_list.xml */
package com.example.a5minutechallenge.screens.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.screens.topic.TopicListManager;

import java.util.ArrayList;

public class SubjectListManager extends RecyclerView.Adapter<SubjectListManager.ViewHolder> {

    private final Context context;
    private final ArrayList<Subject> subjects;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    /**
     * Constructs a SubjectListManager with context, subject data, and click listeners.
     * @param context The application context
     * @param subjects The list of subjects to display
     * @param longClickListener Callback for long-click events on items
     */
    public SubjectListManager(Context context, ArrayList<Subject> subjects, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.subjects = subjects;
        this.longClickListener = longClickListener;
    }

    /**
     * Creates a new ViewHolder for subject list items by inflating the layout.
     * @param parent The parent ViewGroup
     * @param viewType The view type of the new View
     * @return A new ViewHolder instance
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subject_list_element, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds subject data to the ViewHolder at the specified position.
     * Sets up click and long-click listeners for navigation and edit operations.
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.title.setText(subject.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Subject ID: " + subject.getSubjectId(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, TopicListManager.class);
            intent.putExtra("SUBJECT_ID", subject.getSubjectId());
            intent.putExtra("SUBJECT_TITLE", subject.getTitle());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.subject_list_title);
        }
    }
}

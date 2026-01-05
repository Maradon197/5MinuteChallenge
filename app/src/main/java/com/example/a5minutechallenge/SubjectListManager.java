/** RecyclerView that displays Subjects in subject_list.xml */
package com.example.a5minutechallenge;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubjectListManager extends RecyclerView.Adapter<SubjectListManager.ViewHolder> {

    private final Context context;
    private final ArrayList<Subject> subjects;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public SubjectListManager(Context context, ArrayList<Subject> subjects, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.subjects = subjects;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subject_list_element, parent, false);
        return new ViewHolder(view);
    }

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

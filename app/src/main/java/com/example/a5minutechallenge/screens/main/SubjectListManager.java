/** RecyclerView that displays Subjects in subject_list.xml */
package com.example.a5minutechallenge.screens.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.screens.topic.TopicListActivity;

import java.util.ArrayList;
import java.util.List;

public class SubjectListManager extends RecyclerView.Adapter<SubjectListManager.ViewHolder> {

    private final Context context;
    private final ArrayList<Subject> allSubjects;
    private List<Subject> filteredSubjects;
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
        this.allSubjects = subjects;
        this.filteredSubjects = new ArrayList<>(subjects);
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
        Subject subject = filteredSubjects.get(position);
        holder.title.setText(subject.getTitle());

        // Load topics data for progress display
        subject.getTopics(context);

        // Update topics preview
        String topicsPreview = subject.getTopicsPreview(context, 3);
        if (topicsPreview != null && !topicsPreview.isEmpty()) {
            holder.topicsPreview.setText(topicsPreview);
            holder.topicsPreview.setVisibility(View.VISIBLE);
        } else {
            holder.topicsPreview.setVisibility(View.GONE);
        }

        // Update status indicator
        if (subject.isCompleted()) {
            holder.status.setText("✓");
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.success));
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.success));
            holder.card.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
        } else if (subject.getTotalAttempts() > 0) {
            holder.status.setText("◐");
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.warning));
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.warning));
            holder.card.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
        } else {
            holder.status.setText("○");
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary));
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.card_stroke));
            holder.card.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
        }

        // Update score and attempts
        holder.bestScore.setText("Best: " + subject.getBestScore());
        holder.attempts.setText("Attempts: " + subject.getTotalAttempts());

        // Update progress
        holder.progress.setProgress(subject.getProgressPercentage());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Subject ID: " + subject.getSubjectId(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, TopicListActivity.class);
            intent.putExtra("SUBJECT_ID", subject.getSubjectId());
            intent.putExtra("SUBJECT_TITLE", subject.getTitle());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int actualPosition = allSubjects.indexOf(subject);
                longClickListener.onItemLongClick(actualPosition);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredSubjects.size();
    }

    /**
     * Filters the subject list based on the search query.
     * @param query The search query string
     */
    public void filter(String query) {
        filteredSubjects.clear();
        if (query == null || query.isEmpty()) {
            filteredSubjects.addAll(allSubjects);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Subject subject : allSubjects) {
                String title = subject.getTitle();
                if (title != null && title.toLowerCase().contains(lowerQuery)) {
                    filteredSubjects.add(subject);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Resets filter to show all subjects.
     */
    public void resetFilter() {
        filteredSubjects.clear();
        filteredSubjects.addAll(allSubjects);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView card;
        TextView title;
        TextView topicsPreview;
        TextView status;
        TextView bestScore;
        TextView attempts;
        ProgressBar progress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.subject_card);
            title = itemView.findViewById(R.id.subject_list_title);
            topicsPreview = itemView.findViewById(R.id.subject_topics_preview);
            status = itemView.findViewById(R.id.subject_status);
            bestScore = itemView.findViewById(R.id.subject_best_score);
            attempts = itemView.findViewById(R.id.subject_attempts);
            progress = itemView.findViewById(R.id.subject_progress);
        }
    }
}

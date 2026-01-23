/** ArrayAdapter to display topic containers in the subject specific screen **/
package com.example.a5minutechallenge.screens.topic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TopicListAdapter extends ArrayAdapter<Topic> {

    private List<Topic> myTopics;

    public TopicListAdapter(@NonNull Context context, List<Topic> topics) {
        super(context, 0, new ArrayList<>(topics));
        this.myTopics = new ArrayList<>(topics);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.topic_list_element, parent, false);
        }

        Topic topic = getItem(position);

        MaterialCardView card = convertView.findViewById(R.id.topic_card);
        TextView titleTextView = convertView.findViewById(R.id.topic_title);
        TextView statusTextView = convertView.findViewById(R.id.topic_status);
        TextView bestScoreTextView = convertView.findViewById(R.id.topic_best_score);
        TextView attemptsTextView = convertView.findViewById(R.id.topic_attempts);
        LinearLayout dotProgressLayout = convertView.findViewById(R.id.topic_dot_progress);

        titleTextView.setText(topic.getTitle());

        //Update status indicator with color-coded outline
        if (topic.isCompleted()) {
            statusTextView.setText("✓");
            statusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.success));
            card.setStrokeColor(ContextCompat.getColor(getContext(), R.color.success));
        } else if (topic.getTotalAttempts() > 0) {
            statusTextView.setText("◐");
            statusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.warning));
            card.setStrokeColor(ContextCompat.getColor(getContext(), R.color.warning));
        } else {
            statusTextView.setText("○");
            statusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_tertiary));
            card.setStrokeColor(ContextCompat.getColor(getContext(), R.color.card_stroke));
        }

        // Update score and attempts
        bestScoreTextView.setText("Best: " + topic.getBestScore());
        attemptsTextView.setText("Attempts: " + topic.getTotalAttempts());

        // Update dot progress indicator
        updateDotProgress(dotProgressLayout, topic);

        return convertView;
    }

    /**
     * Creates a dot-by-dot progress indicator for the topic.
     */
    private void updateDotProgress(LinearLayout layout, Topic topic) {
        layout.removeAllViews();
        ArrayList<Challenge> challenges = topic.getChallenges();

        if (challenges.isEmpty()) {
            return;
        }

        int dotSize = getContext().getResources().getDimensionPixelSize(R.dimen.icon_size_sm);
        int dotMargin = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_xs);

        // Limit dots to max 10 for visual clarity
        int maxDots = Math.min(challenges.size(), 10);

        for (int i = 0; i < maxDots; i++) {
            TextView dot = new TextView(getContext());
            dot.setText("●");
            dot.setTextSize(12);

            if (challenges.get(i).isCompleted()) {
                dot.setTextColor(ContextCompat.getColor(getContext(), R.color.success));
            } else {
                dot.setTextColor(ContextCompat.getColor(getContext(), R.color.text_disabled));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);

            layout.addView(dot);
        }

        //If there are more challenges than dots, show "..."
        if (challenges.size() > maxDots) {
            TextView ellipsis = new TextView(getContext());
            ellipsis.setText("…");
            ellipsis.setTextSize(12);
            ellipsis.setTextColor(ContextCompat.getColor(getContext(), R.color.text_tertiary));
            layout.addView(ellipsis);
        }
    }

    /**
     * Filters the topic list based on the search query.
     * @param query The search query string
     */
    public void filter(String query) {
        clear();
        if (query == null || query.isEmpty()) {
            addAll(myTopics);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Topic topic : myTopics) {
                String title = topic.getTitle();
                if (title != null && title.toLowerCase().contains(lowerQuery)) {
                    add(topic);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Resets filter to show all topics.
     */
    public void resetFilter() {
        clear();
        addAll(myTopics);
        notifyDataSetChanged();
    }

    public void updateTopics(List<Topic> newTopics) {
        myTopics.clear();
        clear(); //Also clear the ArrayAdapter's internal list
        if (newTopics != null) {
            myTopics.addAll(newTopics);
            addAll(newTopics); // Sync the ArrayAdapter's internal list
        }
        notifyDataSetChanged();
    }
}

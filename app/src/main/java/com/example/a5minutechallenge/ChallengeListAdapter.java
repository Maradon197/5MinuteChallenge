/** ArrayAdapter to display challenges in the challenge list screen */
package com.example.a5minutechallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ChallengeListAdapter extends ArrayAdapter<Challenge> {

    public ChallengeListAdapter(@NonNull Context context, List<Challenge> challenges) {
        super(context, 0, challenges);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.challenge_list_element, parent, false);
        }

        Challenge challenge = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.challenge_title);
        TextView descriptionTextView = convertView.findViewById(R.id.challenge_description);
        TextView statusTextView = convertView.findViewById(R.id.challenge_status);
        TextView bestScoreTextView = convertView.findViewById(R.id.challenge_best_score);
        TextView attemptsTextView = convertView.findViewById(R.id.challenge_attempts);
        ProgressBar progressBar = convertView.findViewById(R.id.challenge_progress);

        titleTextView.setText(challenge.getTitle());
        descriptionTextView.setText(challenge.getDescription());
        
        // Update status indicator
        if (challenge.isCompleted()) {
            statusTextView.setText("✓");
            statusTextView.setTextColor(getContext().getColor(R.color.success));
        } else {
            statusTextView.setText("○");
            statusTextView.setTextColor(getContext().getColor(R.color.text_tertiary));
        }
        
        // Update score and attempts
        bestScoreTextView.setText("Best: " + challenge.getBestScore());
        attemptsTextView.setText("Attempts: " + challenge.getAttempts());
        
        // Update progress
        progressBar.setProgress(challenge.getProgressPercentage());

        return convertView;
    }
}

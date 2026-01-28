/** Activity that displays a list of 5-minute challenges for a selected topic */
package com.example.a5minutechallenge.screens.challenge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.example.a5minutechallenge.screens.fiveminute.FiveMinuteActivity;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.challenge.Challenge;

import java.util.ArrayList;

public class ChallengeListActivity extends AppCompatActivity {

    private static final int COUNTDOWN_ANIMATION_DELAY_MS = 500;
    private static final int MAX_TITLE_LENGTH = 30;
    private static final int TRUNCATED_TITLE_LENGTH = MAX_TITLE_LENGTH - 3; // Reserve 3 chars for "..."

    private ArrayList<Challenge> challengeList;
    private ChallengeListAdapter adapter;
    private Subject subject;
    private String topicName;
    private int subjectId;
    private AlertDialog countdownDialog;
    private Handler countdownHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_list);

        topicName = getIntent().getStringExtra("TOPIC_NAME");
        subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);

        TextView titleTextView = findViewById(R.id.challenge_screen_title);
        titleTextView.setText("5-Minute Challenges");

        TextView topicNameTextView = findViewById(R.id.topic_name_display);
        topicNameTextView.setText(topicName);

        ListView challengeListView = findViewById(R.id.challenge_list_view);

        // Load challenges for this topic
        challengeList = loadChallengesForTopic(this, topicName, subjectId);
        adapter = new ChallengeListAdapter(this, challengeList);
        challengeListView.setAdapter(adapter);

        challengeListView.setOnItemClickListener((parent, view, position, id) -> {
            Challenge selectedChallenge = challengeList.get(position);
            showCountdownDialog(selectedChallenge, position);
        });
    }

    /**
     * Loads challenges for a given topic.
     * In a real application, this would load from a database or API.
     * 
     * @param topicName The name of the topic
     * @return ArrayList of challenges
     */
    private ArrayList<Challenge> loadChallengesForTopic(Context context, String topicName, int subjectId) {

        subject = new Subject(subjectId);
        ArrayList<Topic> topics = subject.getTopics(context);

        // match topic name because i was too dumb to implement an id in topic.java
        int topicId = 0;
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).getTitle().equals(topicName)) {
                topicId = i;
                break;
            }
        }

        //
        ArrayList<Challenge> challenges = topics.get(topicId).getChallenges();
        //
        return challenges;
    }

    /**
     * Shows a countdown dialog before starting the challenge.
     * 
     * @param challenge The challenge to start
     * @param position  The position of the challenge in the list
     */
    private void showCountdownDialog(Challenge challenge, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_countdown, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextView countdownNumber = dialogView.findViewById(R.id.countdown_number);
        TextView countdownTitle = dialogView.findViewById(R.id.countdown_title);

        // Truncate title if too long to prevent layout issues
        String title = challenge.getTitle();
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, TRUNCATED_TITLE_LENGTH) + "...";
        }
        countdownTitle.setText(title);

        countdownDialog = builder.create();
        countdownDialog.show();

        // Animate countdown
        countdownHandler = new Handler(Looper.getMainLooper());
        startCountdown(countdownNumber, 3, () -> {
            countdownDialog.dismiss();
            startChallenge(challenge, position);
        });
    }

    /**
     * Performs the countdown animation.
     * 
     * @param textView   The TextView displaying the countdown
     * @param count      Current countdown number
     * @param onComplete Callback to run when countdown completes
     */
    private void startCountdown(TextView textView, int count, Runnable onComplete) {
        if (count > 0) {
            textView.setText(String.valueOf(count));

            // Scale in animation
            Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
            textView.startAnimation(scaleIn);

            countdownHandler.postDelayed(() -> {
                // Scale out animation
                Animation scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out);
                scaleOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        startCountdown(textView, count - 1, onComplete);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                textView.startAnimation(scaleOut);
            }, COUNTDOWN_ANIMATION_DELAY_MS);
        } else {
            // Show "GO!" before starting
            textView.setText("GO!");
            textView.setTextColor(getColor(R.color.success));
            Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
            textView.startAnimation(scaleIn);

            countdownHandler.postDelayed(onComplete, COUNTDOWN_ANIMATION_DELAY_MS);
        }
    }

    /**
     * Starts the selected challenge.
     * 
     * @param challenge The challenge to start
     * @param position  The position of the challenge in the list
     */
    private void startChallenge(Challenge challenge, int position) {
        if (countdownDialog != null) {
            countdownDialog.dismiss();
            countdownDialog = null;
        }

        challenge.incrementAttempts();
        // Save the attempt to storage
        if (subject != null) {
            subject.saveToStorage(getApplicationContext());
        }
        adapter.notifyDataSetChanged();

        Intent intent = new Intent(ChallengeListActivity.this, FiveMinuteActivity.class);
        intent.putExtra("SUBJECT_ID", subjectId);
        intent.putExtra("TOPIC_NAME", topicName);
        intent.putExtra("CHALLENGE_POSITION", position);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list in case challenges were completed
        if (adapter != null) {
            // Reload challenges from storage to get updated progress
            ArrayList<Challenge> refreshedChallenges = loadChallengesForTopic(this, topicName, subjectId);
            challengeList.clear();
            challengeList.addAll(refreshedChallenges);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownDialog != null && countdownDialog.isShowing()) {
            countdownDialog.dismiss();
        }
        countdownDialog = null;
        if (countdownHandler != null) {
            countdownHandler.removeCallbacksAndMessages(null);
        }
    }
}

/** Activity displayed when a lesson is completed successfully with results and next topic option.
 **/
package com.example.a5minutechallenge.screens.challenge;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.example.a5minutechallenge.screens.topic.TopicListActivity;

import java.util.ArrayList;

public class LessonOverActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView scoreText;
    private TextView basePointsText;
    private TextView accuracyText;
    private TextView streakText;
    private TextView timeBonusText;
    private TextView accuracyBonusText;
    private Button continueButton;
    private Button backToTopicsButton;

    private int totalScore;
    private int basePoints;
    private int timeBonus;
    private int accuracyBonus;
    private Handler animationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_over);

        animationHandler = new Handler(Looper.getMainLooper());
        initViews();
        saveProgress();
        setupButtons();
        animateSettlementScore();
    }

    /**
     * Initializes all view references.
     */
    private void initViews() {
        titleText = findViewById(R.id.title_text);
        scoreText = findViewById(R.id.score_text);
        basePointsText = findViewById(R.id.base_points_text);
        accuracyText = findViewById(R.id.accuracy_text);
        streakText = findViewById(R.id.streak_text);
        timeBonusText = findViewById(R.id.time_bonus_text);
        accuracyBonusText = findViewById(R.id.accuracy_bonus_text);
        continueButton = findViewById(R.id.continue_button);
        backToTopicsButton = findViewById(R.id.back_to_topics_button);

        // Get values from intent
        totalScore = getIntent().getIntExtra("TOTAL_SCORE", 0);
        timeBonus = getIntent().getIntExtra("TIME_BONUS", 0);
        accuracyBonus = getIntent().getIntExtra("ACCURACY_BONUS", 0);
        // Calculate base points (total minus bonuses)
        basePoints = totalScore - timeBonus - accuracyBonus;
        if (basePoints < 0)
            basePoints = 0;
    }

    /**
     * Saves progress to storage.
     */
    private void saveProgress() {
        double accuracy = getIntent().getDoubleExtra("ACCURACY", 0.0);
        int maxStreak = getIntent().getIntExtra("MAX_STREAK", 0);
        String topicName = getIntent().getStringExtra("TOPIC_NAME");
        int challengePosition = getIntent().getIntExtra("CHALLENGE_POSITION", -1);
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);

        // Set text values for display
        accuracyText.setText(String.format("Accuracy: %.0f%%", accuracy * 100));
        streakText.setText(String.format("Max Streak: %d", maxStreak));

        // Save progress to storage
        if (topicName != null && challengePosition >= 0) {
            Log.d("LessonOverActivity", "Saving progress for Subject: " + subjectId + ", Topic: " + topicName
                    + ", Challenge Pos: " + challengePosition + ", Score: " + totalScore);
            Subject subject = new Subject(subjectId);
            ArrayList<Topic> topics = subject.getTopics(getApplicationContext());

            boolean topicFound = false;
            // Find topic by name (primary key)
            for (Topic topic : topics) {
                if (topicName.equals(topic.getTitle())) {
                    topicFound = true;
                    ArrayList<Challenge> challenges = topic.getChallenges();
                    if (challenges != null && challengePosition < challenges.size()) {
                        Challenge c = challenges.get(challengePosition);
                        c.setCompleted(true);
                        c.setBestScore(totalScore);
                        Log.i("LessonOverActivity",
                                "Updated challenge '" + c.getTitle() + "' with best score: " + c.getBestScore());
                        // Note: Attempts are incremented when challenge is started in
                        // ChallengeListActivity
                    } else {
                        Log.w("LessonOverActivity",
                                "Challenge position " + challengePosition + " out of bounds or challenges null");
                    }
                    break;
                }
            }

            if (!topicFound) {
                Log.w("LessonOverActivity", "Topic '" + topicName + "' not found in subject " + subjectId);
            }

            // Save updated progress
            boolean saved = subject.saveToStorage(getApplicationContext());
            Log.i("LessonOverActivity", "Progress save result: " + saved);
        } else {
            Log.w("LessonOverActivity",
                    "Missing data for saving: topicName=" + topicName + ", challengePosition=" + challengePosition);
        }
    }

    /**
     * Animates the settlement score like a "settlement" - shows base then adds
     * bonuses.
     */
    private void animateSettlementScore() {
        // Initial state - hide everything
        scoreText.setAlpha(0f);
        if (basePointsText != null)
            basePointsText.setAlpha(0f);
        accuracyText.setAlpha(0f);
        streakText.setAlpha(0f);
        timeBonusText.setAlpha(0f);
        timeBonusText.setVisibility(View.GONE);
        accuracyBonusText.setAlpha(0f);
        accuracyBonusText.setVisibility(View.GONE);

        // Fade in title
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        titleText.startAnimation(fadeIn);

        // Step 1: Show base points and count up
        animationHandler.postDelayed(() -> {
            if (basePointsText != null) {
                basePointsText.setVisibility(View.VISIBLE);
                basePointsText.setText("Base Points: 0");
                basePointsText.animate().alpha(1f).setDuration(300).start();
            }

            scoreText.setVisibility(View.VISIBLE);
            animateCountUp(scoreText, 0, basePoints, "Score: %d", 600, () -> {
                // Step 2: Show accuracy and streak
                animationHandler.postDelayed(() -> {
                    accuracyText.animate().alpha(1f).setDuration(300).start();
                    streakText.animate().alpha(1f).setDuration(300).start();

                    // Step 3: Add time bonus if any
                    if (timeBonus > 0) {
                        animationHandler.postDelayed(() -> {
                            timeBonusText.setText(getString(R.string.time_bonus, timeBonus));
                            timeBonusText.setVisibility(View.VISIBLE);
                            timeBonusText.animate().alpha(1f).setDuration(300).start();

                            // Count up score with time bonus
                            animateCountUp(scoreText, basePoints, basePoints + timeBonus, "Score: %d", 400, () -> {
                                // Step 4: Add accuracy bonus if any
                                if (accuracyBonus > 0) {
                                    animationHandler.postDelayed(() -> {
                                        accuracyBonusText.setText(getString(R.string.accuracy_bonus, accuracyBonus));
                                        accuracyBonusText.setVisibility(View.VISIBLE);
                                        accuracyBonusText.animate().alpha(1f).setDuration(300).start();

                                        // Count up final score
                                        animateCountUp(scoreText, basePoints + timeBonus, totalScore, "Score: %d", 400,
                                                null);
                                    }, 300);
                                }
                            });
                        }, 400);
                    } else if (accuracyBonus > 0) {
                        // No time bonus, just accuracy bonus
                        animationHandler.postDelayed(() -> {
                            accuracyBonusText.setText(getString(R.string.accuracy_bonus, accuracyBonus));
                            accuracyBonusText.setVisibility(View.VISIBLE);
                            accuracyBonusText.animate().alpha(1f).setDuration(300).start();

                            animateCountUp(scoreText, basePoints, totalScore, "Score: %d", 400, null);
                        }, 400);
                    }
                }, 300);
            });
        }, 500);
    }

    /**
     * Animates a count-up effect on a TextView.
     */
    private void animateCountUp(TextView textView, int from, int to, String format, long duration,
            Runnable onComplete) {
        textView.setAlpha(1f);
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.format(format, value));
        });
        if (onComplete != null) {
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    onComplete.run();
                }
            });
        }
        animator.start();
    }

    /**
     * Sets up button click listeners for navigation.
     */
    private void setupButtons() {
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);

        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(LessonOverActivity.this, TopicListActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        backToTopicsButton.setOnClickListener(v -> {
            Intent intent = new Intent(LessonOverActivity.this, TopicListActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }
}

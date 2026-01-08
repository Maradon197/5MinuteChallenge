/** Activity displayed when time runs out in a lesson with score and learning summary.
 **/
package com.example.a5minutechallenge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TimeOverActivity extends AppCompatActivity {
    
    private TextView scoreText;
    private TextView accuracyText;
    private TextView streakText;
    private TextView encouragementText;
    private Button tryAgainButton;
    private Button backToTopicsButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_over);
        
        initViews();
        displayResults();
        setupButtons();
        animateResults();
    }
    
    /**
     * Initializes all view references.
     */
    private void initViews() {
        scoreText = findViewById(R.id.score_text);
        accuracyText = findViewById(R.id.accuracy_text);
        streakText = findViewById(R.id.streak_text);
        encouragementText = findViewById(R.id.encouragement_text);
        tryAgainButton = findViewById(R.id.try_again_button);
        backToTopicsButton = findViewById(R.id.back_to_topics_button);
    }
    
    /**
     * Displays the final results from intent extras.
     */
    private void displayResults() {
        int finalScore = getIntent().getIntExtra("FINAL_SCORE", 0);
        double accuracy = getIntent().getDoubleExtra("ACCURACY", 0.0);
        int maxStreak = getIntent().getIntExtra("MAX_STREAK", 0);
        int correctAnswers = getIntent().getIntExtra("CORRECT_ANSWERS", 0);
        
        scoreText.setText(getString(R.string.final_score, finalScore));
        accuracyText.setText(String.format("Accuracy: %.0f%%", accuracy * 100));
        streakText.setText(String.format("Max Streak: %d", maxStreak));
        
        // Display encouragement based on performance
        if (accuracy >= 0.8) {
            encouragementText.setText("Great job! Keep up the excellent work!");
        } else if (accuracy >= 0.5) {
            encouragementText.setText("Good effort! Practice makes perfect.");
        } else {
            encouragementText.setText("Keep learning! Every attempt makes you better.");
        }
    }
    
    /**
     * Sets up button click listeners for navigation.
     */
    private void setupButtons() {
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);
        String topicName = getIntent().getStringExtra("TOPIC_NAME");
        
        tryAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(TimeOverActivity.this, FiveMinuteActivity.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.putExtra("TOPIC_NAME", topicName);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        backToTopicsButton.setOnClickListener(v -> {
            Intent intent = new Intent(TimeOverActivity.this, TopicListManager.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Animates the result elements for visual appeal.
     */
    private void animateResults() {
        Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        // Shake the score to draw attention
        scoreText.startAnimation(shakeAnim);
        
        accuracyText.setVisibility(View.INVISIBLE);
        accuracyText.postDelayed(() -> {
            accuracyText.setVisibility(View.VISIBLE);
            accuracyText.startAnimation(fadeIn);
        }, 300);
        
        streakText.setVisibility(View.INVISIBLE);
        streakText.postDelayed(() -> {
            streakText.setVisibility(View.VISIBLE);
            streakText.startAnimation(fadeIn);
        }, 600);
        
        encouragementText.setAlpha(0f);
        encouragementText.postDelayed(() -> {
            encouragementText.animate().alpha(1f).setDuration(400).start();
        }, 900);
    }
}

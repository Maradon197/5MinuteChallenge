/** Activity displayed when a lesson is completed successfully with results and next topic option.
 **/
package com.example.a5minutechallenge.screens.challenge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.screens.topic.TopicListManager;

public class LessonOverActivity extends AppCompatActivity {
    
    private TextView scoreText;
    private TextView accuracyText;
    private TextView streakText;
    private TextView timeBonusText;
    private TextView accuracyBonusText;
    private Button continueButton;
    private Button backToTopicsButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_over);
        
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
        timeBonusText = findViewById(R.id.time_bonus_text);
        accuracyBonusText = findViewById(R.id.accuracy_bonus_text);
        continueButton = findViewById(R.id.continue_button);
        backToTopicsButton = findViewById(R.id.back_to_topics_button);
    }
    
    /**
     * Displays the lesson results from intent extras.
     */
    private void displayResults() {
        int totalScore = getIntent().getIntExtra("TOTAL_SCORE", 0);
        double accuracy = getIntent().getDoubleExtra("ACCURACY", 0.0);
        int maxStreak = getIntent().getIntExtra("MAX_STREAK", 0);
        int timeBonus = getIntent().getIntExtra("TIME_BONUS", 0);
        int accuracyBonus = getIntent().getIntExtra("ACCURACY_BONUS", 0);
        String topicName = getIntent().getStringExtra("TOPIC_NAME");
        int challengePosition = getIntent().getIntExtra("CHALLENGE_POSITION", -1);
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);

        /*
        Subject subject = new Subject(subjectId);
        Challenge c = subject.getTopics(getApplicationContext()).get(index).getChallenges().get(challengePosition);
        c.setCompleted(true);
        subject.saveToStorage(getApplicationContext());

        // Update challenge completion if this was from a challenge
        if (topicName != null && challengePosition >= 0) {
            ChallengeManager.getInstance().updateChallengeCompletion(topicName, challengePosition, totalScore);
        }*/
        
        scoreText.setText(getString(R.string.your_score, totalScore));
        accuracyText.setText(String.format("Accuracy: %.0f%%", accuracy * 100));
        streakText.setText(String.format("Max Streak: %d", maxStreak));
        
        if (timeBonus > 0) {
            timeBonusText.setText(getString(R.string.time_bonus, timeBonus));
            timeBonusText.setVisibility(View.VISIBLE);
        }
        
        if (accuracyBonus > 0) {
            accuracyBonusText.setText(getString(R.string.accuracy_bonus, accuracyBonus));
            accuracyBonusText.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Sets up button click listeners for navigation.
     */
    private void setupButtons() {
        int subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);
        
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(LessonOverActivity.this, TopicListManager.class);
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        backToTopicsButton.setOnClickListener(v -> {
            Intent intent = new Intent(LessonOverActivity.this, TopicListManager.class);
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
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        scoreText.startAnimation(scaleIn);
        
        accuracyText.setVisibility(View.INVISIBLE);
        accuracyText.postDelayed(() -> {
            accuracyText.setVisibility(View.VISIBLE);
            accuracyText.startAnimation(fadeIn);
        }, 200);
        
        streakText.setVisibility(View.INVISIBLE);
        streakText.postDelayed(() -> {
            streakText.setVisibility(View.VISIBLE);
            streakText.startAnimation(fadeIn);
        }, 400);
        
        if (timeBonusText.getVisibility() == View.VISIBLE) {
            timeBonusText.setAlpha(0f);
            timeBonusText.postDelayed(() -> {
                timeBonusText.animate().alpha(1f).setDuration(300).start();
            }, 600);
        }
        
        if (accuracyBonusText.getVisibility() == View.VISIBLE) {
            accuracyBonusText.setAlpha(0f);
            accuracyBonusText.postDelayed(() -> {
                accuracyBonusText.animate().alpha(1f).setDuration(300).start();
            }, 800);
        }
    }
}

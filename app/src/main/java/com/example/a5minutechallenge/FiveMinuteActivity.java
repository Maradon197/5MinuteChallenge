/** Activity for the 5-minute-screen opened by the user. Calls ContentContainerAdapter
 *  for content creation and display. Includes gamification with timer, scoring, and swipe gestures.
 */
package com.example.a5minutechallenge;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FiveMinuteActivity extends AppCompatActivity implements TimerManager.TimerListener {

    private ListView contentListView;
    private TextView timerText;
    private TextView scoreDisplay;
    private TextView streakIndicator;
    private TextView scorePopup;
    private ProgressBar timerProgress;
    
    private ScoreManager scoreManager;
    private TimerManager timerManager;
    private GestureDetector gestureDetector;
    
    private List<ContentContainer> contentContainers;
    private ContentContainerAdapter adapter;
    private int currentContainerIndex = 0;
    
    private String topicName;
    private int subjectId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_minute);

        initViews();
        initData();
        initGamification();
        setupGestureDetector();
        loadContent();
    }

    /**
     * Initializes all view references.
     */
    private void initViews() {
        contentListView = findViewById(R.id.box_list);
        timerText = findViewById(R.id.timer_text);
        scoreDisplay = findViewById(R.id.score_display);
        streakIndicator = findViewById(R.id.streak_indicator);
        scorePopup = findViewById(R.id.score_popup);
        timerProgress = findViewById(R.id.timer_progress);
    }

    /**
     * Initializes data from intent extras.
     */
    private void initData() {
        topicName = getIntent().getStringExtra("TOPIC_NAME");
        subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);
        
        if (topicName == null) {
            topicName = "Default Topic";
        }
    }

    /**
     * Initializes the gamification systems (scoring and timer).
     */
    private void initGamification() {
        scoreManager = new ScoreManager();
        timerManager = new TimerManager(this);
        
        updateScoreDisplay();
        updateTimerDisplay();
        
        // Start timer after a short delay
        timerText.postDelayed(() -> timerManager.start(), 1000);
    }

    /**
     * Sets up the swipe gesture detector for container progression.
     */
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                
                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) {
                            // Swipe up
                            onSwipeUp();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        contentListView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    /**
     * Loads and displays the content containers for this lesson.
     */
    private void loadContent() {
        // Load content from ContentLoader based on subject and topic
        contentContainers = (List<ContentContainer>) ContentLoader.loadContent(subjectId, topicName);

        adapter = new ContentContainerAdapter(this, contentContainers);
        contentListView.setAdapter(adapter);
    }

    /**
     * Handles swipe up gesture to progress to next container.
     */
    private void onSwipeUp() {
        if (currentContainerIndex < contentContainers.size() - 1) {
            currentContainerIndex++;
            
            // Animate current container sliding up
            View currentView = contentListView.getChildAt(0);
            if (currentView != null) {
                Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_out);
                slideUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        contentListView.smoothScrollToPosition(currentContainerIndex);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                currentView.startAnimation(slideUp);
            }
            
            checkLessonComplete();
        }
    }

    /**
     * Records a correct answer and updates score with animations.
     * @param answerTimeMs Time taken to answer in milliseconds
     */
    public void onCorrectAnswer(long answerTimeMs) {
        int points = scoreManager.recordCorrectAnswer(answerTimeMs);
        timerManager.addCorrectAnswerBonus();
        
        updateScoreDisplay();
        showScorePopup(points, scoreManager.wasQuickAnswer(answerTimeMs));
        updateStreakDisplay();
        
        // Play bounce animation on score display
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        scoreDisplay.startAnimation(bounce);
    }

    /**
     * Records an incorrect answer and updates UI.
     */
    public void onIncorrectAnswer() {
        scoreManager.recordIncorrectAnswer();
        updateStreakDisplay();
        
        // Shake animation for incorrect answer
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        scoreDisplay.startAnimation(shake);
    }

    /**
     * Displays a score popup animation with the points earned.
     * @param points Points to display
     * @param isQuickAnswer Whether this was a quick answer bonus
     */
    private void showScorePopup(int points, boolean isQuickAnswer) {
        String text = "+" + points;
        if (isQuickAnswer) {
            text += " ⚡";
        }
        
        scorePopup.setText(text);
        scorePopup.setVisibility(View.VISIBLE);
        
        Animation popupAnim = AnimationUtils.loadAnimation(this, R.anim.score_popup);
        popupAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                scorePopup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        scorePopup.startAnimation(popupAnim);
    }

    /**
     * Updates the score display text.
     */
    private void updateScoreDisplay() {
        scoreDisplay.setText("Score: " + scoreManager.getTotalScore());
    }

    /**
     * Updates the timer display text.
     */
    private void updateTimerDisplay() {
        timerText.setText(timerManager.getFormattedTime());
    }

    /**
     * Updates the streak indicator visibility and text.
     */
    private void updateStreakDisplay() {
        int streak = scoreManager.getCurrentStreak();
        if (streak > 1) {
            streakIndicator.setText("🔥 Streak: " + streak);
            streakIndicator.setVisibility(View.VISIBLE);
            
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            streakIndicator.startAnimation(pulse);
        } else {
            streakIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * Checks if the lesson is complete and navigates to result screen.
     */
    private void checkLessonComplete() {
        if (currentContainerIndex >= contentContainers.size() - 1) {
            timerManager.stop();
            
            int timeBonus = scoreManager.addTimeBonus(timerManager.getRemainingTimeSeconds());
            int accuracyBonus = scoreManager.addAccuracyBonus();
            
            Intent intent = new Intent(this, LessonOverActivity.class);
            intent.putExtra("TOTAL_SCORE", scoreManager.getTotalScore());
            intent.putExtra("ACCURACY", scoreManager.getAccuracy());
            intent.putExtra("MAX_STREAK", scoreManager.getMaxStreak());
            intent.putExtra("TIME_BONUS", timeBonus);
            intent.putExtra("ACCURACY_BONUS", accuracyBonus);
            intent.putExtra("SUBJECT_ID", subjectId);
            startActivity(intent);
            finish();
        }
    }

    // TimerListener implementation

    @Override
    public void onTimeUpdate(int remainingSeconds, float percentage) {
        runOnUiThread(() -> {
            updateTimerDisplay();
            timerProgress.setProgress((int) (percentage * 100));
            
            // Change timer color based on remaining time
            if (timerManager.isCritical()) {
                timerText.setTextColor(getColor(R.color.timer_low));
                
                // Pulse animation when critical
                if (remainingSeconds % 2 == 0 && timerText.getAnimation() == null) {
                    Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
                    timerText.startAnimation(pulse);
                }
            } else if (timerManager.isWarning()) {
                timerText.setTextColor(getColor(R.color.timer_medium));
            } else {
                timerText.setTextColor(getColor(R.color.text_primary));
            }
        });
    }

    @Override
    public void onTimeOver() {
        runOnUiThread(() -> {
            Intent intent = new Intent(this, TimeOverActivity.class);
            intent.putExtra("FINAL_SCORE", scoreManager.getTotalScore());
            intent.putExtra("ACCURACY", scoreManager.getAccuracy());
            intent.putExtra("MAX_STREAK", scoreManager.getMaxStreak());
            intent.putExtra("CORRECT_ANSWERS", scoreManager.getCorrectAnswers());
            intent.putExtra("SUBJECT_ID", subjectId);
            intent.putExtra("TOPIC_NAME", topicName);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onTimerStateChanged(boolean isRunning) {
        // Could add pause/resume UI feedback here
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerManager != null && timerManager.getRemainingTimeSeconds() > 0) {
            timerManager.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerManager != null) {
            timerManager.stop();
        }
    }
}

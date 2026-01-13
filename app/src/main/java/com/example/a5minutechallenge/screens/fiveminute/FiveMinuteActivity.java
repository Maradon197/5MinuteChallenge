/** Activity for the 5-minute-screen opened by the user.
 *  Displays content containers one at a time with a preview of the next container.
 *  Includes gamification with timer, scoring, and swipe gestures.
 */
package com.example.a5minutechallenge.screens.fiveminute;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a5minutechallenge.screens.challenge.LessonOverActivity;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.screens.challenge.TimerManager;
import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.screens.challenge.ScoreManager;

import java.util.List;

public class FiveMinuteActivity extends AppCompatActivity implements TimerManager.TimerListener {

    private static final long MIN_ANSWER_TIME_MS = 1000;

    private FrameLayout currentContainerLayout;
    private FrameLayout nextContainerLayout;
    private FrameLayout nextContainerPreview;
    private Button checkButton;
    private TextView timerText;
    private TextView scoreDisplay;
    private TextView streakIndicator;
    private TextView scorePopup;
    private ProgressBar timerProgress;
    
    private ScoreManager scoreManager;
    private TimerManager timerManager;
    private GestureDetector gestureDetector;
    
    private List<ContentContainer> contentContainers;
    private int currentContainerIndex = 0;
    private long lastQuestionStartTime;
    
    private String topicName;
    private int subjectId;
    private int challengePosition = -1; // Position of challenge in the list, -1 if not from challenge list

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_minute);

        initViews();//realize all view instances needed for the screen
        initData();//get topic, subject name from intent
        initGamification();//init scoremanager and timermanager, start timer
        initGestureDetector();//init swipe detection
        loadContent();//display first container
    }
    
    private void initViews() {
        currentContainerLayout = findViewById(R.id.current_container);//nested layout in five_minute_activity.xml
        nextContainerLayout = findViewById(R.id.next_container);
        nextContainerPreview = findViewById(R.id.next_container_preview);
        checkButton = findViewById(R.id.check_button);
        timerText = findViewById(R.id.timer_text);
        scoreDisplay = findViewById(R.id.score_display);
        streakIndicator = findViewById(R.id.streak_indicator);
        scorePopup = findViewById(R.id.score_popup);
        timerProgress = findViewById(R.id.timer_progress);
        
        checkButton.setOnClickListener(v -> onCheckButtonClicked());
    }

    private void initData() {
        topicName = getIntent().getStringExtra("TOPIC_NAME");
        subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);
        challengePosition = getIntent().getIntExtra("CHALLENGE_POSITION", -1);

        if (topicName == null) {
            topicName = "Default Topic";
        }
        
        // Initialize question start time to current time
        lastQuestionStartTime = System.currentTimeMillis();
    }
    
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
    private void initGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                //vertical?
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

            //GestureDetector needs onDown: true to track gesture
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        View contentArea = findViewById(R.id.content_container_area);
        contentArea.setOnTouchListener((v, event) -> {
            // Pass event to detector and return true to "consume" the touch
            return gestureDetector.onTouchEvent(event);
        });
    }


    /**
     * Loads and displays the content containers for this lesson.
     */
    private void loadContent() {
        // Load content from ContentLoader based on subject topic and challenge
        contentContainers = ContentLoader.loadContent(subjectId, topicName, challengePosition);

        if (contentContainers != null && !contentContainers.isEmpty()) {
            displayContainer(currentContainerIndex);
        }
    }

    /**
     * Displays the content container at the specified index.
     * @param index The index of the container to display
     */
    private void displayContainer(int index) {
        if (index < 0 || index >= contentContainers.size()) {
            return;
        }
        
        ContentContainer container = contentContainers.get(index);
        
        // Track when question containers are displayed
        lastQuestionStartTime = System.currentTimeMillis();
        
        // Inflate and display vurrent container
        currentContainerLayout.removeAllViews();
        View containerView = inflateContainerView(container);
        currentContainerLayout.addView(containerView);
        
        /*// Display preview of next container if available, broken rn
        if (index + 1 < contentContainers.size()) {
            ContentContainer nextContainer = contentContainers.get(index + 1);
            nextContainerLayout.removeAllViews();
            View nextView = inflateContainerView(nextContainer);
            nextContainerLayout.addView(nextView);
            nextContainerPreview.setVisibility(View.VISIBLE);
        } else {
            nextContainerPreview.setVisibility(View.GONE);
        }*/
        
        // Update button text based on container type -> "check" or "next", hoping to make this unnecessary
        updateCheckButtonText(container);
    }

    /**
     * Inflates the appropriate view for a content container.
     * Since its an outragingly long switchcase, i moved it to a new class
     * @param container The content container
     * @return The inflated view
     */
    private View inflateContainerView(ContentContainer container) {
        ContainerInflater containerInflater = new ContainerInflater();
        return containerInflater.inflateContainerView(container, this);
    }

    /**
     * Updates the check button text based on container type.
     *  Im also hoping to make this redundant by automatically checking responses
     *  and swiping up to progress to the next container
     * @param container The current content container
     */
    private void updateCheckButtonText(ContentContainer container) {
        switch (container.getType()) {
            case TEXT:
            case TITLE:
            case VIDEO:
                checkButton.setText(R.string.next_question);
                break;
            default:
                checkButton.setText(R.string.check_answer);
                break;
        }
    }


    //HERE IS THE CHECK IF ANSWER CORRECT PART
    /**
     * Handles the check button click event.
     */
    private void onCheckButtonClicked() {
        ContentContainer currentContainer = contentContainers.get(currentContainerIndex);
        
        // For interactive containers (quizzes, etc.), validate the answer
        boolean isCorrect = false;
        boolean needsValidation = false;
        
        switch (currentContainer.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
            case REVERSE_QUIZ:
            case FILL_IN_THE_GAPS:
            case SORTING_TASK:
            case ERROR_SPOTTING:
            case WIRE_CONNECTING:
            case QUIZ:
                // These containers need validation
                needsValidation = true;
                // NOTE: Simplified for demonstration purposes
                // this must check user selections:
                // - For MULTIPLE_CHOICE_QUIZ: verify selected option matches correct answer
                // - For FILL_IN_THE_GAPS: validate filled words against correct words
                // - For SORTING_TASK: check if items are in correct order
                // - etc.
                // For now, treating all answers as correct to demonstrate scoring animations
                isCorrect = true;//THIS IS WRONG
                break;
            default:
                // TEXT, TITLE, VIDEO, RECAP don't need validation
                needsValidation = false;
                break;
        }
        
        if (needsValidation) {
            // Calculate answer time since question was displayed
            //minimum value to ensure reasonable scoring
            long minAnswerTimeMs = Math.max(MIN_ANSWER_TIME_MS, System.currentTimeMillis() - lastQuestionStartTime);


            onAnswer(minAnswerTimeMs, isCorrect);
        }
        
        progressToNextContainer();
    }

    /**
     * Forwards swipe up gesture to handle answer checking and progression.
     * This now matches the behavior of clicking the check button.
     */
    private void onSwipeUp() {
        onCheckButtonClicked();
    }

    /**
     * Progresses to the next content container with animation.
     * Calls checkLessonComplete() if there are no more containers
     */
    private void progressToNextContainer() {
        if (currentContainerIndex < contentContainers.size() - 1) {//if there are more containers to show
            // Animate current container sliding up and fading out
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_out);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    checkButton.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    currentContainerIndex++;
                    displayContainer(currentContainerIndex);
                    
                    // Animate new container sliding into our DMs
                    Animation slideIn = AnimationUtils.loadAnimation(FiveMinuteActivity.this, R.anim.slide_up_in);
                    currentContainerLayout.startAnimation(slideIn);
                    checkButton.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            currentContainerLayout.startAnimation(slideUp);
        } else { //last container, finish lesson
            checkLessonComplete(false);
        }
    }

    /**
     * Records an answer and updates the score display.
     * @param answerTimeMs Time taken to answer in milliseconds
     * @param isCorrect Wether the answer was correct or not
     */
    public void onAnswer(long answerTimeMs, boolean isCorrect) {
        if (isCorrect) {
            int points = scoreManager.recordCorrectAnswer(answerTimeMs);
            timerManager.addCorrectAnswerBonus();

            updateScoreDisplay();
            showScorePopup(points, scoreManager.wasQuickAnswer(answerTimeMs));
            updateStreakDisplay();

            // Play bounce animation on score display
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            scoreDisplay.startAnimation(bounce);
        }
        else {// !isCorrect
            scoreManager.recordIncorrectAnswer();
            updateStreakDisplay();

            // Shake animation for incorrect answer
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            scoreDisplay.startAnimation(shake);
        }
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

        //animate score popup
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
    private void updateScoreDisplay() {scoreDisplay.setText("Score: " + scoreManager.getTotalScore());}

    /**
     * Updates the timer display text.
     */
    private void updateTimerDisplay() {timerText.setText(timerManager.getFormattedTime());}

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
    private void checkLessonComplete(boolean timeOver) {
        timerManager.stop();
        
        int timeBonus = scoreManager.addTimeBonus(timerManager.getRemainingTimeSeconds());
        int accuracyBonus = scoreManager.addAccuracyBonus();
        if(timeOver) {
            Toast.makeText(this, "Time's up!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Lesson Complete!", Toast.LENGTH_SHORT).show();
        }
        
        Intent intent = new Intent(this, LessonOverActivity.class);
        intent.putExtra("TOTAL_SCORE", scoreManager.getTotalScore());
        intent.putExtra("ACCURACY", scoreManager.getAccuracy());
        intent.putExtra("MAX_STREAK", scoreManager.getMaxStreak());
        intent.putExtra("TIME_BONUS", timeBonus);
        intent.putExtra("ACCURACY_BONUS", accuracyBonus);
        intent.putExtra("SUBJECT_ID", subjectId);
        intent.putExtra("TOPIC_NAME", topicName);
        intent.putExtra("CHALLENGE_POSITION", challengePosition);
        startActivity(intent);
        finish();
    }

    // TimerListener
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
        checkLessonComplete(true);
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

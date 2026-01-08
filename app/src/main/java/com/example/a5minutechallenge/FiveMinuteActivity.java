/** Activity for the 5-minute-screen opened by the user.
 *  Displays content containers one at a time with a preview of the next container.
 *  Includes gamification with timer, scoring, and swipe gestures.
 */
package com.example.a5minutechallenge;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FiveMinuteActivity extends AppCompatActivity implements TimerManager.TimerListener {

    private FrameLayout currentContainerLayout;
    //private FrameLayout nextContainerLayout;
    //private FrameLayout nextContainerPreview;
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
    
    private void initViews() {
        currentContainerLayout = findViewById(R.id.current_container);
        //nextContainerLayout = findViewById(R.id.next_container);
        //nextContainerPreview = findViewById(R.id.next_container_preview);
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
        
        if (topicName == null) {
            topicName = "Default Topic";
        }
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

        View contentArea = findViewById(R.id.content_container_area);
        contentArea.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    /**
     * Loads and displays the content containers for this lesson.
     */
    private void loadContent() {
        // Load content from ContentLoader based on subject and topic
        contentContainers = ContentLoader.loadContent(subjectId, topicName);
        
        // Display the first container
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
        
        // Inflate and display the current container
        currentContainerLayout.removeAllViews();
        View containerView = inflateContainerView(container);
        currentContainerLayout.addView(containerView);
        
        /*// Display preview of next container if available
        if (index + 1 < contentContainers.size()) {
            ContentContainer nextContainer = contentContainers.get(index + 1);
            nextContainerLayout.removeAllViews();
            View nextView = inflateContainerView(nextContainer);
            nextContainerLayout.addView(nextView);
            nextContainerPreview.setVisibility(View.VISIBLE);
        } else {
            nextContainerPreview.setVisibility(View.GONE);
        }*/
        
        // Update button text based on container type
        updateCheckButtonText(container);
    }

    /**
     * Inflates the appropriate view for a content container.
     * @param container The content container
     * @return The inflated view
     */
    private View inflateContainerView(ContentContainer container) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        
        switch (container.getType()) {
            case TITLE:
                // view = inflater.inflate(R.layout.title_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.title_container, null);
                TextView titleView = view.findViewById(R.id.title_text);
                TitleContainer titleContainer = (TitleContainer) container;
                titleView.setText(titleContainer.getTitle());
                break;
            case TEXT:
                // view = inflater.inflate(R.layout.text_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.text_container, null);
                TextView textView = view.findViewById(R.id.text_content);
                TextContainer textContainer = (TextContainer) container;
                textView.setText(textContainer.getText());
                break;
            case MULTIPLE_CHOICE_QUIZ:
                // view = inflater.inflate(R.layout.multiple_choice_quiz_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.multiple_choice_quiz_container, null);
                TextView questionText = view.findViewById(R.id.question_text);
                MultipleChoiceQuizContainer mcqContainer = (MultipleChoiceQuizContainer) container;
                questionText.setText(mcqContainer.getQuestion());
                break;
            case REVERSE_QUIZ:
                // view = inflater.inflate(R.layout.reverse_quiz_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.reverse_quiz_container, null);
                TextView answerText = view.findViewById(R.id.answer_text);
                ReverseQuizContainer reverseQuizContainer = (ReverseQuizContainer) container;
                answerText.setText(reverseQuizContainer.getAnswer());
                break;
            case WIRE_CONNECTING:
                // view = inflater.inflate(R.layout.wire_connecting_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.wire_connecting_container, null);
                TextView wireInstructions = view.findViewById(R.id.instructions_text);
                WireConnectingContainer wireContainer = (WireConnectingContainer) container;
                wireInstructions.setText(wireContainer.getInstructions());
                break;
            case FILL_IN_THE_GAPS:
                // view = inflater.inflate(R.layout.fill_in_gaps_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.fill_in_gaps_container, null);
                TextView gapsText = view.findViewById(R.id.text_with_gaps);
                FillInTheGapsContainer gapsContainer = (FillInTheGapsContainer) container;
                gapsText.setText(gapsContainer.getDisplayText());
                break;
            case SORTING_TASK:
                // view = inflater.inflate(R.layout.sorting_task_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.sorting_task_container, null);
                TextView sortInstructions = view.findViewById(R.id.instructions_text);
                SortingTaskContainer sortContainer = (SortingTaskContainer) container;
                sortInstructions.setText(sortContainer.getInstructions());
                break;
            case ERROR_SPOTTING:
                // view = inflater.inflate(R.layout.error_spotting_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.error_spotting_container, null);
                TextView errorInstructions = view.findViewById(R.id.instructions_text);
                ErrorSpottingContainer errorContainer = (ErrorSpottingContainer) container;
                errorInstructions.setText(errorContainer.getInstructions());
                break;
            case RECAP:
                // view = inflater.inflate(R.layout.recap_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.recap_container, null);
                TextView recapTitle = view.findViewById(R.id.recap_title);
                RecapContainer recapContainer = (RecapContainer) container;
                recapTitle.setText(recapContainer.getRecapTitle());
                break;
            case VIDEO:
                // view = inflater.inflate(R.layout.video_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.video_container, null);
                break;
            case QUIZ:
                // view = inflater.inflate(R.layout.quiz_container, currentContainerLayout, false);
                view = inflater.inflate(R.layout.quiz_container, null);
                break;
        }
        
        return view;
    }

    /**
     * Updates the check button text based on container type.
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

    /**
     * Handles the check button click event.
     */
    private void onCheckButtonClicked() {
        ContentContainer currentContainer = contentContainers.get(currentContainerIndex);
        
        /*For interactive containers (quizzes, etc.), validation would happen here
        For now, we just progress to the next container
        Answer validation can be added in a follow-up based on container type:
        - MultipleChoiceQuizContainer: check selected options
        - FillInTheGapsContainer: validate filled gaps
        - SortingTaskContainer: verify order
         - etc.
        */
        progressToNextContainer();
    }

    /**
     * Handles swipe up gesture to progress to next container.
     */
    private void onSwipeUp() {
        progressToNextContainer();
    }

    /**
     * Progresses to the next content container with animation.
     */
    private void progressToNextContainer() {
        if (currentContainerIndex < contentContainers.size() - 1) {
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
                    
                    // Animate new container sliding in
                    Animation slideIn = AnimationUtils.loadAnimation(FiveMinuteActivity.this, R.anim.slide_up_in);
                    currentContainerLayout.startAnimation(slideIn);
                    checkButton.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            currentContainerLayout.startAnimation(slideUp);
            
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

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

import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerErrorSpotting;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerMultipleChoiceQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerReverseQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerFillInTheGaps;
import com.example.a5minutechallenge.screens.challenge.LessonOverActivity;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.List;

public class FiveMinuteActivity extends AppCompatActivity implements TimerManager.TimerListener, ContainerInflater.OnContainerItemSelectedListener {

    private static final long MIN_ANSWER_TIME_MS = 1000;

    private FrameLayout currentContainerLayout;
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
    }
    
    private void initGamification() {
        scoreManager = new ScoreManager();
        timerManager = new TimerManager(this);

        
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
                            onCheckButtonClicked();
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
            // Pass event to detector and return true to consume the touch
            return gestureDetector.onTouchEvent(event);
        });
    }


    /**
     * Loads and displays the content containers for this lesson.
     */
    private void loadContent() {
        // Load content from ContentContainerLoader based on subject topic challenge and start index
        contentContainers = ContentContainerLoader.loadContent(this, subjectId, topicName, challengePosition, 0);

        if (!contentContainers.isEmpty()) {
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
        
        // Inflate and display vurrent container
        currentContainerLayout.removeAllViews();
        View containerView = inflateContainerView(container);
        currentContainerLayout.addView(containerView);
        
        /*// Display preview of next container if available, ill leave it as an idea but this is not intended to be used
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
        return containerInflater.inflateContainerView(container, this, this);
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



    /**
     * Handles the check button click event.
     */
    private void onCheckButtonClicked() {
        ContentContainer currentContainerGeneric = contentContainers.get(currentContainerIndex);
        
        // For interactive containers (quizzes, etc.), validate the answer
        boolean isCorrect = false;
        boolean userResponseExpected = false;
        
        switch (currentContainerGeneric.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
                userResponseExpected = true;
                ContainerMultipleChoiceQuiz currentContainer = (ContainerMultipleChoiceQuiz) currentContainerGeneric;
                // Use the existing isCorrect() method which properly checks if user-selected indices match correct answer indices
                isCorrect = currentContainer.isCorrect();
                break;
            case REVERSE_QUIZ:
                userResponseExpected = true;
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) currentContainerGeneric;
                // Use the existing isCorrect() method
                isCorrect = reverseQuizContainer.isCorrect();
                break;
            case FILL_IN_THE_GAPS:
                userResponseExpected = true;
                ContainerFillInTheGaps gapsContainer = (ContainerFillInTheGaps) currentContainerGeneric;
                // Use the existing isCorrect() method
                isCorrect = gapsContainer.isCorrect();
                break;
            case SORTING_TASK:
                //postponed
                break;
            case ERROR_SPOTTING:
                userResponseExpected = true;
                ContainerErrorSpotting errorSpottingContainer = (ContainerErrorSpotting) currentContainerGeneric;
                // Use the existing isCorrect() method
                isCorrect = errorSpottingContainer.isCorrect();
                break;
            case WIRE_CONNECTING:
                //postponed
                break;
            case QUIZ:
                //deprecated
                break;

            default:// TEXT, TITLE, VIDEO, RECAP don't need validation
                userResponseExpected = false;
                break;
        }
        
        if (userResponseExpected) {onAnswer(isCorrect);}
        
        progressToNextContainer();
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
                public void onAnimationRepeat(Animation animation) {
                }
            });
            currentContainerLayout.startAnimation(slideUp);
        } else { //last container, finish lesson
            checkLessonComplete(false);
        }
    }

    /**
     * Records an answer and updates the score display.

     * @param isCorrect Wether the answer was correct or not
     */
    public void onAnswer(boolean isCorrect) {
        if (isCorrect) {
            int points = scoreManager.recordCorrectAnswer();
            timerManager.addCorrectAnswerBonus();

            updateScoreDisplay();
            showScorePopup(points);
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
     */
    private void showScorePopup(int points) {
        String text = "+" + points;
        
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
        //pause/resume UI feedback here?
    }

    // OnContainerItemSelectedListener implementation from interface
    @Override
    public void onContainerItemSelected(ContentContainer container, int position) {
        //THIS IS LACKING USER INPUT FEEDBACK
        switch (container.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
                ContainerMultipleChoiceQuiz mcqContainer = (ContainerMultipleChoiceQuiz) container;
                if (mcqContainer.isAllowMultipleAnswers()) {
                    //Toggle selection for multiple answer mode
                    if (mcqContainer.getUserSelectedIndices().contains(position)) {
                        mcqContainer.removeUserSelectedIndex(position);
                    } else {
                        mcqContainer.addUserSelectedIndex(position);//Add UI feedback!!
                    }
                } else {
                    //Single answer, adding an option clears previous
                    mcqContainer.addUserSelectedIndex(position);
                }
                break;
            case REVERSE_QUIZ:
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) container;
                reverseQuizContainer.setUserSelectedIndex(position);
                break;
            case ERROR_SPOTTING:
                ContainerErrorSpotting errorSpottingContainer = (ContainerErrorSpotting) container;
                errorSpottingContainer.setUserSelectedIndex(position);
                break;
            case FILL_IN_THE_GAPS:
                ContainerFillInTheGaps gapsContainer = (ContainerFillInTheGaps) container;
                gapsContainer.setUserSelectedWordIndex(position);
                break;
            case WIRE_CONNECTING:
                //postponed
                break;
            case SORTING_TASK:
                //postponed
                break;
            default:
                //No action needed for other container types
                break;
        }
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

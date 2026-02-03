/** Activity for the 5-minute-screen opened by the user.
 *  Displays content containers one at a time with a preview of the next container.
 *  Includes gamification with timer, scoring, and swipe gestures.
 */
package com.example.a5minutechallenge.screens.fiveminute;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerErrorSpotting;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerMultipleChoiceQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerRecap;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerReverseQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerFillInTheGaps;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerSortingTask;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerWireConnecting;
import com.example.a5minutechallenge.screens.challenge.LessonOverActivity;
import com.example.a5minutechallenge.R;
import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FiveMinuteActivity extends AppCompatActivity implements TimerManager.TimerListener, ContainerInflater.OnContainerItemSelectedListener {

    private static final long MIN_ANSWER_TIME_MS = 1000;
    private static final long COMPLETION_DELAY_MS = 800; // Delay before showing "finished" screen

    private FrameLayout currentContainerLayout;
    private Button checkButton;
    private TextView timerText;
    private TextView scoreDisplay;
    private TextView streakIndicator;
    private TextView scorePopup;
    private TextView timeBonusPopup;
    private TextView transitionTimeDisplay;
    private ProgressBar timerProgress;
    private View lowTimeOverlay;
    private View correctFlashOverlay;
    
    private ScoreManager scoreManager;
    private TimerManager timerManager;
    private GestureDetector gestureDetector;
    
    private List<ContentContainer> contentContainers;
    private int currentContainerIndex = 0;
    private boolean currentAnswerChecked = false;
    private int totalInteractiveContainers = 0;
    private int correctAnswersCount = 0;
    private boolean lastAnswerWasCorrect = false;
    private int recapIdCounter = 10000; // init value for generating unique recap container IDs, 10k is kust random value
    
    private String topicName;
    private int subjectId;
    private int challengePosition = -1; //first challenge pos, -1 if not from challenge list

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
        timeBonusPopup = findViewById(R.id.time_bonus_popup);
        transitionTimeDisplay = findViewById(R.id.transition_time_display);
        timerProgress = findViewById(R.id.timer_progress);
        lowTimeOverlay = findViewById(R.id.low_time_overlay);
        correctFlashOverlay = findViewById(R.id.correct_flash_overlay);
        
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

        
        //timer start w delay
        timerText.postDelayed(() -> timerManager.start(), 3000);
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

            //gestureDetector needs onDown: true to track gesture
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        View contentArea = findViewById(R.id.content_container_area);
        contentArea.setOnTouchListener((v, event) -> {
            //throughput event to gesture detector,
            // dont consume touch so the nested container view can work with it
            // as well
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }


    /**
     * Loads and displays the content containers for this lesson.
     */
    private void loadContent() {
        //content from ContentContainerDataLoader based on subject topic challenge and start index
        contentContainers = ContentContainerDataLoader.loadContent(this, subjectId, topicName, challengePosition, 0);

        //total interactive containers for progress bar
        for (ContentContainer c : contentContainers) {
            switch (c.getType()) {
                case MULTIPLE_CHOICE_QUIZ:
                case REVERSE_QUIZ:
                case FILL_IN_THE_GAPS:
                case ERROR_SPOTTING:
                case SORTING_TASK:
                case WIRE_CONNECTING:
                    totalInteractiveContainers++;
                    break;
                default:
                    break;
            }
        }

        //init container display
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
        
        // Reset answer checked state for new container
        currentAnswerChecked = false;
        
        // Inflate and display vurrent container
        currentContainerLayout.removeAllViews();
        View containerView = inflateContainerView(container);
        currentContainerLayout.addView(containerView);
        
        /*// Display preview of next container if available, ill leave it here as an idea but this is not intended to be used
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
     * Since it requires an outragingly long switchcase, i moved it to a new class.
     * This right here may seem duplicate but i think
     * its good to keep it around in case i ever want to add something
     * to this algorithm
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
     * Handles the check button click or swipe event.
     */
    private void onCheckButtonClicked() {
        ContentContainer currentContainerGeneric = contentContainers.get(currentContainerIndex);
        
        // For RECAP containers, check the wrapped container type instead
        ContentContainer containerToCheck = currentContainerGeneric;
        if (currentContainerGeneric.getType() == ContentContainer.Types.RECAP) {
            ContainerRecap recapContainer = (ContainerRecap) currentContainerGeneric;
            if (recapContainer.getWrappedContainer() != null) {
                containerToCheck = recapContainer.getWrappedContainer();
            }
        }
        
        // Determine if this container requires user response
        boolean userResponseExpected = false;
        switch (containerToCheck.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
            case REVERSE_QUIZ:
            case FILL_IN_THE_GAPS:
            case ERROR_SPOTTING:
            case SORTING_TASK:
            case WIRE_CONNECTING:
                userResponseExpected = true;
                break;
            default:
                userResponseExpected = false;
                break;
        }
        
        // If user response is expected and answer hasn't been checked yet, check it
        if (userResponseExpected && !currentAnswerChecked) {
            checkAnswer();
            currentAnswerChecked = true;
            checkButton.setText(R.string.next_question);
            return; // Don't progress yet
        }
        
        // Otherwise, progress to next container
        progressToNextContainer();
    }

    /**
     * Checks the answer for the current container and provides feedback
     */
    private void checkAnswer() {
        ContentContainer currentContainerGeneric = contentContainers.get(currentContainerIndex);
        View containerView = currentContainerLayout.getChildAt(0);
        
        // For RECAP containers, check the wrapped container instead
        ContentContainer containerToCheck = currentContainerGeneric;
        if (currentContainerGeneric.getType() == ContentContainer.Types.RECAP) {
            ContainerRecap recapContainer = (ContainerRecap) currentContainerGeneric;
            if (recapContainer.getWrappedContainer() != null) {
                containerToCheck = recapContainer.getWrappedContainer();
                // For RECAP, the wrapped container view is inside the recap frame
                View wrappedFrame = containerView.findViewById(R.id.wrapped_container_frame);
                if (wrappedFrame instanceof FrameLayout && ((FrameLayout) wrappedFrame).getChildCount() > 0) {
                    containerView = ((FrameLayout) wrappedFrame).getChildAt(0);
                }
            }
        }
        
        // For interactive containers (quizzes, etc.), validate the answer
        boolean isCorrect = false;
        
        switch (containerToCheck.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
                ContainerMultipleChoiceQuiz currentContainer = (ContainerMultipleChoiceQuiz) containerToCheck;
                isCorrect = currentContainer.isCorrect();
                
                // Update UI with correct/incorrect colors
                RecyclerView mcqRecyclerView = containerView.findViewById(R.id.options_recycler_view);
                if (mcqRecyclerView != null) {
                    ContentContainerAdapter adapter = (ContentContainerAdapter) mcqRecyclerView.getTag(R.id.options_recycler_view);
                    if (adapter != null) {
                        adapter.revealAnswers(currentContainer.getCorrectAnswerIndices(), currentContainer.getUserSelectedIndices());
                    }
                }
                break;
            case REVERSE_QUIZ:
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) containerToCheck;
                isCorrect = (reverseQuizContainer.getUserSelectedIndex() == reverseQuizContainer.getCorrectQuestionIndex());
                
                // Update UI with correct/incorrect colors
                RecyclerView reverseRecyclerView = containerView.findViewById(R.id.question_options_recycler_view);
                if (reverseRecyclerView != null) {
                    ContentContainerAdapter adapter = (ContentContainerAdapter) reverseRecyclerView.getTag(R.id.question_options_recycler_view);
                    if (adapter != null) {
                        adapter.revealAnswers(
                            Collections.singletonList(reverseQuizContainer.getCorrectQuestionIndex()),
                            Collections.singletonList(reverseQuizContainer.getUserSelectedIndex())
                        );
                    }
                }
                break;
            case FILL_IN_THE_GAPS:
                ContainerFillInTheGaps gapsContainer = (ContainerFillInTheGaps) containerToCheck;
                isCorrect = gapsContainer.isCorrect();
                break;
            case SORTING_TASK:
                ContainerSortingTask sortingContainer = (ContainerSortingTask) containerToCheck;
                isCorrect = sortingContainer.isCorrect();
                // No visual feedback needed for sorting - the order itself is the feedback
                break;
            case ERROR_SPOTTING:
                //This container is broken for some reason and I dont have the Time to find a satisfactory fix.
                //Thus, it'll be marked as "correct" and excluded from all other pipeline steps.
                /*ContainerErrorSpotting errorSpottingContainer = (ContainerErrorSpotting) containerToCheck;
                isCorrect = (errorSpottingContainer.getUserSelectedIndex() == errorSpottingContainer.getErrorIndex());
                
                // Update UI with correct/incorrect colors
                RecyclerView errorRecyclerView = containerView.findViewById(R.id.items_recycler_view);
                if (errorRecyclerView != null) {
                    ContentContainerAdapter adapter = (ContentContainerAdapter) errorRecyclerView.getTag(R.id.items_recycler_view);
                    if (adapter != null) {
                        adapter.revealAnswers(
                            Collections.singletonList(errorSpottingContainer.getErrorIndex()),
                            Collections.singletonList(errorSpottingContainer.getUserSelectedIndex())
                        );
                    }
                }*/
                isCorrect = true;
                break;
            case WIRE_CONNECTING:
                //This container is broken for some reason and I dont have the Time to find a satisfactory fix.
                //Thus, it'll be marked as "correct" automatically and excluded from all other pipeline steps.
                //ContainerWireConnecting wireConnectingContainer = (ContainerWireConnecting) containerToCheck;
                isCorrect = true;
                break;
            case QUIZ:
                //deprecated
                break;
        }
        
        onAnswer(isCorrect);
    }

    /**
     * Progresses to the next content container with animation.
     * Calls checkLessonComplete() if there are no more containers
     */
    private void progressToNextContainer() {
        if (currentContainerIndex < contentContainers.size() - 1) {//if there are more containers to show
            // Show transition time display during swipe animation
            showTransitionTimeDisplay();
            
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
                    slideIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Hide transition time display when new container is fully shown
                            hideTransitionTimeDisplay();
                            checkButton.setEnabled(true);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    currentContainerLayout.startAnimation(slideIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            currentContainerLayout.startAnimation(slideUp);
        } else { //last container, finish lesson
            // Add delay to allow UI animations (progress, score updates) to complete before showing finished screen
            checkButton.setEnabled(false);
            currentContainerLayout.postDelayed(() -> checkLessonComplete(false), COMPLETION_DELAY_MS);
        }
    }

    /**
     * Shows the remaining time display in the center during container transition.
     */
    private void showTransitionTimeDisplay() {
        if (transitionTimeDisplay != null && timerManager != null) {
            transitionTimeDisplay.setText(timerManager.getFormattedTime());
            transitionTimeDisplay.setAlpha(0f);
            transitionTimeDisplay.setVisibility(View.VISIBLE);
            transitionTimeDisplay.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start();
        }
    }

    /**
     * Hides the transition time display with fade out animation.
     */
    private void hideTransitionTimeDisplay() {
        if (transitionTimeDisplay != null) {
            transitionTimeDisplay.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> transitionTimeDisplay.setVisibility(View.GONE))
                    .start();
        }
    }

    /**
     * Records an answer and updates the score display.

     * @param isCorrect Wether the answer was correct or not
     */
    public void onAnswer(boolean isCorrect) {
        lastAnswerWasCorrect = isCorrect;
        
        if (isCorrect) {
            correctAnswersCount++;
            int points = scoreManager.recordCorrectAnswer();
            timerManager.addCorrectAnswerBonus();

            updateScoreDisplay();
            showScorePopup(points);
            updateStreakDisplay();
            updateCorrectAnswersProgress();
            showCorrectFlash();

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
            
            // Queue current container for recap
            addCurrentContainerToRecap();
        }
    }

    /**
     * Adds the current container wrapped in a recap container to the end of the pipeline.
     * For recap containers, adds a fresh copy of the wrapped container without nesting.
     */
    private void addCurrentContainerToRecap() {

        ContentContainer currentContainer = contentContainers.get(currentContainerIndex);
        if(currentContainer.getType() == ContentContainer.Types.WIRE_CONNECTING || currentContainer.getType() == ContentContainer.Types.ERROR_SPOTTING) {
            return;
        }
        
        // If the current container is already a recap, extract its wrapped container
        // to avoid nesting recap containers
        ContentContainer containerToRecap = currentContainer;
        if (currentContainer.getType() == ContentContainer.Types.RECAP) {
            ContainerRecap recapContainer = (ContainerRecap) currentContainer;
            containerToRecap = recapContainer.getWrappedContainer();
            if (containerToRecap == null) {
                return; // No wrapped container to recap
            }
        }
        
        // Create a new recap container wrapping a fresh copy of the container
        ContainerRecap newRecapContainer = new ContainerRecap(recapIdCounter++);
        newRecapContainer.setRecapTitle(getString(R.string.recap) + ": " + getString(R.string.review_time));
        
        // Create a fresh copy of the container for recap (resets user selections)
        ContentContainer freshContainer = createFreshContainerCopy(containerToRecap);
        if (freshContainer != null) {
            newRecapContainer.setWrappedContainer(freshContainer);
            contentContainers.add(newRecapContainer);
        }
    }


    /**
     * Creates a fresh copy of a container with user selections reset.
     */
    private ContentContainer createFreshContainerCopy(ContentContainer original) {
        switch (original.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
                ContainerMultipleChoiceQuiz mcq = (ContainerMultipleChoiceQuiz) original;
                return new ContainerMultipleChoiceQuiz(recapIdCounter++)
                        .setQuestion(mcq.getQuestion())
                        .setOptions(new ArrayList<>(mcq.getOptions()))
                        .setCorrectAnswerIndices(new ArrayList<>(mcq.getCorrectAnswerIndices()))
                        .setAllowMultipleAnswers(mcq.isAllowMultipleAnswers())
                        .setExplanationText(mcq.getExplanationText());
            case REVERSE_QUIZ:
                ContainerReverseQuiz rq = (ContainerReverseQuiz) original;
                return new ContainerReverseQuiz(recapIdCounter++)
                        .setAnswer(rq.getAnswer())
                        .setQuestionOptions(new ArrayList<>(rq.getQuestionOptions()))
                        .setCorrectQuestionIndex(rq.getCorrectQuestionIndex())
                        .setExplanationText(rq.getExplanationText());
            case ERROR_SPOTTING:
                ContainerErrorSpotting es = (ContainerErrorSpotting) original;
                return new ContainerErrorSpotting(recapIdCounter++)
                        .setInstructions(es.getInstructions())
                        .setItems(new ArrayList<>(es.getItems()))
                        .setErrorIndex(es.getErrorIndex())
                        .setExplanationText(es.getExplanationText());
            case FILL_IN_THE_GAPS:
                ContainerFillInTheGaps fitg = (ContainerFillInTheGaps) original;
                return new ContainerFillInTheGaps(recapIdCounter++)
                        .setTextTemplate(fitg.getTextTemplate())
                        .setCorrectWords(new ArrayList<>(fitg.getCorrectWords()))
                        .setWordOptions(new ArrayList<>(fitg.getWordOptions()));
            case SORTING_TASK:
                ContainerSortingTask st = (ContainerSortingTask) original;
                return new ContainerSortingTask(recapIdCounter++)
                        .setInstructions(st.getInstructions())
                        .setCorrectOrder(new ArrayList<>(st.getCorrectOrder()));
            case WIRE_CONNECTING:
                ContainerWireConnecting wc = (ContainerWireConnecting) original;
                return new ContainerWireConnecting(recapIdCounter++)
                        .setInstructions(wc.getInstructions())
                        .setLeftItems(new ArrayList<>(wc.getLeftItems()))
                        .setRightItems(new ArrayList<>(wc.getRightItems()))
                        .setCorrectMatches(wc.getCorrectMatches());
            default:
                return null;
        }
    }

    /**
     * Updates the correct answers progress bar with animation.
     */
    private void updateCorrectAnswersProgress() {
        if (totalInteractiveContainers > 0) {
            int targetProgress = (correctAnswersCount * 100) / totalInteractiveContainers;
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(timerProgress, "progress", timerProgress.getProgress(), targetProgress);
            progressAnimator.setDuration(500);
            progressAnimator.setInterpolator(new DecelerateInterpolator());
            progressAnimator.start();
        }
    }

    /**
     * Shows a quick green flash on the edges for correct answers.
     */
    private void showCorrectFlash() {
        if (correctFlashOverlay != null) {
            correctFlashOverlay.setVisibility(View.VISIBLE);
            correctFlashOverlay.setAlpha(0.3f);
            correctFlashOverlay.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> correctFlashOverlay.setVisibility(View.GONE))
                    .start();
        }
    }

    /**
     * Shows the time bonus popup.
     */
    private void showTimeBonusPopup(int bonusSeconds) {
        if (timeBonusPopup != null) {
            timeBonusPopup.setText("+" + bonusSeconds + "s");
            timeBonusPopup.setAlpha(1f); // Reset alpha in case previous animation left it at 0
            timeBonusPopup.setVisibility(View.VISIBLE);

            Animation popupAnim = AnimationUtils.loadAnimation(this, R.anim.score_popup);
            popupAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    timeBonusPopup.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            timeBonusPopup.startAnimation(popupAnim);
        }
    }

    /**
     * Displays a score popup animation with the points earned.
     * @param points Points to display
     */
    private void showScorePopup(int points) {
        String text = "+" + points;
        
        scorePopup.setText(text);
        scorePopup.setAlpha(1f); // Reset alpha in case previous animation left it at 0
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
            // Note: timerProgress is now used for correct answers, not time
            
            // Change timer color based on remaining time
            if (timerManager.isCritical()) {
                timerText.setTextColor(getColor(R.color.timer_low));
                
                // Show low time overlay with animation
                if (lowTimeOverlay != null && lowTimeOverlay.getVisibility() != View.VISIBLE) {
                    lowTimeOverlay.setVisibility(View.VISIBLE);
                    lowTimeOverlay.animate().alpha(0.5f).setDuration(1000).start();
                }
                
                // Pulse animation when critical
                if (remainingSeconds % 2 == 0 && timerText.getAnimation() == null) {
                    Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
                    timerText.startAnimation(pulse);
                }
            } else if (timerManager.isWarning()) {
                timerText.setTextColor(getColor(R.color.timer_medium));
                // Slowly start showing the overlay
                if (lowTimeOverlay != null && lowTimeOverlay.getVisibility() != View.VISIBLE) {
                    lowTimeOverlay.setVisibility(View.VISIBLE);
                    lowTimeOverlay.setAlpha(0f);
                }
                if (lowTimeOverlay != null) {
                    float warningAlpha = ((float) TimerManager.WARNING_TIME - remainingSeconds) / (float) TimerManager.WARNING_TIME * 0.3f;
                    lowTimeOverlay.setAlpha(warningAlpha);
                }
            } else {// lowTimeOverlay = null
                timerText.setTextColor(getColor(R.color.text_primary));
                if (lowTimeOverlay != null) {
                    lowTimeOverlay.setVisibility(View.GONE);
                    lowTimeOverlay.setAlpha(0f);
                }
            }
        });
    }

    @Override
    public void onTimeOver() {
        checkLessonComplete(true);
    }

    @Override
    public void onTimerStateChanged(boolean isRunning) {
        //pause/resume UI feedback here? -> not implemented due to timely limitations
    }

    @Override
    public void onTimeBonusAwarded(int bonusSeconds) {
        runOnUiThread(() -> showTimeBonusPopup(bonusSeconds));
    }

    // OnContainerItemSelectedListener implementation from interface
    @Override
    public void onContainerItemSelected(ContentContainer container, int position) {
        // Get the current container view and its RecyclerView
        View containerView = currentContainerLayout.getChildAt(0);
        ContentContainerAdapter adapter = null;
        
        switch (container.getType()) {
            case MULTIPLE_CHOICE_QUIZ:
                ContainerMultipleChoiceQuiz mcqContainer = (ContainerMultipleChoiceQuiz) container;
                RecyclerView mcqRecyclerView = containerView.findViewById(R.id.options_recycler_view);
                if (mcqRecyclerView != null) {
                    adapter = (ContentContainerAdapter) mcqRecyclerView.getTag(R.id.options_recycler_view);
                }
                
                if (mcqContainer.isAllowMultipleAnswers()) {
                    //Toggle selection for multiple answer mode
                    if (mcqContainer.getUserSelectedIndices().contains(position)) {
                        mcqContainer.removeUserSelectedIndex(position);
                    } else {
                        mcqContainer.addUserSelectedIndex(position);
                    }
                    if (adapter != null) adapter.toggleSelection(position);
                } else {
                    //Single answer, adding an option clears previous
                    mcqContainer.addUserSelectedIndex(position);
                    if (adapter != null) adapter.setSingleSelection(position);
                }
                break;
            case REVERSE_QUIZ:
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) container;
                reverseQuizContainer.setUserSelectedIndex(position);
                RecyclerView reverseRecyclerView = containerView.findViewById(R.id.question_options_recycler_view);
                if (reverseRecyclerView != null) {
                    adapter = (ContentContainerAdapter) reverseRecyclerView.getTag(R.id.question_options_recycler_view);
                    if (adapter != null) adapter.setSingleSelection(position);
                }
                break;
            case ERROR_SPOTTING:
                ContainerErrorSpotting errorSpottingContainer = (ContainerErrorSpotting) container;
                errorSpottingContainer.setUserSelectedIndex(position);
                RecyclerView errorRecyclerView = containerView.findViewById(R.id.items_recycler_view);
                if (errorRecyclerView != null) {
                    adapter = (ContentContainerAdapter) errorRecyclerView.getTag(R.id.items_recycler_view);
                    if (adapter != null) adapter.setSingleSelection(position);
                }
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

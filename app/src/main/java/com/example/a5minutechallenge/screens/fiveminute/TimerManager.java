/** Manages the countdown timer for the 5-minute challenge with time refill capabilities.
 **/
package com.example.a5minutechallenge.screens.fiveminute;

import android.os.Handler;
import android.os.Looper;

public class TimerManager {

    private static final int INITIAL_TIME_SECONDS = 300; // 5 minutes
    private static final int CORRECT_ANSWER_BONUS_SECONDS = 10;
    private static final int UPDATE_INTERVAL_MS = 100; // Update 10 times per second for smooth animation
    public static final int LOW_TIME = 30;
    public static final int WARNING_TIME = 60;

    private int remainingTimeSeconds;
    private long lastUpdateTime;
    private boolean isRunning;
    private Handler handler;
    private Runnable updateRunnable;
    private TimerListener listener;
    private int lastBonusAwarded = 0; // Track last bonus for popup
    
    public interface TimerListener {
        void onTimeUpdate(int remainingSeconds, float percentage);
        void onTimeOver();
        void onTimerStateChanged(boolean isRunning);
        default void onTimeBonusAwarded(int bonusSeconds) {} // Optional callback for time bonus popup
    }
    
    public TimerManager(TimerListener listener) {
        this.listener = listener;
        this.remainingTimeSeconds = INITIAL_TIME_SECONDS;
        this.isRunning = false;
        this.handler = new Handler(Looper.getMainLooper());
        
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedMs = currentTime - lastUpdateTime;
                    
                    if (elapsedMs >= 1000) {
                        lastUpdateTime = currentTime;
                        remainingTimeSeconds--;
                        
                        if (remainingTimeSeconds <= 0) {
                            remainingTimeSeconds = 0;
                            stop();
                            if (listener != null) {
                                listener.onTimeOver();
                            }
                        }
                        
                        if (listener != null) {
                            listener.onTimeUpdate(remainingTimeSeconds, getPercentage());
                        }
                    }
                    
                    handler.postDelayed(this, UPDATE_INTERVAL_MS);
                }
            }
        };
    }
    
    /**
     * Starts the timer countdown.
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            lastUpdateTime = System.currentTimeMillis();
            handler.post(updateRunnable);
            
            if (listener != null) {
                listener.onTimerStateChanged(true);
            }
        }
    }
    
    /**
     * Pauses the timer countdown.
     */
    public void pause() {
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(updateRunnable);
            
            if (listener != null) {
                listener.onTimerStateChanged(false);
            }
        }
    }
    
    /**
     * Stops the timer completely.
     */
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(updateRunnable);
        
        if (listener != null) {
            listener.onTimerStateChanged(false);
        }
    }
    
    /**
     * Adds time to the timer as a reward for correct answers.
     * @param seconds Seconds to add
     */
    public void addTime(int seconds) {
        lastBonusAwarded = seconds;
        remainingTimeSeconds += seconds;
        if (remainingTimeSeconds > INITIAL_TIME_SECONDS) {
            remainingTimeSeconds = INITIAL_TIME_SECONDS; // Cap at initial time
        }
        
        if (listener != null) {
            listener.onTimeUpdate(remainingTimeSeconds, getPercentage());
            listener.onTimeBonusAwarded(seconds);
        }
    }
    
    /**
     * Adds bonus time for a correct answer.
     */
    public void addCorrectAnswerBonus() {
        addTime(CORRECT_ANSWER_BONUS_SECONDS);
    }

    /**
     * Gets the last bonus amount awarded.
     */
    public int getLastBonusAwarded() {
        return lastBonusAwarded;
    }
    
    /**
     * Gets the remaining time in seconds.
     * @return Remaining seconds
     */
    public int getRemainingTimeSeconds() {
        return remainingTimeSeconds;
    }
    
    /**
     * Gets the percentage of time remaining (0.0 to 1.0).
     * @return Percentage as a float
     */
    public float getPercentage() {
        return (float) remainingTimeSeconds / INITIAL_TIME_SECONDS;
    }
    
    /**
     * Checks if timer is in critical state (< 30 seconds).
     * @return true if less than 30 seconds remaining
     */
    public boolean isCritical() {
        return remainingTimeSeconds < LOW_TIME;
    }
    
    /**
     * Checks if timer is in warning state (< 60 seconds).
     * @return true if less than 60 seconds remaining
     */
    public boolean isWarning() {
        return remainingTimeSeconds < WARNING_TIME && remainingTimeSeconds >= LOW_TIME;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Resets the timer to initial time.
     */
    public void reset() {
        stop();
        remainingTimeSeconds = INITIAL_TIME_SECONDS;
        
        if (listener != null) {
            listener.onTimeUpdate(remainingTimeSeconds, getPercentage());
        }
    }
    
    /**
     * Formats remaining time as MM:SS string.
     * @return Formatted time string
     */
    public String getFormattedTime() {
        int minutes = remainingTimeSeconds / 60;
        int seconds = remainingTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

/** Manages the scoring system for the 5-minute challenge including streaks, bonuses, and point calculations.
 **/
package com.example.a5minutechallenge.screens.fiveminute;

public class ScoreManager {
    
    private int totalScore;
    private int currentStreak;
    private int maxStreak;

    private long lastAnswerTime;
    private int correctAnswers;
    private int totalAnswers;
    private static final int QUICK_ANSWER_THRESHOLD_MS = 5000; // 5s
    private static final int QUICK_ANSWER_BONUS = 5;
    private static final int STREAK_MULTIPLIER = 20;
    private static final int BASE_CORRECT_POINTS = 100;
    
    public ScoreManager() {
        this.totalScore = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.correctAnswers = 0;
        this.totalAnswers = 0;
        this.lastAnswerTime = System.currentTimeMillis();
    }
    
    /**
     * Records a correct answer and calculates points with bonuses.
     * @param answerTimeMs Time taken to answer in milliseconds
     * @return Points awarded for this answer
     */
    public int recordCorrectAnswer() {
        correctAnswers++;
        totalAnswers++;
        currentStreak++;
        
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
        
        int points = BASE_CORRECT_POINTS;
        
        // Streak bonus
        if (currentStreak > 1) {
            points += (currentStreak - 1) * STREAK_MULTIPLIER;
        }
        
        totalScore += points;
        lastAnswerTime = System.currentTimeMillis();
        
        return points;
    }
    
    /**
     * Records an incorrect answer and resets the streak.
     */
    public void recordIncorrectAnswer() {
        totalAnswers++;
        currentStreak = 0;
        lastAnswerTime = System.currentTimeMillis();
    }
    
    /**
     * Adds a time bonus when the lesson is completed before time runs out. No longer in use
     * because quick answers were a stupid incentive
     * @param remainingSeconds Seconds remaining when lesson completed
     * @return Bonus points awarded
     */
    /*public int addTimeBonus(int remainingSeconds) {
        int bonus = remainingSeconds * 5;
        totalScore += bonus;
        return bonus;
    }*/
    
    /**
     * Adds an accuracy bonus based on the percentage of correct answers.
     * @return Accuracy bonus points
     */
    public int addAccuracyBonus() {
        if (totalAnswers == 0) return 0;
        
        double accuracy = (double) correctAnswers / totalAnswers;
        int bonus = (int) (accuracy * 100);//all correct makes +100%
        
        if (accuracy == 1.0) {
            bonus += 200; // Perfect bonus; ideally there would be a popup for this
        }
        
        totalScore += bonus;
        return bonus;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public int getMaxStreak() {
        return maxStreak;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public int getTotalAnswers() {
        return totalAnswers;
    }
    
    public double getAccuracy() {
        if (totalAnswers == 0) return 0;
        return (double) correctAnswers / totalAnswers;
    }

    // Reset all scores and counters; no longer used
    public void reset() {
        totalScore = 0;
        currentStreak = 0;
        maxStreak = 0;
        correctAnswers = 0;
        totalAnswers = 0;
        lastAnswerTime = System.currentTimeMillis();
    }
}

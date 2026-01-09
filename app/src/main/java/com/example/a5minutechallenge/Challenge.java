/** Represents a 5-minute challenge with progress tracking */
package com.example.a5minutechallenge;

public class Challenge {
    private String title;
    private String description;
    private boolean completed;
    private int bestScore;
    private int attempts;
    
    public Challenge(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
        this.bestScore = 0;
        this.attempts = 0;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public int getBestScore() {
        return bestScore;
    }
    
    public void setBestScore(int bestScore) {
        if (bestScore > this.bestScore) {
            this.bestScore = bestScore;
        }
    }
    
    public int getAttempts() {
        return attempts;
    }
    
    public void incrementAttempts() {
        this.attempts++;
    }
    
    public int getProgressPercentage() {
        if (!completed) {
            return 0;
        }
        return 100;
    }
}

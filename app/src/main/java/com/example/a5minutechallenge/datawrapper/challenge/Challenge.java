/** Represents a 5-minute challenge with progress tracking */
package com.example.a5minutechallenge.datawrapper.challenge;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.ArrayList;

public class Challenge {
    private String title;
    private String description;
    private boolean completed;
    private int bestScore;
    private int attempts;
    private ArrayList<ContentContainer> containerlist;

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

    /**
     * Updates the best score if the new score is higher than the current best.
     * 
     * @param bestScore The new score to consider
     */
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

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getProgressPercentage() {
        // Simple binary progress: 0 if not completed, 100 if completed
        // In a more sophisticated implementation, this could track partial progress
        // based on score thresholds, question completion, or other milestones
        if (!completed) {
            return 0;
        }
        return 100;
    }

    public ArrayList<ContentContainer> getContainerlist() {
        if (containerlist == null) {
            containerlist = new ArrayList<>();
        }
        return containerlist;
    }

    public void setContainerlist(ArrayList<ContentContainer> containerlist) {
        this.containerlist = containerlist;
    }

    public void addContainer(ContentContainer container) {
        getContainerlist().add(container);
    }
}

/** The main class for topics that stores all information every topic must have */
package com.example.a5minutechallenge.datawrapper.topic;

import com.example.a5minutechallenge.datawrapper.challenge.Challenge;

import java.util.ArrayList;

public class Topic {
    private String title;
    private ArrayList<Challenge> challenges;

    public Topic(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public ArrayList<Challenge> getChallenges() {
        if (challenges == null) {
            challenges = new ArrayList<>();
        }
        return challenges;
    }

    public void setChallenges(ArrayList<Challenge> challenges) {
        this.challenges = challenges;
    }

    public void addChallenge(Challenge challenge) {
        getChallenges().add(challenge);
    }

    //Aggregation helpers for progress tracking

    /**
     * Returns the total number of challenges in this topic.
     */
    public int getTotalChallenges() {
        return getChallenges().size();
    }

    /**
     * Returns the number of completed challenges in this topic.
     */
    public int getCompletedChallenges() {
        int count = 0;
        for (Challenge c : getChallenges()) {
            if (c.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the progress percentage (0-100) based on completed challenges.
     */
    public int getProgressPercentage() {
        int total = getTotalChallenges();
        if (total == 0) return 0;
        return (getCompletedChallenges() * 100) / total;
    }

    /**
     * Returns true if all challenges in this topic are completed.
     */
    public boolean isCompleted() {
        int total = getTotalChallenges();
        return total > 0 && getCompletedChallenges() == total;
    }

    /**
     * Returns total attempts across all challenges in this topic.
     */
    public int getTotalAttempts() {
        int total = 0;
        for (Challenge c : getChallenges()) {
            total += c.getAttempts();
        }
        return total;
    }

    /**
     * Returns the best score among all challenges in this topic.
     */
    public int getBestScore() {
        int best = 0;
        for (Challenge c : getChallenges()) {
            if (c.getBestScore() > best) {
                best = c.getBestScore();
            }
        }
        return best;
    }

}

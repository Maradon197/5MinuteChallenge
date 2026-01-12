/** Singleton manager to track challenge state across activities.
 * Stores challenges in memory for the current session.
 **/
package com.example.a5minutechallenge.datawrapper.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChallengeManager {
    private static ChallengeManager instance;
    private Map<String, ArrayList<Challenge>> challengesByTopic;
    
    private ChallengeManager() {
        challengesByTopic = new HashMap<>();
    }
    
    /**
     * Gets the singleton instance of ChallengeManager.
     * @return The ChallengeManager instance
     */
    public static synchronized ChallengeManager getInstance() {
        if (instance == null) {
            instance = new ChallengeManager();
        }
        return instance;
    }
    
    /**
     * Stores challenges for a specific topic.
     * @param topicName The name of the topic
     * @param challenges The list of challenges for this topic
     */
    public void setChallengesForTopic(String topicName, ArrayList<Challenge> challenges) {
        challengesByTopic.put(topicName, challenges);
    }
    
    /**
     * Retrieves challenges for a specific topic.
     * @param topicName The name of the topic
     * @return The list of challenges for this topic, or null if not found
     */
    public ArrayList<Challenge> getChallengesForTopic(String topicName) {
        return challengesByTopic.get(topicName);
    }
    
    /**
     * Gets a specific challenge by topic and position.
     * @param topicName The name of the topic
     * @param position The position of the challenge in the list
     * @return The challenge, or null if not found
     */
    public Challenge getChallenge(String topicName, int position) {
        ArrayList<Challenge> challenges = challengesByTopic.get(topicName);
        if (challenges != null && position >= 0 && position < challenges.size()) {
            return challenges.get(position);
        }
        return null;
    }
    
    /**
     * Updates a challenge's completion status and score.
     * @param topicName The name of the topic
     * @param position The position of the challenge in the list
     * @param score The score achieved
     */
    public void updateChallengeCompletion(String topicName, int position, int score) {
        Challenge challenge = getChallenge(topicName, position);
        if (challenge != null) {
            challenge.setCompleted(true);
            challenge.setBestScore(score);
        }
    }
}

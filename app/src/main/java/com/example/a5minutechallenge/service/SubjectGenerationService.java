/** Orchestration service for generating Subject content from uploaded files using Gemini AI.
 * Coordinates file processing and populates Subject with Topics, Challenges, and ContentContainers.
 */
package com.example.a5minutechallenge.service;

import android.content.Context;
import android.util.Log;

import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;
import com.example.a5minutechallenge.datawrapper.topic.Topic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubjectGenerationService {
    
    private static final String TAG = "SubjectGenerationService";
    
    private final GeminiContentProcessor geminiProcessor;
    
    public SubjectGenerationService() {
        this.geminiProcessor = new GeminiContentProcessor();
    }
    
    /**
     * Generates content for a Subject from its uploaded files
     * @param subject The Subject to populate with generated content
     * @param context Android context for file access
     * @return true if generation was successful, false otherwise
     */
    public boolean generateContent(Subject subject, Context context) {
        try {
            // Get files for this subject
            ArrayList<SubjectFile> files = subject.getFiles(context);
            if (files == null || files.isEmpty()) {
                Log.e(TAG, "No files found for subject");
                return false;
            }
            
            // Process files with Gemini
            String jsonResponse = geminiProcessor.processFiles(files, subject.getTitle());
            
            // Parse and populate subject
            parseAndPopulateSubject(subject, jsonResponse);
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IO error during content generation", e);
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error during content generation", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during content generation", e);
            return false;
        }
    }
    
    /**
     * Parses JSON response and populates Subject with Topics, Challenges, and Containers
     */
    private void parseAndPopulateSubject(Subject subject, String jsonResponse) throws JSONException {
        JSONObject root = new JSONObject(jsonResponse);
        JSONArray topicsArray = root.getJSONArray("topics");
        
        ArrayList<Topic> topics = new ArrayList<>();
        
        for (int i = 0; i < topicsArray.length(); i++) {
            JSONObject topicJson = topicsArray.getJSONObject(i);
            Topic topic = parseTopic(topicJson);
            topics.add(topic);
        }
        
        subject.setTopics(topics);
        
        Log.i(TAG, "Successfully generated " + topics.size() + " topics for subject: " + subject.getTitle());
    }
    
    /**
     * Parses a Topic from JSON
     */
    private Topic parseTopic(JSONObject topicJson) throws JSONException {
        String title = topicJson.getString("title");
        Topic topic = new Topic(title);
        
        if (topicJson.has("challenges")) {
            JSONArray challengesArray = topicJson.getJSONArray("challenges");
            ArrayList<Challenge> challenges = new ArrayList<>();
            
            for (int i = 0; i < challengesArray.length(); i++) {
                JSONObject challengeJson = challengesArray.getJSONObject(i);
                Challenge challenge = parseChallenge(challengeJson);
                challenges.add(challenge);
            }
            
            topic.setChallenges(challenges);
        }
        
        return topic;
    }
    
    /**
     * Parses a Challenge from JSON
     */
    private Challenge parseChallenge(JSONObject challengeJson) throws JSONException {
        String title = challengeJson.getString("title");
        String description = challengeJson.optString("description", "");
        
        Challenge challenge = new Challenge(title, description);
        
        if (challengeJson.has("containers")) {
            JSONArray containersArray = challengeJson.getJSONArray("containers");
            ArrayList<ContentContainer> containers = new ArrayList<>();
            
            for (int i = 0; i < containersArray.length(); i++) {
                JSONObject containerJson = containersArray.getJSONObject(i);
                ContentContainer container = ContentContainerFactory.createFromJson(containerJson);
                if (container != null) {
                    containers.add(container);
                }
            }
            
            challenge.setContainerlist(containers);
        }
        
        return challenge;
    }
    
    /**
     * Validates that all source content was captured
     * @param subject The Subject to validate
     * @return Validation result with statistics
     */
    public ValidationResult validateContent(Subject subject) {
        ValidationResult result = new ValidationResult();
        
        ArrayList<Topic> topics = subject.getTopics();
        if (topics == null || topics.isEmpty()) {
            result.valid = false;
            result.message = "No topics generated";
            return result;
        }
        
        int totalChallenges = 0;
        int totalContainers = 0;
        
        for (Topic topic : topics) {
            ArrayList<Challenge> challenges = topic.getChallenges();
            if (challenges != null) {
                totalChallenges += challenges.size();
                
                for (Challenge challenge : challenges) {
                    ArrayList<ContentContainer> containers = challenge.getContainerlist();
                    if (containers != null) {
                        totalContainers += containers.size();
                    }
                }
            }
        }
        
        result.valid = true;
        result.topicCount = topics.size();
        result.challengeCount = totalChallenges;
        result.containerCount = totalContainers;
        result.message = String.format("Generated %d topics, %d challenges, %d containers", 
                topics.size(), totalChallenges, totalContainers);
        
        Log.i(TAG, result.message);
        return result;
    }
    
    /**
     * Result of content validation
     */
    public static class ValidationResult {
        public boolean valid = false;
        public int topicCount = 0;
        public int challengeCount = 0;
        public int containerCount = 0;
        public String message = "";
    }
}

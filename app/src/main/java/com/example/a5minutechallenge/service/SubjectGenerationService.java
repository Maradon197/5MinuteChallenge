/** Orchestration service for generating Subject content from uploaded files using Gemini AI.
 * Coordinates file processing and populates Subject with Topics, Challenges, and ContentContainers.
 */
package com.example.a5minutechallenge.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectGenerationService {

    private static final String TAG = "SubjectGenerationService";

    private final GeminiContentProcessor geminiProcessor;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Callback interface for generation results.
     */
    public interface GenerationCallback {
        void onGenerationSuccess(Subject subject);

        void onGenerationFailure(Exception e);

        void onProgress(int progress, String message);
    }

    public SubjectGenerationService() {
        this.geminiProcessor = new GeminiContentProcessor();
    }

    /**
     * Asynchronously generates content for a Subject from its uploaded files.
     * The result is delivered via the callback on the main UI thread.
     * 
     * @param subject  The Subject to populate with generated content
     * @param context  Android context for file access
     * @param callback Callback to handle success or failure
     */
    public void generateContent(final Subject subject, final Context context, final GenerationCallback callback) {
        executor.execute(() -> {
            try {
                // Get files for this subject
                ArrayList<SubjectFile> files = subject.getFiles(context);
                if (files == null || files.isEmpty()) {
                    throw new IOException("No files found for subject");
                }

                // Process files with Gemini (This runs in the background)
                String jsonResponse = geminiProcessor.processFiles(files, subject.getTitle(context), context,
                        (progress, message) -> handler.post(() -> callback.onProgress(progress, message)));

                // Clear old generated content before saving new results
                subject.clearGeneratedContent(context);

                // Save the raw JSON as content.json (single source of truth)
                try {
                    subject.saveGeneratedJson(context, jsonResponse, "content.json");
                } catch (Exception e) {
                    Log.w(TAG, "Failed to save generated JSON: " + e.getMessage());
                }

                // Parse and populate subject
                parseAndPopulateSubject(subject, jsonResponse);

                // Save formatted progress structure
                subject.saveToStorage(context);

                // Post success result back to the main thread
                handler.post(() -> callback.onGenerationSuccess(subject));

            } catch (Exception e) {
                // Post failure result back to the main thread
                handler.post(() -> callback.onGenerationFailure(e));
            }
        });
    }

    /**
     * Parses JSON response and populates Subject with Topics, Challenges, and
     * Containers
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

        Log.i(TAG, "Successfully generated " + topics.size() + " topics for subject: " + subject.getTitle(null));
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
     * Parses a Challenge from JSON with graceful container error handling.
     * Skips malformed containers instead of failing the entire challenge.
     */
    private Challenge parseChallenge(JSONObject challengeJson) throws JSONException {
        String title = challengeJson.optString("title", "Untitled Challenge");
        String description = challengeJson.optString("description", "");

        Challenge challenge = new Challenge(title, description);

        if (challengeJson.has("containers")) {
            JSONArray containersArray = challengeJson.getJSONArray("containers");
            ArrayList<ContentContainer> containers = new ArrayList<>();
            int skippedCount = 0;

            for (int i = 0; i < containersArray.length(); i++) {
                try {
                    JSONObject containerJson = containersArray.getJSONObject(i);
                    ContentContainer container = ContentContainerFactory.createFromJson(containerJson);
                    if (container != null) {
                        containers.add(container);
                    } else {
                        skippedCount++;
                        Log.w(TAG, "Container " + i + " returned null in challenge: " + title);
                    }
                } catch (JSONException e) {
                    skippedCount++;
                    Log.w(TAG,
                            "Skipping malformed container " + i + " in challenge '" + title + "': " + e.getMessage());
                }
            }

            if (skippedCount > 0) {
                Log.i(TAG, "Challenge '" + title + "': parsed " + containers.size() +
                        " containers, skipped " + skippedCount + " malformed containers");
            }

            challenge.setContainerlist(containers);
        }

        return challenge;
    }

    /**
     * Validates that all source content was captured
     * 
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
     * Scans internal storage for folders named subject_<id> and returns the
     * list of numeric IDs found.
     */
    public ArrayList<Integer> getAllSubjectIDs(Context context) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (context == null)
            return ids;

        File filesDir = context.getFilesDir();
        if (filesDir == null || !filesDir.exists())
            return ids;

        File[] children = filesDir.listFiles();
        if (children == null)
            return ids;

        for (File f : children) {
            if (f.isDirectory()) {
                String name = f.getName();
                if (name.startsWith("subject_")) {
                    String num = name.substring("subject_".length());
                    try {
                        int id = Integer.parseInt(num);
                        ids.add(id);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return ids;
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

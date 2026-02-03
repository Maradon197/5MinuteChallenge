/** The main subject class
 *
 */

package com.example.a5minutechallenge.datawrapper.subject;

import android.content.Context;
import android.util.Log;

import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.util.fileutil.fileutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Subject {

    private Integer subjectId;
    private String title;
    private String description;
    private ArrayList<Topic> topics;
    private ArrayList<StorageListItem> storageItems;
    private ArrayList<SubjectFile> subjectFiles;

    public Subject(Integer id) {
        this.subjectId = id;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public String getTitle() {
        if (title == null) {
            // title may be lazily loaded from subject.json if available
        }
        return title;
    }

    /**
     * Returns the title for this subject, attempting to load from storage
     * if not already present in memory.
     */
    public String getTitle(Context context) {
        if (title == null && context != null) {
            loadMetaFromStorage(context);
        }
        return title;
    }

    public Subject setTitle(String newTitle) {
        title = newTitle;
        return this;
    }

    public String getDescription() {
        if (description == null) {
            // description may be lazily loaded from subject.json if available
        }
        return description;
    }

    /**
     * Returns the description for this subject, attempting to load from storage
     * if not already present in memory.
     */
    public String getDescription(Context context) {
        if (description == null && context != null) {
            loadMetaFromStorage(context);
        }
        return description;
    }

    /**
     * Writes a small metadata JSON (`subject.json`) containing title & description
     * into the subject_<id> folder.
     */
    public boolean saveMetaToStorage(Context context) {
        if (context == null)
            return false;
        try {
            File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
            if (!subjectDir.exists())
                subjectDir.mkdirs();

            JSONObject meta = new JSONObject();
            meta.put("subjectId", subjectId);
            meta.put("title", title == null ? "" : title);
            meta.put("description", description == null ? "" : description);

            File metaFile = new File(subjectDir, "subject.json");
            try (FileOutputStream fos = new FileOutputStream(metaFile)) {
                fos.write(meta.toString().getBytes(StandardCharsets.UTF_8));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads `subject.json` from the subject_<id> folder and sets title/description
     */
    private boolean loadMetaFromStorage(Context context) {
        if (context == null)
            return false;
        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        if (!subjectDir.exists() || !subjectDir.isDirectory())
            return false;

        File metaFile = new File(subjectDir, "subject.json");
        if (!metaFile.exists() || !metaFile.isFile())
            return false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(metaFile), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            String jsonText = sb.toString();
            JSONObject root = new JSONObject(jsonText);
            title = root.optString("title", title);
            description = root.optString("description", description);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes the entire subject_<id> folder and its contents.
     */
    public boolean deleteSubjectStorage(Context context) {
        if (context == null)
            return false;
        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        return deleteRecursively(subjectDir);
    }

    private boolean deleteRecursively(File fileOrDir) {
        if (fileOrDir == null || !fileOrDir.exists())
            return true;
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursively(child))
                        return false;
                }
            }
        }
        return fileOrDir.delete();
    }

    public Subject setDescription(String newDescription) {
        description = newDescription;
        return this;
    }

    public void setTopics(ArrayList<Topic> newtopics) {
        topics = newtopics;
    }

    /**
     * Returns topics for this subject. If topics are not yet loaded in memory,
     * attempts to load generated content JSON from storage.
     * 
     * @param context The application context, required for loading from storage
     * @return ArrayList of topics, or empty list if none are available
     */
    public ArrayList<Topic> getTopics(Context context) {
        if (topics == null) {
            topics = new ArrayList<>();
            if (context != null) {
                loadGeneratedContentFromStorage(context);
            }
        }
        return topics;
    }

    /**
     * Forces a reload of topics from storage, clearing any cached data.
     * Use this to refresh progress/gamification data after changes.
     * 
     * @param context The application context, required for loading from storage
     * @return ArrayList of topics, or empty list if none are available
     */
    public ArrayList<Topic> reloadTopics(Context context) {
        topics = null; // Clear cached topics to force reload
        return getTopics(context);
    }

    /**
     * Returns topics that have already been loaded into memory.
     * Use this only when topics have been explicitly set via setTopics() or
     * have already been loaded via getTopics(Context).
     * 
     * @return ArrayList of topics, or empty list if none are loaded
     */
    public ArrayList<Topic> getTopics() {
        if (topics == null) {
            topics = new ArrayList<>();
        }
        return topics;
    }

    public ArrayList<StorageListItem> getStorageItems() {
        if (storageItems == null) {
            storageItems = new ArrayList<>();
        }
        return storageItems;
    }

    public void addStorageItem(SubjectFile file) {
        getStorageItems().add(new StorageListItem(file.getFileName(), file));
    }

    /**
     * Gets all files for this subject. Extracts files from internal storage.
     * 
     * @param context The application context
     * @return ArrayList of all files for this subject
     */
    public ArrayList<SubjectFile> getFiles(Context context) {
        if (subjectFiles == null) {
            subjectFiles = new ArrayList<>();
            loadFilesFromStorage(context);
        }
        return subjectFiles;
    }

    /**
     * Saves a file to internal storage organized by subjectID
     * 
     * @param context     The application context
     * @param inputStream The input stream of the file to save
     * @param fileName    The name of the file
     * @return The SubjectFile object if successful, null otherwise
     */
    public SubjectFile saveFileToStorage(Context context, InputStream inputStream, String fileName) {
        if (context == null || inputStream == null || fileName == null) {
            return null;
        }

        // Sanitize filename to prevent directory traversal attacks
        String sanitizedFileName = fileutil.sanitizeFileName(fileName);
        if (sanitizedFileName.isEmpty()) {
            return null;
        }

        try {
            // Create subject-specific directory
            File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
            if (!subjectDir.exists()) {
                subjectDir.mkdirs();
            }

            // User-uploaded files are stored under a nested uploads folder so
            // other types of files can co-exist under the subject folder.
            File uploadsDir = new File(subjectDir, "uploads");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            // Create the file inside the nested uploads folder
            File file = new File(uploadsDir, sanitizedFileName);

            // Copy the file content
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            SubjectFile subjectFile = new SubjectFile(sanitizedFileName, file.getAbsolutePath());
            if (subjectFiles == null) {
                subjectFiles = new ArrayList<>();
            }
            subjectFiles.add(subjectFile);
            return subjectFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save a generated JSON string into subject_<id>/json/<fileName>
     */
    public SubjectFile saveGeneratedJson(Context context, String jsonContent, String fileName) {
        if (context == null || jsonContent == null || fileName == null)
            return null;

        String sanitizedFileName = fileutil.sanitizeFileName(fileName);
        if (sanitizedFileName.isEmpty())
            return null;

        try {
            File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
            if (!subjectDir.exists())
                subjectDir.mkdirs();

            File jsonDir = new File(subjectDir, "json");
            if (!jsonDir.exists())
                jsonDir.mkdirs();

            File file = new File(jsonDir, sanitizedFileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
            }

            return new SubjectFile(sanitizedFileName, file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Clears all generated content JSON files for this subject.
     * Use this before a new generation to ensure a fresh start.
     */
    public boolean clearGeneratedContent(Context context) {
        if (context == null)
            return false;
        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        File jsonDir = new File(subjectDir, "json");
        if (jsonDir.exists() && jsonDir.isDirectory()) {
            File[] files = jsonDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
        return true;
    }

    /**
     * Saves the current subject state (including all topics, challenges, and
     * progress)
     * to internal storage. This overwrites the existing content.json file.
     * 
     * @param context The application context
     * @return true if save was successful, false otherwise
     */
    public boolean saveToStorage(Context context) {
        if (context == null || topics == null || topics.isEmpty()) {
            return false;
        }

        try {
            // Build the JSON structure matching what GeminiContentProcessor generates
            JSONObject root = new JSONObject();
            JSONArray topicsArray = new JSONArray();

            for (Topic topic : topics) {
                JSONObject topicJson = new JSONObject();
                topicJson.put("title", topic.getTitle());

                ArrayList<Challenge> challenges = topic.getChallenges();
                if (challenges != null && !challenges.isEmpty()) {
                    JSONArray challengesArray = new JSONArray();
                    for (Challenge challenge : challenges) {
                        JSONObject challengeJson = new JSONObject();
                        challengeJson.put("title", challenge.getTitle());
                        challengeJson.put("description", challenge.getDescription());
                        // Save progress fields
                        challengeJson.put("completed", challenge.isCompleted());
                        challengeJson.put("bestScore", challenge.getBestScore());
                        challengeJson.put("attempts", challenge.getAttempts());

                        // Save containers
                        ArrayList<ContentContainer> containers = challenge.getContainerlist();
                        if (containers != null && !containers.isEmpty()) {
                            JSONArray containersArray = new JSONArray();
                            for (ContentContainer container : containers) {
                                containersArray.put(containerToJSON(container));
                            }
                            challengeJson.put("containers", containersArray);
                        }

                        challengesArray.put(challengeJson);
                    }
                    topicJson.put("challenges", challengesArray);
                }

                topicsArray.put(topicJson);
            }

            root.put("topics", topicsArray);

            // Save to content.json (overwrites existing)
            File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
            if (!subjectDir.exists())
                subjectDir.mkdirs();

            File jsonDir = new File(subjectDir, "json");
            if (!jsonDir.exists())
                jsonDir.mkdirs();

            File file = new File(jsonDir, "content.json");
            String jsonOutput = root.toString();
            Log.d("Subject",
                    "Writing content.json for subject_" + subjectId + ". Content length: " + jsonOutput.length());
            // Log a snippet for debugging (don't log the whole thing if it's huge)
            Log.d("Subject",
                    "JSON Snippet: " + (jsonOutput.length() > 500 ? jsonOutput.substring(0, 500) + "..." : jsonOutput));

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonOutput.getBytes(StandardCharsets.UTF_8));
            }

            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads all files from internal storage for this subject
     * 
     * @param context The application context
     */
    private void loadFilesFromStorage(Context context) {
        if (context == null) {
            return;
        }

        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        if (subjectDir.exists() && subjectDir.isDirectory()) {
            // Only load files that live under the uploads/ folder. Other
            // files will be handled separately via getOtherFiles().
            File uploadsDir = new File(subjectDir, "uploads");
            if (uploadsDir.exists() && uploadsDir.isDirectory()) {
                addFilesRecursively(uploadsDir);
            }
        }
    }

    /**
     * Loads generated JSON files from subject_<id>/json/ and populates `topics`.
     * Returns true if at least one topic was loaded.
     */
    private boolean loadGeneratedContentFromStorage(Context context) {
        if (context == null)
            return false;

        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        File jsonDir = new File(subjectDir, "json");
        if (!jsonDir.exists() || !jsonDir.isDirectory())
            return false;

        File[] files = jsonDir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0)
            return false;

        // Sort files so that "content.json" comes last (it should hold the most
        // up-to-date progress)
        java.util.Arrays.sort(files, (f1, f2) -> {
            if (f1.getName().equals("content.json"))
                return 1;
            if (f2.getName().equals("content.json"))
                return -1;
            return f1.getName().compareTo(f2.getName());
        });

        Log.d("Subject", "Loading generated content for subject_" + subjectId + ". Found " + files.length + " files.");
        for (File f : files) {
            Log.d("Subject", "Processing file: " + f.getName());
        }

        // Use a Map to de-duplicate topics by title while preserving order
        java.util.LinkedHashMap<String, Topic> uniqueTopics = new java.util.LinkedHashMap<>();

        for (File f : files) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                String jsonText = sb.toString();
                org.json.JSONObject root = new org.json.JSONObject(jsonText);
                if (!root.has("topics"))
                    continue;

                org.json.JSONArray topicsArray = root.getJSONArray("topics");
                for (int i = 0; i < topicsArray.length(); i++) {
                    org.json.JSONObject topicJson = topicsArray.getJSONObject(i);
                    String title = topicJson.optString("title", "");
                    if (title.isEmpty())
                        continue;

                    Topic topic = new Topic(title);

                    if (topicJson.has("challenges")) {
                        org.json.JSONArray challengesArray = topicJson.getJSONArray("challenges");
                        ArrayList<com.example.a5minutechallenge.datawrapper.challenge.Challenge> challenges = new ArrayList<>();
                        for (int j = 0; j < challengesArray.length(); j++) {
                            org.json.JSONObject challengeJson = challengesArray.getJSONObject(j);
                            String cTitle = challengeJson.optString("title", "");
                            String cDesc = challengeJson.optString("description", "");
                            com.example.a5minutechallenge.datawrapper.challenge.Challenge challenge = new com.example.a5minutechallenge.datawrapper.challenge.Challenge(
                                    cTitle, cDesc);

                            // Load progress fields
                            if (challengeJson.has("completed")) {
                                challenge.setCompleted(challengeJson.getBoolean("completed"));
                            }
                            if (challengeJson.has("bestScore")) {
                                challenge.setBestScore(challengeJson.getInt("bestScore"));
                            }
                            if (challengeJson.has("attempts")) {
                                challenge.setAttempts(challengeJson.getInt("attempts"));
                            }

                            if (challengeJson.has("containers")) {
                                org.json.JSONArray containersArray = challengeJson.getJSONArray("containers");
                                ArrayList<com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer> containers = new ArrayList<>();
                                for (int k = 0; k < containersArray.length(); k++) {
                                    org.json.JSONObject containerJson = containersArray.getJSONObject(k);
                                    try {
                                        com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer container = com.example.a5minutechallenge.service.ContentContainerFactory
                                                .createFromJson(containerJson);
                                        if (container != null)
                                            containers.add(container);
                                    } catch (org.json.JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                challenge.setContainerlist(containers);
                            }

                            challenges.add(challenge);
                        }
                        topic.setChallenges(challenges);
                    }

                    // Map-based de-duplication: titles are keys. content.json processed last wins.
                    uniqueTopics.put(title, topic);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!uniqueTopics.isEmpty()) {
            this.topics = new ArrayList<>(uniqueTopics.values());
            return true;
        }

        return false;
    }

    /**
     * Returns files under the subject directory that are NOT inside the
     * `uploads/` folder. Useful for handling other file types separately.
     */
    public ArrayList<SubjectFile> getOtherFiles(Context context) {
        ArrayList<SubjectFile> otherFiles = new ArrayList<>();
        if (context == null)
            return otherFiles;

        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        if (!subjectDir.exists() || !subjectDir.isDirectory())
            return otherFiles;

        File uploadsDir = new File(subjectDir, "uploads");
        addFilesRecursivelyExcluding(subjectDir, uploadsDir, otherFiles);
        return otherFiles;
    }

    /**
     * Recursively add files from dir to the supplied list, skipping the
     * excludeDir (and its children) if provided.
     */
    private void addFilesRecursivelyExcluding(File dir, File excludeDir, ArrayList<SubjectFile> list) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            if (excludeDir != null) {
                try {
                    String fCanonical = f.getCanonicalPath();
                    String excludeCanonical = excludeDir.getCanonicalPath();
                    if (fCanonical.startsWith(excludeCanonical)) {
                        // skip this entry (it's inside uploads/)
                        continue;
                    }
                } catch (IOException e) {
                    // If canonicalization fails, fall back to path compare
                    if (f.getAbsolutePath().startsWith(excludeDir.getAbsolutePath()))
                        continue;
                }
            }

            if (f.isFile()) {
                list.add(new SubjectFile(f.getName(), f.getAbsolutePath()));
            } else if (f.isDirectory()) {
                addFilesRecursivelyExcluding(f, excludeDir, list);
            }
        }
    }

    /**
     * Recursively add files from the given directory to the subjectFiles list.
     */
    private void addFilesRecursively(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File f : files) {
            if (f.isFile()) {
                subjectFiles.add(new SubjectFile(f.getName(), f.getAbsolutePath()));
            } else if (f.isDirectory()) {
                addFilesRecursively(f);
            }
        }
    }

    /**
     * Deletes a file from internal storage
     * 
     * @param subjectFile The SubjectFile to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteFile(SubjectFile subjectFile) {
        if (subjectFile == null || subjectFile.getFilePath() == null) {
            return false;
        }

        File file = new File(subjectFile.getFilePath());
        boolean deleted = file.exists() && file.delete();

        if (deleted && subjectFiles != null) {
            subjectFiles.remove(subjectFile);
        }

        return deleted;
    }

    /**
     * Renames a file in internal storage
     * 
     * @param subjectFile The SubjectFile to rename
     * @param newFileName The new name for the file
     * @return true if successful, false otherwise
     */
    public boolean renameFile(SubjectFile subjectFile, String newFileName) {
        if (subjectFile == null || subjectFile.getFilePath() == null || newFileName == null) {
            return false;
        }

        // Sanitize filename to prevent directory traversal attacks
        String sanitizedFileName = fileutil.sanitizeFileName(newFileName);
        if (sanitizedFileName.isEmpty()) {
            return false;
        }

        File oldFile = new File(subjectFile.getFilePath());
        if (!oldFile.exists()) {
            return false;
        }

        File newFile = new File(oldFile.getParent(), sanitizedFileName);

        // Check if target file already exists
        if (newFile.exists()) {
            return false;
        }

        if (oldFile.renameTo(newFile)) {
            subjectFile.setFileName(sanitizedFileName);
            subjectFile.setFilePath(newFile.getAbsolutePath());
            return true;
        }

        return false;
    }

    /**
     * Converts the Subject to JSON for serialization
     * 
     * @return JSONObject representation of the Subject
     * @throws JSONException if JSON creation fails
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("subjectId", subjectId);
        json.put("title", title);
        json.put("description", description);

        // Serialize topics
        if (topics != null && !topics.isEmpty()) {
            JSONArray topicsArray = new JSONArray();
            for (Topic topic : topics) {
                topicsArray.put(topicToJSON(topic));
            }
            json.put("topics", topicsArray);
        }

        return json;
    }

    /**
     * Creates a Subject from JSON data
     * 
     * @param json JSONObject containing Subject data
     * @return Subject instance
     * @throws JSONException if JSON parsing fails
     */
    public static Subject fromJSON(JSONObject json) throws JSONException {
        Integer id = json.getInt("subjectId");
        Subject subject = new Subject(id);

        if (json.has("title")) {
            subject.setTitle(json.getString("title"));
        }

        if (json.has("description")) {
            subject.setDescription(json.getString("description"));
        }

        if (json.has("topics")) {
            JSONArray topicsArray = json.getJSONArray("topics");
            ArrayList<Topic> topics = new ArrayList<>();
            for (int i = 0; i < topicsArray.length(); i++) {
                topics.add(topicFromJSON(topicsArray.getJSONObject(i)));
            }
            subject.setTopics(topics);
        }

        return subject;
    }

    private JSONObject topicToJSON(Topic topic) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", topic.getTitle());

        ArrayList<Challenge> challenges = topic.getChallenges();
        if (challenges != null && !challenges.isEmpty()) {
            JSONArray challengesArray = new JSONArray();
            for (Challenge challenge : challenges) {
                challengesArray.put(challengeToJSON(challenge));
            }
            json.put("challenges", challengesArray);
        }

        return json;
    }

    private JSONObject challengeToJSON(Challenge challenge) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", challenge.getTitle());
        json.put("description", challenge.getDescription());
        json.put("completed", challenge.isCompleted());
        json.put("bestScore", challenge.getBestScore());
        json.put("attempts", challenge.getAttempts());

        ArrayList<ContentContainer> containers = challenge.getContainerlist();
        if (containers != null && !containers.isEmpty()) {
            JSONArray containersArray = new JSONArray();
            for (ContentContainer container : containers) {
                containersArray.put(containerToJSON(container));
            }
            json.put("containers", containersArray);
        }

        return json;
    }

    private JSONObject containerToJSON(ContentContainer container) throws JSONException {
        // Delegate to ContentContainerFactory for complete serialization of all
        // container types
        return com.example.a5minutechallenge.service.ContentContainerFactory.containerToJson(container);
    }

    private static Topic topicFromJSON(JSONObject json) throws JSONException {
        Topic topic = new Topic(json.getString("title"));

        if (json.has("challenges")) {
            JSONArray challengesArray = json.getJSONArray("challenges");
            ArrayList<Challenge> challenges = new ArrayList<>();
            for (int i = 0; i < challengesArray.length(); i++) {
                challenges.add(challengeFromJSON(challengesArray.getJSONObject(i)));
            }
            topic.setChallenges(challenges);
        }

        return topic;
    }

    private static Challenge challengeFromJSON(JSONObject json) throws JSONException {
        Challenge challenge = new Challenge(
                json.getString("title"),
                json.getString("description"));

        if (json.has("completed")) {
            challenge.setCompleted(json.getBoolean("completed"));
        }

        if (json.has("bestScore")) {
            challenge.setBestScore(json.getInt("bestScore"));
        }

        // Note: Full deserialization of containers would use ContentContainerFactory

        return challenge;
    }

    // ===== Aggregation helpers for progress tracking =====

    /**
     * Returns the total number of topics in this subject.
     */
    public int getTotalTopics() {
        return getTopics().size();
    }

    /**
     * Returns the number of completed topics in this subject.
     * A topic is complete when all its challenges are complete.
     */
    public int getCompletedTopics() {
        int count = 0;
        for (Topic t : getTopics()) {
            if (t.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the total number of challenges across all topics.
     */
    public int getTotalChallenges() {
        int count = 0;
        for (Topic t : getTopics()) {
            count += t.getTotalChallenges();
        }
        return count;
    }

    /**
     * Returns the number of completed challenges across all topics.
     */
    public int getCompletedChallenges() {
        int count = 0;
        for (Topic t : getTopics()) {
            count += t.getCompletedChallenges();
        }
        return count;
    }

    /**
     * Returns the progress percentage (0-100) based on completed challenges.
     */
    public int getProgressPercentage() {
        int total = getTotalChallenges();
        if (total == 0)
            return 0;
        return (getCompletedChallenges() * 100) / total;
    }

    /**
     * Returns true if all challenges in this subject are completed.
     */
    public boolean isCompleted() {
        int total = getTotalChallenges();
        return total > 0 && getCompletedChallenges() == total;
    }

    /**
     * Returns total attempts across all challenges in this subject.
     */
    public int getTotalAttempts() {
        int total = 0;
        for (Topic t : getTopics()) {
            total += t.getTotalAttempts();
        }
        return total;
    }

    /**
     * Returns the best score among all challenges in this subject.
     */
    public int getBestScore() {
        int best = 0;
        for (Topic t : getTopics()) {
            int topicBest = t.getBestScore();
            if (topicBest > best) {
                best = topicBest;
            }
        }
        return best;
    }

    /**
     * Returns a short description string listing topic names, e.g. "Algebra •
     * Geometry • ..."
     * 
     * @param context   The application context for loading topics if needed
     * @param maxTopics Maximum number of topics to show before truncating with
     *                  "..."
     */
    public String getTopicsPreview(Context context, int maxTopics) {
        ArrayList<Topic> topicsList = getTopics(context);
        if (topicsList == null || topicsList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int count = Math.min(topicsList.size(), maxTopics);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(" • ");
            }
            sb.append(topicsList.get(i).getTitle());
        }
        if (topicsList.size() > maxTopics) {
            sb.append(" • …");
        }
        return sb.toString();
    }

}

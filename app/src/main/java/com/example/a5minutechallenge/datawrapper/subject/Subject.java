/** The main subject class
 *
 */

package com.example.a5minutechallenge.datawrapper.subject;

import android.content.Context;
import android.widget.Toast;

import com.example.a5minutechallenge.screens.storage.StorageListItem;
import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.service.SubjectGenerationService;
import com.example.a5minutechallenge.util.fileutil.fileutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        if(title == null) {
            //Fetch from DB
        }
        return title;
    }

    public Subject setTitle(String newTitle) {
        title = newTitle;
        return this;
    }

    public String getDescription() {
        if(description == null) {
            //Fetch from DB
        }
        return description;
    }

    public Subject setDescription(String newDescription) {
        description = newDescription;
        return this;
    }

    public void setTopics(ArrayList<Topic> newtopics) {
        topics = newtopics;
    }

    public void addTopic(String topicName) {
        getTopics().add(new Topic(topicName));
    }

    public ArrayList<Topic> getTopics() {
        if (topics == null) {
            topics = new ArrayList<>();
            switch (subjectId) {
                case 0: //MPI
                    topics.add(new Topic("Parallel architecture"));
                    topics.add(new Topic("Programming in distributed adress fields"));
                    topics.add(new Topic("Parallelization with MPI"));
                    topics.add(new Topic("Multithreading essentials"));
                    break;
                case 2:
                    topics.add(new Topic("ORDB"));
                    topics.add(new Topic("SQL - Statements"));
                    topics.add(new Topic("Why are you lurking in my code?"));
                    break;
                case 3:
                    /*
                    topics.add(new Topic("HTML"));
                    topics.add(new Topic("CSS"));
                    topics.add(new Topic("JavaScript"));
                     */



                    break;
                default:
                    topics.add(new Topic("Topic 1"));
                    topics.add(new Topic("Topic 2"));
                    topics.add(new Topic("Topic 3"));
                    break;
            }
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
     * @param context The application context
     * @param inputStream The input stream of the file to save
     * @param fileName The name of the file
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

            // Create the file
            File file = new File(subjectDir, sanitizedFileName);
            
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
     * Loads all files from internal storage for this subject
     * @param context The application context
     */
    private void loadFilesFromStorage(Context context) {
        if (context == null) {
            return;
        }

        File subjectDir = new File(context.getFilesDir(), "subject_" + subjectId);
        if (subjectDir.exists() && subjectDir.isDirectory()) {
            File[] files = subjectDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        subjectFiles.add(new SubjectFile(file.getName(), file.getAbsolutePath()));
                    }
                }
            }
        }
    }

    /**
     * Deletes a file from internal storage
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
        JSONObject json = new JSONObject();
        json.put("type", container.getType().toString());
        json.put("id", container.getId());
        // Note: Full serialization of container properties would require 
        // type-specific handling in each container class
        return json;
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
            json.getString("description")
        );
        
        if (json.has("completed")) {
            challenge.setCompleted(json.getBoolean("completed"));
        }
        
        if (json.has("bestScore")) {
            challenge.setBestScore(json.getInt("bestScore"));
        }
        
        // Note: Full deserialization of containers would use ContentContainerFactory
        
        return challenge;
    }

}

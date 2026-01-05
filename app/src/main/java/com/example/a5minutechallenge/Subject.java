/** The main subject class
 *
 */

package com.example.a5minutechallenge;

import android.content.Context;

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
                case 0:
                    topics.add(new Topic("Jetpack Compose"));           //list population, later fetched form DB
                    topics.add(new Topic("Kotlin"));                    //
                    topics.add(new Topic("Coroutines"));
                    break;
                case 2:
                    topics.add(new Topic("ORDB"));
                    topics.add(new Topic("SQL - Statements"));
                    topics.add(new Topic("Why are you lurking in my code?"));
                    break;
                case 3:
                    topics.add(new Topic("HTML"));
                    topics.add(new Topic("CSS"));
                    topics.add(new Topic("JavaScript"));
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

    public void addStorageItem(String itemName, SubjectFile file) {
        getStorageItems().add(new StorageListItem(itemName, file));
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
        String sanitizedFileName = sanitizeFileName(fileName);
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
        String sanitizedFileName = sanitizeFileName(newFileName);
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
     * Sanitizes a filename by removing path separators and other dangerous characters
     * @param fileName The filename to sanitize
     * @return The sanitized filename
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        
        // Remove path separators and other dangerous characters
        String sanitized = fileName.replaceAll("[/\\\\:*?\"<>|]", "_");
        
        // Remove leading/trailing dots and spaces
        sanitized = sanitized.replaceAll("^\\.+", "").replaceAll("\\.+$", "");
        sanitized = sanitized.trim();
        
        // Ensure filename is not empty after sanitization
        if (sanitized.isEmpty()) {
            return "";
        }
        
        return sanitized;
    }
}

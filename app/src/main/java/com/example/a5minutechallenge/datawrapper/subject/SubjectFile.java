/** The internally used Subjectfile. This is the actual file with a path */
package com.example.a5minutechallenge.datawrapper.subject;

import java.io.File;

public class SubjectFile {
    private String fileName;
    private String filePath;

    public SubjectFile(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public File getFile() {
        return new File(filePath);
    }

    public boolean exists() {
        return getFile().exists();
    }
}

# Subject File Storage System

## Overview
The file storage system allows files to be saved to internal app storage, organized by subject ID. Files can be retrieved, renamed, and deleted through the `Subject` class.

## Architecture

### Classes
- **SubjectFile**: Represents a file with its name and path
- **Subject**: Manages file operations for a specific subject
- **StorageItem**: Unchanged - used for other purposes in the UI

### Storage Structure
Files are stored in: `{app_files_dir}/subject_{subjectId}/{filename}`

Example:
```
/data/data/com.example.a5minutechallenge/files/
  ├── subject_0/
  │   ├── document1.pdf
  │   └── notes.txt
  ├── subject_1/
  │   └── lecture.pdf
  └── subject_2/
      └── assignment.docx
```

## Usage

### 1. Saving a File
```java
// When a file is selected via file picker
InputStream inputStream = getContentResolver().openInputStream(uri);
SubjectFile savedFile = subject.saveFileToStorage(context, inputStream, fileName);

if (savedFile != null) {
    // File saved successfully
    // It's now accessible via subject.getFiles(context)
}
```

### 2. Getting All Files for a Subject
```java
// This extracts files from storage into an ArrayList
ArrayList<SubjectFile> files = subject.getFiles(context);

for (SubjectFile file : files) {
    String name = file.getFileName();
    String path = file.getFilePath();
    File actualFile = file.getFile();
}
```

### 3. Renaming a File
```java
SubjectFile file = files.get(position);
boolean success = subject.renameFile(file, "newFileName.txt");

if (success) {
    // File renamed successfully
    // file.getFileName() and file.getFilePath() are updated
}
```

### 4. Deleting a File
```java
SubjectFile file = files.get(position);
boolean success = subject.deleteFile(file);

if (success) {
    // File deleted from storage and removed from the list
}
```

## Key Features
1. **Organized by Subject**: Each subject has its own directory
2. **Persistent Storage**: Files are saved to internal storage and persist across app restarts
3. **Getter Method Extraction**: `getFiles(Context)` automatically loads files from storage on first call
4. **File Management**: Built-in rename and delete functionality
5. **Separation of Concerns**: StorageItem remains unchanged for other purposes

## Implementation Details
- Files are copied to internal storage using FileOutputStream
- Directory structure: `subject_{subjectId}/`
- File paths are stored in SubjectFile objects
- Files are loaded lazily on first `getFiles()` call
- All file operations update both filesystem and in-memory ArrayList

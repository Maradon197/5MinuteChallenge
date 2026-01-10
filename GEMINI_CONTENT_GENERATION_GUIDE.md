# Gemini Content Generation Service - Usage Guide

## Overview
This implementation provides automated content generation for the 5-Minute Challenge app using Google's Gemini AI. The service processes uploaded documents (PDFs, text files, etc.) and generates structured learning content with topics, challenges, and interactive containers.

## Architecture

### Service Components

1. **GeminiContentProcessor** (`service/GeminiContentProcessor.java`)
   - Handles direct communication with Gemini REST API
   - Processes files and generates content via AI
   - Uses gemini-2.0-flash-exp model

2. **ContentContainerFactory** (`service/ContentContainerFactory.java`)
   - Factory pattern for creating ContentContainer instances from JSON
   - Supports all 10 container types:
     - TITLE, TEXT, MULTIPLE_CHOICE_QUIZ, FILL_IN_THE_GAPS
     - SORTING_TASK, ERROR_SPOTTING, REVERSE_QUIZ, WIRE_CONNECTING
     - RECAP, VIDEO

3. **SubjectGenerationService** (`service/SubjectGenerationService.java`)
   - Orchestrates the content generation workflow
   - Validates generated content
   - Populates Subject with generated Topics and Challenges

## Prerequisites

### API Key Configuration
Add your Gemini API key to `local.properties`:
```properties
GEMINI_API_KEY=your_api_key_here
```

Get your API key from: https://aistudio.google.com/app/apikey

## Usage Example

### Basic Usage
```java
// Create a subject with uploaded files
Subject subject = new Subject(1);
subject.setTitle("Machine Learning Basics");
subject.setDescription("Introduction to ML concepts");

// Upload some files (PDFs, text files, etc.)
// Files are automatically stored in internal storage
try (InputStream is = contentResolver.openInputStream(fileUri)) {
    SubjectFile file = subject.saveFileToStorage(context, is, "ml_notes.pdf");
    subject.addStorageItem(file);
}

// Generate content from files using AI
boolean success = subject.generateContentFromFiles(context);

if (success) {
    // Content is now populated in the Subject
    ArrayList<Topic> topics = subject.getTopics();
    for (Topic topic : topics) {
        Log.i("Generated Topic", topic.getTitle());
        
        ArrayList<Challenge> challenges = topic.getChallenges();
        for (Challenge challenge : challenges) {
            Log.i("Generated Challenge", challenge.getTitle());
            Log.i("Container Count", String.valueOf(challenge.getContainerlist().size()));
        }
    }
}
```

### Manual Service Usage
```java
// If you need more control, use the service directly
SubjectGenerationService service = new SubjectGenerationService();

// Generate content
boolean success = service.generateContent(subject, context);

// Validate the generated content
SubjectGenerationService.ValidationResult result = service.validateContent(subject);
if (result.valid) {
    Log.i("Validation", result.message);
    Log.i("Stats", "Topics: " + result.topicCount + 
                   ", Challenges: " + result.challengeCount + 
                   ", Containers: " + result.containerCount);
}
```

### Serialization
```java
// Convert Subject to JSON
try {
    JSONObject json = subject.toJSON();
    String jsonString = json.toString(2); // Pretty print with indent
    
    // Save to file or send to server
    // ...
    
    // Restore from JSON
    Subject restoredSubject = Subject.fromJSON(json);
} catch (JSONException e) {
    e.printStackTrace();
}
```

## Generated Content Structure

The AI generates content in this hierarchy:
```
Subject
├── Topic 1
│   ├── Challenge 1.1 (5-minute learning module)
│   │   ├── Container: TITLE
│   │   ├── Container: TEXT
│   │   ├── Container: MULTIPLE_CHOICE_QUIZ
│   │   └── Container: RECAP
│   ├── Challenge 1.2
│   └── Challenge 1.3
├── Topic 2
│   └── ...
```

Each Challenge contains 3-7 containers of various types for engagement.

## Container Types Explained

### Interactive Containers
- **MULTIPLE_CHOICE_QUIZ**: Single or multiple correct answers with explanations
- **FILL_IN_THE_GAPS**: Duolingo-style word selection exercises
- **SORTING_TASK**: Drag-and-drop ordering exercises
- **ERROR_SPOTTING**: Find the incorrect item in a list
- **REVERSE_QUIZ**: Given an answer, choose the correct question
- **WIRE_CONNECTING**: Match items from two columns

### Content Containers
- **TITLE**: Section headings
- **TEXT**: Explanatory content
- **RECAP**: Review container wrapping other containers
- **VIDEO**: Video URL container (not currently populated by AI)

## Prompt Engineering

The service uses a comprehensive prompt that instructs Gemini to:
1. Extract ALL information without summarization
2. Preserve technical terminology exactly
3. Organize into logical Topics
4. Create 5-minute Challenges with 3-7 containers each
5. Use diverse container types
6. Output valid JSON only (no markdown)

See `GeminiContentProcessor.buildPrompt()` for the full prompt template.

## Limitations and Considerations

### Current Limitations
1. **File Size**: Files are currently limited to 20MB for inline processing
2. **File Types**: Best results with text-based formats (PDF, TXT, MD, DOC)
3. **Context Window**: Gemini has a ~1M token limit; very large documents may need chunking
4. **API Costs**: Each generation call consumes API credits

### Future Enhancements
- Implement Gemini Files API for files > 20MB
- Add chunking for very large documents
- Support more file formats (images, presentations)
- Cache generated content to avoid regeneration
- Add progress callbacks for long-running generations
- Implement incremental updates (add content without regenerating all)

## Error Handling

```java
try {
    boolean success = subject.generateContentFromFiles(context);
    if (!success) {
        // Check logs for error details
        // Common issues:
        // - No files uploaded
        // - API key not configured
        // - Network error
        // - Malformed API response
    }
} catch (Exception e) {
    Log.e("ContentGeneration", "Failed to generate content", e);
    // Handle error appropriately
}
```

## Security Considerations

1. **API Key**: Never commit API keys to version control
2. **File Sanitization**: File names are sanitized to prevent directory traversal
3. **Input Validation**: All user inputs are validated before processing
4. **Error Messages**: Avoid exposing sensitive information in error messages

## Testing

### Unit Testing ContentContainerFactory
```java
@Test
public void testCreateMultipleChoiceQuiz() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", "MULTIPLE_CHOICE_QUIZ");
    json.put("question", "What is 2+2?");
    json.put("options", new JSONArray().put("3").put("4").put("5"));
    json.put("correctAnswerIndices", new JSONArray().put(1));
    
    ContentContainer container = ContentContainerFactory.createFromJson(json);
    
    assertNotNull(container);
    assertTrue(container instanceof ContainerMultipleChoiceQuiz);
    assertEquals("What is 2+2?", ((ContainerMultipleChoiceQuiz) container).getQuestion());
}
```

### Integration Testing
For integration testing with the actual Gemini API, ensure you have:
1. A valid API key configured
2. Test files in the subject's storage
3. Network connectivity
4. Sufficient API quota

## Troubleshooting

### "GEMINI_API_KEY not configured"
- Add your API key to `local.properties`
- Ensure the file is not checked into version control (.gitignore should exclude it)
- Rebuild the project after adding the key

### "No files found for subject"
- Verify files were uploaded successfully
- Check that `subject.getFiles(context)` returns non-empty list
- Ensure files exist at the stored paths

### "JSON parsing error"
- Check API response in logs
- Verify Gemini returned valid JSON
- Model might have wrapped JSON in markdown - extraction logic handles this

### Network/API Errors
- Check internet connectivity
- Verify API key is valid and has quota
- Check Gemini API status: https://status.cloud.google.com/

## Performance Tips

1. **Batch Processing**: Process multiple small files together rather than individually
2. **Caching**: Cache generated content to avoid repeated API calls
3. **Async Execution**: Run generation on background thread (already async-friendly)
4. **Progress Indication**: Show loading indicator during generation (can take 10-60 seconds)

## Support and Resources

- Gemini API Documentation: https://ai.google.dev/
- API Pricing: https://ai.google.dev/pricing
- Issue Tracker: [Your repo's issue tracker]

# Service Package - Gemini Content Generation

This package contains the service layer for AI-powered content generation using Google's Gemini API.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Subject.java                         │
│  (Data Model - Subject with uploaded files)                 │
│                                                              │
│  + generateContentFromFiles(Context) → boolean              │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              SubjectGenerationService.java                  │
│  (Orchestration Layer)                                      │
│                                                              │
│  + generateContent(Subject, Context) → boolean              │
│  + validateContent(Subject) → ValidationResult              │
└──────────────┬───────────────────────┬──────────────────────┘
               │                       │
               ▼                       ▼
┌──────────────────────────┐  ┌──────────────────────────────┐
│ GeminiContentProcessor   │  │  ContentContainerFactory     │
│  (AI Integration)        │  │   (Factory Pattern)          │
│                          │  │                               │
│  + processFiles()        │  │  + createFromJson()          │
│    → JSON String         │  │    → ContentContainer        │
└──────────────────────────┘  └──────────────────────────────┘
        │                              ▲
        │                              │
        ▼                              │
  Gemini REST API            JSON Response Parsing
```

## Components

### SubjectGenerationService
**Purpose:** Main orchestration service that coordinates the content generation workflow.

**Responsibilities:**
- Retrieve files from Subject
- Call GeminiContentProcessor to generate content
- Parse JSON response into data model objects
- Populate Subject with Topics, Challenges, and Containers
- Validate generated content

**Key Methods:**
- `generateContent(Subject, Context)` - Main entry point
- `validateContent(Subject)` - Returns validation statistics

### GeminiContentProcessor
**Purpose:** Handles communication with Gemini AI API.

**Responsibilities:**
- Read uploaded file contents
- Build comprehensive prompt for AI
- Make REST API calls to Gemini
- Extract and clean JSON from response
- Handle API errors gracefully

**Key Methods:**
- `processFiles(List<SubjectFile>, String)` - Processes files and returns JSON
- `isApiKeyConfigured()` - Validates API key presence

**API Details:**
- Model: gemini-2.0-flash-exp
- Endpoint: generativelanguage.googleapis.com/v1beta/models/.../generateContent
- Temperature: 0.7
- Max Output Tokens: 8192

### ContentContainerFactory
**Purpose:** Factory pattern for creating ContentContainer instances from JSON.

**Responsibilities:**
- Parse JSON objects
- Identify container type
- Instantiate appropriate ContentContainer subclass
- Populate all container properties
- Handle missing/optional fields

**Supported Types:**
1. TITLE - Section headings
2. TEXT - Explanatory content
3. MULTIPLE_CHOICE_QUIZ - Interactive quiz
4. FILL_IN_THE_GAPS - Word selection exercise
5. SORTING_TASK - Ordering exercise
6. ERROR_SPOTTING - Find incorrect item
7. REVERSE_QUIZ - Choose question for answer
8. WIRE_CONNECTING - Matching exercise
9. RECAP - Review wrapper container
10. VIDEO - Video URL container

**Key Methods:**
- `createFromJson(JSONObject)` - Main factory method
- `resetIdCounter()` - Reset ID sequence (for testing)

## Data Flow

1. **Input:** Subject with uploaded files
2. **File Processing:** Read file contents into memory
3. **Prompt Generation:** Build comprehensive AI prompt with JSON schema
4. **API Call:** Send prompt + files to Gemini API
5. **Response Parsing:** Extract JSON from API response
6. **Object Creation:** Parse JSON into data model hierarchy:
   - Topics created with titles
   - Challenges created with titles and descriptions
   - Containers created using factory pattern
7. **Population:** Subject populated with generated structure
8. **Validation:** Statistics calculated and returned

## Error Handling

### API Errors
- Network failures → IOException with message
- Invalid API key → IllegalStateException on initialization
- Rate limits → HTTP error codes in response
- Malformed responses → JSONException with fallback

### File Errors
- Missing files → Warning logged, skipped
- Large files (>20MB) → Current implementation skips, future enhancement needed
- Unreadable files → IOException, logged

### JSON Errors
- Invalid JSON → JSONException caught and logged
- Missing fields → Defaults used where possible
- Unknown container types → Returns null, logged

## Usage Example

```java
// Basic usage through Subject
Subject subject = new Subject(1);
subject.setTitle("Machine Learning");

// Upload files
SubjectFile file = subject.saveFileToStorage(context, inputStream, "ml.pdf");

// Generate content
boolean success = subject.generateContentFromFiles(context);

// Access generated content
if (success) {
    ArrayList<Topic> topics = subject.getTopics();
    for (Topic topic : topics) {
        ArrayList<Challenge> challenges = topic.getChallenges();
        for (Challenge challenge : challenges) {
            ArrayList<ContentContainer> containers = challenge.getContainerlist();
            // Use containers in UI
        }
    }
}
```

## Testing

Unit tests available in:
- `app/src/test/java/.../service/ContentContainerFactoryTest.java`

Run tests:
```bash
./gradlew test
```

## Configuration

Add to `local.properties`:
```properties
GEMINI_API_KEY=your_api_key_here
```

Get API key from: https://aistudio.google.com/app/apikey

## Performance Considerations

- **Synchronous Processing:** Current implementation is synchronous
  - Recommendation: Call from background thread
  - Consider using AsyncTask or Kotlin coroutines

- **Memory Usage:** Files read into memory
  - Current limit: 20MB per file
  - Future: Implement streaming for larger files

- **API Latency:** Generation takes 10-60 seconds depending on content
  - Show progress indicator in UI
  - Consider timeout handling

- **API Costs:** Each generation consumes API credits
  - Consider caching generated content
  - Implement smart regeneration (only when files change)

## Future Enhancements

1. **Large File Support**
   - Implement Gemini Files API for 2GB+ files
   - Add multipart upload support

2. **Chunking**
   - Split very large documents
   - Process in sections
   - Maintain context across chunks

3. **Caching**
   - Cache generated content
   - Invalidate on file changes
   - Reduce API costs

4. **Incremental Updates**
   - Add new content without full regeneration
   - Update existing content selectively

5. **Progress Callbacks**
   - Report progress during long operations
   - Allow cancellation

6. **Batch Processing**
   - Queue multiple subjects
   - Process in background

## Documentation

- **Usage Guide:** `../../../../../../GEMINI_CONTENT_GENERATION_GUIDE.md`
- **Implementation Summary:** `../../../../../../IMPLEMENTATION_SUMMARY.md`

## Dependencies

```gradle
implementation("com.google.genai:google-genai:1.29.0")
implementation("org.json:json:20231013")
```

Note: Implementation uses REST API, not SDK, for better compatibility.

## Support

For issues or questions:
1. Check GEMINI_CONTENT_GENERATION_GUIDE.md troubleshooting section
2. Review implementation summary
3. Examine unit tests for examples
4. Check Gemini API documentation: https://ai.google.dev/

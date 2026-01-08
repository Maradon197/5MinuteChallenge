# 5 Minute Challenge - Content Structure Documentation

## File Structure (ASCII Graphic)

```
5MinuteChallenge/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/a5minutechallenge/
│   │   │   │   ├── Activities/
│   │   │   │   │   ├── MainActivity.java              # Entry point, subject selection
│   │   │   │   │   ├── StartupActivity.java           # Initial startup screen
│   │   │   │   │   ├── SubjectListManager.java        # Displays list of subjects
│   │   │   │   │   ├── TopicListManager.java          # Displays topics for a subject
│   │   │   │   │   ├── FiveMinuteActivity.java        # Main learning activity (displays content)
│   │   │   │   │   ├── LessonOverActivity.java        # Shows results after completing lesson
│   │   │   │   │   ├── TimeOverActivity.java          # Shows results when time runs out
│   │   │   │   │   └── StorageActivity.java           # File storage management
│   │   │   │   │
│   │   │   │   ├── Models/
│   │   │   │   │   ├── Subject.java                   # Subject model with topics
│   │   │   │   │   ├── Topic.java                     # Topic model
│   │   │   │   │   ├── SubjectFile.java               # File attachment model
│   │   │   │   │   └── StorageListItem.java           # Storage item model
│   │   │   │   │
│   │   │   │   ├── ContentContainers/ (Base: ContentContainer.java)
│   │   │   │   │   ├── ContentContainer.java          # Abstract base class with Types enum
│   │   │   │   │   ├── TitleContainer.java            # TITLE - Displays topic title
│   │   │   │   │   ├── TextContainer.java             # TEXT - Displays informational text
│   │   │   │   │   ├── VideoContainer.java            # VIDEO - Video playback (TBD)
│   │   │   │   │   ├── QuizContainer.java             # QUIZ - Basic quiz (deprecated)
│   │   │   │   │   ├── MultipleChoiceQuizContainer.java  # MULTIPLE_CHOICE_QUIZ - MCQ questions
│   │   │   │   │   ├── ReverseQuizContainer.java      # REVERSE_QUIZ - Given answer, find question
│   │   │   │   │   ├── WireConnectingContainer.java   # WIRE_CONNECTING - Match left to right items
│   │   │   │   │   ├── FillInTheGapsContainer.java    # FILL_IN_THE_GAPS - Fill missing words
│   │   │   │   │   ├── SortingTaskContainer.java      # SORTING_TASK - Arrange items in order
│   │   │   │   │   ├── ErrorSpottingContainer.java    # ERROR_SPOTTING - Find incorrect item
│   │   │   │   │   └── RecapContainer.java            # RECAP - Wraps another container for review
│   │   │   │   │
│   │   │   │   ├── Adapters/
│   │   │   │   │   ├── ContentContainerAdapter.java   # Adapter for content containers (ListView)
│   │   │   │   │   └── TopicListAdapter.java          # Adapter for topic list
│   │   │   │   │
│   │   │   │   ├── Content/
│   │   │   │   │   └── ContentLoader.java             # Loads content by subject/topic
│   │   │   │   │
│   │   │   │   └── Managers/
│   │   │   │       ├── ScoreManager.java              # Handles scoring and streaks
│   │   │   │       ├── TimerManager.java              # 5-minute countdown timer
│   │   │   │       └── StorageListManager.java        # Storage management
│   │   │   │
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_five_minute.xml       # Main learning screen layout
│   │   │       │   ├── title_container.xml            # Layout for TITLE type
│   │   │       │   ├── text_container.xml             # Layout for TEXT type
│   │   │       │   ├── video_container.xml            # Layout for VIDEO type
│   │   │       │   ├── quiz_container.xml             # Layout for QUIZ type
│   │   │       │   ├── multiple_choice_quiz_container.xml  # Layout for MULTIPLE_CHOICE_QUIZ
│   │   │       │   ├── reverse_quiz_container.xml     # Layout for REVERSE_QUIZ
│   │   │       │   ├── wire_connecting_container.xml  # Layout for WIRE_CONNECTING
│   │   │       │   ├── fill_in_gaps_container.xml     # Layout for FILL_IN_THE_GAPS
│   │   │       │   ├── sorting_task_container.xml     # Layout for SORTING_TASK
│   │   │       │   ├── error_spotting_container.xml   # Layout for ERROR_SPOTTING
│   │   │       │   └── recap_container.xml            # Layout for RECAP
│   │   │       ├── anim/                              # Animation resources
│   │   │       ├── drawable/                          # Drawable resources
│   │   │       └── values/                            # Strings, colors, dimensions
│   │   │
│   │   └── test/                                      # Unit tests
│   │
│   └── build.gradle.kts                               # App-level Gradle configuration
│
├── build.gradle.kts                                   # Project-level Gradle configuration
└── settings.gradle.kts                                # Gradle settings
```

## Content Container Types

The app supports 11 different content container types for varied learning experiences:

### 1. TITLE Container
**Purpose**: Display the topic title at the start of each lesson
**Layout**: `title_container.xml`
**Key Views**: `title_text` (TextView)

### 2. TEXT Container
**Purpose**: Display informational text content
**Layout**: `text_container.xml`
**Key Views**: `text_content` (TextView)

### 3. VIDEO Container
**Purpose**: Video playback for visual learning (implementation pending)
**Layout**: `video_container.xml`

### 4. QUIZ Container (Deprecated)
**Purpose**: Basic quiz format (superseded by MULTIPLE_CHOICE_QUIZ)
**Layout**: `quiz_container.xml`

### 5. MULTIPLE_CHOICE_QUIZ Container
**Purpose**: Multiple choice questions with single or multiple correct answers
**Layout**: `multiple_choice_quiz_container.xml`
**Key Views**: 
- `question_text` (TextView)
- `options_recycler_view` (RecyclerView)
- `submit_button` (Button)
- `explanation_text` (TextView)

### 6. REVERSE_QUIZ Container
**Purpose**: Given an answer, user selects the matching question
**Layout**: `reverse_quiz_container.xml`
**Key Views**:
- `answer_text` (TextView)
- `question_options_recycler_view` (RecyclerView)
- `submit_button` (Button)

### 7. WIRE_CONNECTING Container
**Purpose**: Match items from left column to right column (e.g., terms to definitions)
**Layout**: `wire_connecting_container.xml`
**Key Views**:
- `instructions_text` (TextView)
- `left_items_recycler_view` (RecyclerView)
- `right_items_recycler_view` (RecyclerView)
- `check_button` (Button)

### 8. FILL_IN_THE_GAPS Container
**Purpose**: Duolingo-style exercise where users fill in missing words
**Layout**: `fill_in_gaps_container.xml`
**Key Views**:
- `text_with_gaps` (TextView)
- `word_options_chip_group` (ChipGroup)
- `check_button` (Button)

### 9. SORTING_TASK Container
**Purpose**: Arrange items in the correct order by dragging
**Layout**: `sorting_task_container.xml`
**Key Views**:
- `instructions_text` (TextView)
- `sortable_items_recycler_view` (RecyclerView)
- `check_button` (Button)

### 10. ERROR_SPOTTING Container
**Purpose**: Identify the incorrect/outlier item from a list
**Layout**: `error_spotting_container.xml`
**Key Views**:
- `instructions_text` (TextView)
- `items_recycler_view` (RecyclerView)
- `submit_button` (Button)
- `explanation_text` (TextView)

### 11. RECAP Container
**Purpose**: Wrapper that highlights content as a recap/review section
**Layout**: `recap_container.xml`
**Key Views**:
- `recap_title` (TextView)
- `wrapped_container_frame` (FrameLayout)

## How to Populate Content Containers from Backend

### Content Flow Architecture

```
Backend API/Database
        ↓
ContentLoader.java
        ↓
List<ContentContainer>
        ↓
ContentContainerAdapter
        ↓
FiveMinuteActivity (ListView)
```

### Step 1: Backend Data Structure

Your backend should provide content in a structured format (JSON example):

```json
{
  "subjectId": 0,
  "topicName": "Shortcuts",
  "containers": [
    {
      "id": 0,
      "type": "TITLE",
      "data": {
        "title": "Shortcuts"
      }
    },
    {
      "id": 1,
      "type": "TEXT",
      "data": {
        "text": "Keyboard shortcuts are essential..."
      }
    },
    {
      "id": 2,
      "type": "MULTIPLE_CHOICE_QUIZ",
      "data": {
        "question": "What is the shortcut for Search Everywhere?",
        "options": ["Ctrl+Shift+F", "Double Shift", "Ctrl+N"],
        "correctAnswerIndices": [1],
        "allowMultipleAnswers": false,
        "explanationText": "Double Shift opens Search Everywhere..."
      }
    },
    {
      "id": 3,
      "type": "REVERSE_QUIZ",
      "data": {
        "answer": "Ctrl+Shift+F",
        "questionOptions": [
          "What opens Search Everywhere?",
          "What is the shortcut for Find in Files?",
          "What opens Run menu?"
        ],
        "correctQuestionIndex": 1,
        "explanationText": "Ctrl+Shift+F searches across all files..."
      }
    },
    {
      "id": 4,
      "type": "FILL_IN_THE_GAPS",
      "data": {
        "textTemplate": "Press {} to comment. Press {} to duplicate.",
        "correctWords": ["Ctrl+/", "Ctrl+D"],
        "wordOptions": ["Ctrl+/", "Ctrl+D", "Ctrl+Z", "Ctrl+X"]
      }
    },
    {
      "id": 5,
      "type": "WIRE_CONNECTING",
      "data": {
        "instructions": "Match the shortcut with its action:",
        "leftItems": ["Ctrl+Alt+L", "Alt+Enter", "Ctrl+W"],
        "rightItems": ["Reformat Code", "Show Actions", "Extend Selection"],
        "correctMatches": {
          "0": 0,
          "1": 1,
          "2": 2
        }
      }
    },
    {
      "id": 6,
      "type": "SORTING_TASK",
      "data": {
        "instructions": "Order by complexity:",
        "correctOrder": [
          "Rename (Shift+F6)",
          "Extract Variable",
          "Extract Method"
        ]
      }
    },
    {
      "id": 7,
      "type": "ERROR_SPOTTING",
      "data": {
        "instructions": "Find the incorrect shortcut:",
        "items": [
          "Ctrl+N - Navigate to Class",
          "Ctrl+Shift+N - Navigate to File",
          "Ctrl+G - Go to Implementation"
        ],
        "errorIndex": 2,
        "explanationText": "Ctrl+G goes to Line, not Implementation."
      }
    },
    {
      "id": 8,
      "type": "RECAP",
      "data": {
        "recapTitle": "Shortcuts Recap",
        "wrappedContainer": {
          "type": "TEXT",
          "data": {
            "text": "You learned essential shortcuts..."
          }
        }
      }
    }
  ]
}
```

### Step 2: Modify ContentLoader.java

Replace the static content in `ContentLoader.java` with API calls:

```java
public static List<ContentContainer> loadContent(int subjectId, String topicName) {
    List<ContentContainer> containers = new ArrayList<>();
    
    try {
        // Make API call to backend
        String url = API_BASE_URL + "/content?subjectId=" + subjectId + "&topic=" + topicName;
        JSONObject response = makeAPICall(url); // Implement your HTTP client
        
        JSONArray containersArray = response.getJSONArray("containers");
        
        for (int i = 0; i < containersArray.length(); i++) {
            JSONObject containerJson = containersArray.getJSONObject(i);
            ContentContainer container = parseContainer(containerJson);
            if (container != null) {
                containers.add(container);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        // Fall back to default content on error
        return loadDefaultContent(topicName);
    }
    
    return containers;
}

private static ContentContainer parseContainer(JSONObject json) throws Exception {
    String type = json.getString("type");
    int id = json.getInt("id");
    JSONObject data = json.getJSONObject("data");
    
    switch (type) {
        case "TITLE":
            return new TitleContainer(id)
                .setTitle(data.getString("title"));
                
        case "TEXT":
            return new TextContainer(id)
                .setText(data.getString("text"));
                
        case "MULTIPLE_CHOICE_QUIZ":
            MultipleChoiceQuizContainer mcq = new MultipleChoiceQuizContainer(id);
            mcq.setQuestion(data.getString("question"));
            mcq.setOptions(jsonArrayToList(data.getJSONArray("options")));
            mcq.setCorrectAnswerIndices(jsonArrayToIntList(data.getJSONArray("correctAnswerIndices")));
            if (data.has("explanationText")) {
                mcq.setExplanationText(data.getString("explanationText"));
            }
            return mcq;
            
        case "REVERSE_QUIZ":
            ReverseQuizContainer reverseQuiz = new ReverseQuizContainer(id);
            reverseQuiz.setAnswer(data.getString("answer"));
            reverseQuiz.setQuestionOptions(jsonArrayToList(data.getJSONArray("questionOptions")));
            reverseQuiz.setCorrectQuestionIndex(data.getInt("correctQuestionIndex"));
            return reverseQuiz;
            
        case "FILL_IN_THE_GAPS":
            FillInTheGapsContainer fillGaps = new FillInTheGapsContainer(id);
            fillGaps.setTextTemplate(data.getString("textTemplate"));
            fillGaps.setCorrectWords(jsonArrayToList(data.getJSONArray("correctWords")));
            fillGaps.setWordOptions(jsonArrayToList(data.getJSONArray("wordOptions")));
            return fillGaps;
            
        case "WIRE_CONNECTING":
            WireConnectingContainer wireConnect = new WireConnectingContainer(id);
            wireConnect.setInstructions(data.getString("instructions"));
            wireConnect.setLeftItems(jsonArrayToList(data.getJSONArray("leftItems")));
            wireConnect.setRightItems(jsonArrayToList(data.getJSONArray("rightItems")));
            wireConnect.setCorrectMatches(jsonObjectToIntMap(data.getJSONObject("correctMatches")));
            return wireConnect;
            
        case "SORTING_TASK":
            SortingTaskContainer sortTask = new SortingTaskContainer(id);
            sortTask.setInstructions(data.getString("instructions"));
            sortTask.setCorrectOrder(jsonArrayToList(data.getJSONArray("correctOrder")));
            return sortTask;
            
        case "ERROR_SPOTTING":
            ErrorSpottingContainer errorSpot = new ErrorSpottingContainer(id);
            errorSpot.setInstructions(data.getString("instructions"));
            errorSpot.setItems(jsonArrayToList(data.getJSONArray("items")));
            errorSpot.setErrorIndex(data.getInt("errorIndex"));
            if (data.has("explanationText")) {
                errorSpot.setExplanationText(data.getString("explanationText"));
            }
            return errorSpot;
            
        case "RECAP":
            RecapContainer recap = new RecapContainer(id);
            recap.setRecapTitle(data.getString("recapTitle"));
            if (data.has("wrappedContainer")) {
                recap.setWrappedContainer(parseContainer(data.getJSONObject("wrappedContainer")));
            }
            return recap;
            
        default:
            return null;
    }
}

// Helper methods
private static List<String> jsonArrayToList(JSONArray array) throws Exception {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
        list.add(array.getString(i));
    }
    return list;
}

private static List<Integer> jsonArrayToIntList(JSONArray array) throws Exception {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
        list.add(array.getInt(i));
    }
    return list;
}

private static Map<Integer, Integer> jsonObjectToIntMap(JSONObject obj) throws Exception {
    Map<Integer, Integer> map = new HashMap<>();
    Iterator<String> keys = obj.keys();
    while (keys.hasNext()) {
        String key = keys.next();
        map.put(Integer.parseInt(key), obj.getInt(key));
    }
    return map;
}
```

### Step 3: Backend Implementation Checklist

When implementing the backend, ensure:

1. **Content Validation**: Validate that container data matches expected structure
2. **ID Management**: Ensure unique IDs for each container in a lesson
3. **Correct Answer Security**: Don't send correct answers to client initially for quiz types
4. **Answer Verification**: Implement server-side answer checking endpoint
5. **Progress Tracking**: Track user progress through topics
6. **Content Versioning**: Support content updates without breaking old clients

### Step 4: Interactive Container Handling

For interactive containers (quizzes, tasks), you'll need to:

1. **Set up click listeners** in ContentContainerAdapter or FiveMinuteActivity
2. **Validate answers** when user submits
3. **Update UI** to show correct/incorrect feedback
4. **Call score manager** to update points and streaks
5. **Enable progression** to next container after answering

Example for MULTIPLE_CHOICE_QUIZ:

```java
// In ContentContainerAdapter.getView() or a custom ViewHolder
Button submitButton = convertView.findViewById(R.id.submit_button);
submitButton.setOnClickListener(v -> {
    MultipleChoiceQuizContainer container = (MultipleChoiceQuizContainer) contentContainer;
    if (container.isCorrect()) {
        ((FiveMinuteActivity) getContext()).onCorrectAnswer(answerTimeMs);
        // Show correct feedback
    } else {
        ((FiveMinuteActivity) getContext()).onIncorrectAnswer();
        // Show incorrect feedback
    }
});
```

## Current Implementation Status

### Implemented ✅
- All 11 container types defined with data models
- All layout XML files created
- ContentContainerAdapter updated to handle all types (basic view binding)
- ContentLoader with comprehensive Jetbrains IDE examples
- Scoring and timer systems

### To Implement 🔧
- Interactive functionality for quiz/task containers (click listeners, answer validation)
- RecyclerView adapters for options lists in quiz containers
- Drag-and-drop for sorting tasks
- Touch interaction for wire connecting
- Backend API integration
- Video container playback functionality

## Testing Your Content

To test new content:

1. Add your content in `ContentLoader.loadContent()` method
2. Run the app and select the subject
3. Click on the topic to start the 5-minute challenge
4. Verify each container displays correctly
5. Test interactive elements (buttons, selections)

## Example: Adding a New Subject

```java
// In ContentLoader.java
public static List<ContentContainer> loadContent(int subjectId, String topicName) {
    // ...
    if (subjectId == 0) {
        return loadJetbrainsIDEContent(topicName);
    } else if (subjectId == 4) { // New subject
        return loadYourNewSubjectContent(topicName);
    }
    // ...
}

private static List<ContentContainer> loadYourNewSubjectContent(String topicName) {
    List<ContentContainer> containers = new ArrayList<>();
    int id = 0;
    
    containers.add(new TitleContainer(id++).setTitle(topicName));
    containers.add(new TextContainer(id++)
        .setText("Introduction to your topic..."));
    
    // Add more containers...
    
    return containers;
}

// In Subject.java, add the subject in getTopics() switch statement
case 4: // Your new subject
    topics.add(new Topic("Topic 1"));
    topics.add(new Topic("Topic 2"));
    break;
```

## Troubleshooting

### App crashes when clicking topic
- **Cause**: ContentContainerAdapter doesn't handle all container types
- **Fix**: Ensure all types in ContentContainer.Types enum have cases in both switch statements in ContentContainerAdapter.getView()

### Container displays incorrectly
- **Cause**: Layout inflation or view binding issue
- **Fix**: Verify layout file exists and view IDs match those used in adapter

### Content not loading
- **Cause**: ContentLoader returning empty list or null
- **Fix**: Check ContentLoader logic and ensure it returns valid containers for your subject/topic combination

---

For more information or questions, refer to the source code or contact the development team.

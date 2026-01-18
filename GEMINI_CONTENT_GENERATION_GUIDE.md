# Gemini Content Generation Service - Usage Guide

## Overview
This implementation provides automated content generation for the 5-Minute Challenge app using Google's Gemini AI. The service processes uploaded documents (PDFs, text files, etc.) and generates structured learning content.

## Architecture: 3-Stage Multi-Prompt Pipeline

The service uses a **semantic, multi-prompt** approach to maximize accuracy and minimize token usage:

### Stage 0: Semantic Document Analysis
- **Input**: Full document text with page markers
- **Output**: Logical sections (chapters, topics) with exact page ranges
- **Purpose**: LLM identifies natural content boundaries instead of arbitrary page cuts

### Stage 1: Topic Extraction  
- **Input**: Document + semantic sections from Stage 0
- **Output**: Learning topics mapped to section page ranges
- **Purpose**: Creates meaningful learning units grouped by related sections

### Stage 2: Content Generation (Parallelized)
- **Input**: Only the pages relevant to each topic
- **Output**: Challenges and containers using TOON format
- **Purpose**: Focused context = better quality content + lower token costs

```
┌─────────────────────────────────────────────────────────────────┐
│                        PDF Document                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Page 1   │ │ Page 2   │ │ Page 3   │ │ Page 4   │  ...      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Stage 0: Semantic Analysis                                      │
│  "Identify chapters/sections and their page ranges"             │
│                                                                  │
│  Output: [                                                       │
│    { "s": "Introduction", "sp": 1, "ep": 2 },                   │
│    { "s": "Core Concepts", "sp": 3, "ep": 8 },                  │
│    { "s": "Advanced Topics", "sp": 9, "ep": 15 }                │
│  ]                                                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Stage 1: Topic Extraction                                       │
│  "What learning topics exist? Map to sections."                 │
│                                                                  │
│  Output: [                                                       │
│    { "t": "Getting Started", "refs": [{"sp": 1, "ep": 2}] },    │
│    { "t": "Fundamentals", "refs": [{"sp": 3, "ep": 5}] },       │
│    { "t": "Deep Dive", "refs": [{"sp": 6, "ep": 15}] }          │
│  ]                                                               │
└─────────────────────────────────────────────────────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            ▼                 ▼                 ▼
┌───────────────────┐ ┌───────────────────┐ ┌───────────────────┐
│ Stage 2: Topic 1  │ │ Stage 2: Topic 2  │ │ Stage 2: Topic 3  │
│ (Pages 1-2 only)  │ │ (Pages 3-5 only)  │ │ (Pages 6-15 only) │
│                   │ │                   │ │                   │
│ → Challenges      │ │ → Challenges      │ │ → Challenges      │
│ → Containers      │ │ → Containers      │ │ → Containers      │
└───────────────────┘ └───────────────────┘ └───────────────────┘
            │                 │                 │
            └─────────────────┼─────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Final JSON: { "topics": [...] }                                 │
└─────────────────────────────────────────────────────────────────┘
```

## TOON (Token Oriented Object Notation)

To reduce API costs, Stage 2 requests output in a compact format:

| Standard Key | TOON Key | Description |
|---|---|---|
| title | t | Title of topic/challenge |
| description | d | Brief description |
| challenges | cs | Array of challenges |
| containers | cn | Array of containers |
| type | ty | Container type |
| text | tx | Text content |
| question | q | Quiz question |
| options | os | Answer options |
| correctAnswerIndices | ci | Correct answer indices |
| explanationText | e | Explanation |
| instructions | in | Task instructions |

The service automatically expands TOON to the full schema before saving.

## Benefits of This Architecture

1. **No Content Loss**: Semantic splitting ensures nothing falls between arbitrary page boundaries
2. **Token Efficiency**: Stage 2 only sends relevant pages (e.g., 5 pages instead of 50)
3. **Parallel Processing**: All topics generated simultaneously  
4. **Better Quality**: Focused context = more precise content generation
5. **Flexibility**: LLM decides section boundaries based on actual content structure

## Usage Example

```java
Subject subject = new Subject(1);
subject.setTitle("Machine Learning Fundamentals");

// Upload PDF files...
// ...

// Generate content (uses 3-stage pipeline automatically)
boolean success = subject.generateContentFromFiles(context);

if (success) {
    ArrayList<Topic> topics = subject.getTopics();
    // Topics are populated with challenges and containers
}
```

## Configuration

Key constants in `GeminiContentProcessor`:

```java
MAX_FILE_SIZE = 50 * 1024 * 1024;  // 50MB max file size
MAX_RETRY_DURATION_MS = 30 * 60 * 1000;  // 30 min retry window
```

## Error Handling

- **Rate Limiting (429)**: Automatic exponential backoff up to 30 minutes
- **No Sections Found**: Falls back to treating entire document as one section
- **No Pages Match Topic**: Falls back to using all content
- **JSON Parse Errors**: Cleaned by removing markdown code blocks

## Troubleshooting

### "No readable content found in files"
- Verify PDF is text-based (not scanned images)
- Check file size is under 50MB

### Topics missing content
- Check logs for "No pages matched for topic" warnings
- Semantic analysis may have incorrect page ranges - check Stage 0 output

### Slow generation
- Large PDFs with many pages take longer for Stage 0
- Consider splitting very large PDFs (200+ pages) before upload

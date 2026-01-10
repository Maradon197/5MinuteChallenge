/** Service for processing files with Google's Gemini 2.5 Pro API.
 * Handles file uploads and structured content generation.
 */
package com.example.a5minutechallenge.service;

import android.content.Context;
import android.util.Log;

import com.example.a5minutechallenge.BuildConfig;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;
import com.google.genai.GenerativeModel;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.TextPart;
import com.google.genai.types.FileData;
import com.google.genai.types.FileUploadResponse;
import com.google.genai.types.GenerationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GeminiContentProcessor {
    
    private static final String TAG = "GeminiContentProcessor";
    private static final String MODEL_NAME = "gemini-2.5-pro-latest";
    private static final int MAX_TOKENS = 1000000; // 1M token context window
    
    private final GenerativeModel model;
    
    public GeminiContentProcessor() {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("null")) {
            throw new IllegalStateException("GEMINI_API_KEY not configured in local.properties");
        }
        
        this.model = new GenerativeModel(MODEL_NAME, apiKey, new GenerationConfig.Builder()
                .temperature(0.7f)
                .topK(40)
                .topP(0.95f)
                .maxOutputTokens(8192)
                .build());
    }
    
    /**
     * Processes uploaded files and generates structured learning content
     * @param files List of SubjectFile objects to process
     * @param subjectTitle Title of the subject for context
     * @return JSON string containing the generated content structure
     * @throws IOException if file processing fails
     * @throws JSONException if JSON generation fails
     */
    public String processFiles(List<SubjectFile> files, String subjectTitle) throws IOException, JSONException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for processing");
        }
        
        // Build the prompt
        String prompt = buildPrompt(subjectTitle);
        
        // Prepare content parts including files
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(prompt));
        
        // Add file references
        for (SubjectFile subjectFile : files) {
            File file = subjectFile.getFile();
            if (!file.exists()) {
                Log.w(TAG, "File does not exist: " + file.getAbsolutePath());
                continue;
            }
            
            // For large files, we'd use the Files API
            // For now, we'll process files directly
            // TODO: Implement file upload for files > 20MB
            parts.add(createFilePartFromFile(file));
        }
        
        // Generate content
        Content content = new Content.Builder().addParts(parts).build();
        
        try {
            GenerateContentResponse response = model.generateContent(content);
            String responseText = response.getText();
            
            // Extract JSON from response (Gemini might wrap it in markdown)
            return extractJson(responseText);
        } catch (Exception e) {
            Log.e(TAG, "Error generating content", e);
            throw new IOException("Failed to generate content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds the structured prompt for Gemini
     */
    private String buildPrompt(String subjectTitle) {
        return "You are an expert educational content creator. Analyze the provided files and create comprehensive, structured learning content.\n\n" +
                "Subject: " + subjectTitle + "\n\n" +
                "CRITICAL REQUIREMENTS:\n" +
                "1. Extract EVERY piece of information from the files - DO NOT summarize or omit anything\n" +
                "2. Preserve all technical terminology, examples, and details exactly as they appear\n" +
                "3. Organize content into Topics that group related concepts\n" +
                "4. Split each Topic into multiple 5-minute Challenges (each with 3-7 content containers)\n" +
                "5. Use diverse container types for engagement and learning effectiveness\n" +
                "6. Ensure technical accuracy and maintain the original depth of information\n\n" +
                "OUTPUT FORMAT:\n" +
                "Return ONLY valid JSON matching this exact schema (no markdown, no additional text):\n\n" +
                "{\n" +
                "  \"topics\": [\n" +
                "    {\n" +
                "      \"title\": \"Topic Name\",\n" +
                "      \"challenges\": [\n" +
                "        {\n" +
                "          \"title\": \"Challenge Name\",\n" +
                "          \"description\": \"Brief description\",\n" +
                "          \"containers\": [\n" +
                "            // Container objects - see types below\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "CONTAINER TYPES:\n\n" +
                "1. TITLE (for section headings):\n" +
                "{\n" +
                "  \"type\": \"TITLE\",\n" +
                "  \"title\": \"Section heading\"\n" +
                "}\n\n" +
                "2. TEXT (for explanations):\n" +
                "{\n" +
                "  \"type\": \"TEXT\",\n" +
                "  \"text\": \"Detailed content\"\n" +
                "}\n\n" +
                "3. MULTIPLE_CHOICE_QUIZ:\n" +
                "{\n" +
                "  \"type\": \"MULTIPLE_CHOICE_QUIZ\",\n" +
                "  \"question\": \"Question text\",\n" +
                "  \"options\": [\"Option 1\", \"Option 2\", \"Option 3\", \"Option 4\"],\n" +
                "  \"correctAnswerIndices\": [0],\n" +
                "  \"allowMultipleAnswers\": false,\n" +
                "  \"explanationText\": \"Why this is correct\"\n" +
                "}\n\n" +
                "4. FILL_IN_THE_GAPS:\n" +
                "{\n" +
                "  \"type\": \"FILL_IN_THE_GAPS\",\n" +
                "  \"textTemplate\": \"Text with {} placeholders for {} gaps\",\n" +
                "  \"correctWords\": [\"word1\", \"word2\"],\n" +
                "  \"wordOptions\": [\"word1\", \"word2\", \"distractor1\", \"distractor2\"]\n" +
                "}\n\n" +
                "5. SORTING_TASK:\n" +
                "{\n" +
                "  \"type\": \"SORTING_TASK\",\n" +
                "  \"instructions\": \"Put in correct order\",\n" +
                "  \"correctOrder\": [\"First\", \"Second\", \"Third\"]\n" +
                "}\n\n" +
                "6. ERROR_SPOTTING:\n" +
                "{\n" +
                "  \"type\": \"ERROR_SPOTTING\",\n" +
                "  \"instructions\": \"Find the error\",\n" +
                "  \"items\": [\"Item1\", \"Item2\", \"Incorrect item\", \"Item3\"],\n" +
                "  \"errorIndex\": 2,\n" +
                "  \"explanationText\": \"Why it's wrong\"\n" +
                "}\n\n" +
                "7. REVERSE_QUIZ:\n" +
                "{\n" +
                "  \"type\": \"REVERSE_QUIZ\",\n" +
                "  \"answer\": \"The answer\",\n" +
                "  \"questionOptions\": [\"Question 1?\", \"Question 2?\", \"Correct question?\"],\n" +
                "  \"correctQuestionIndex\": 2,\n" +
                "  \"explanationText\": \"Why this question fits\"\n" +
                "}\n\n" +
                "8. WIRE_CONNECTING:\n" +
                "{\n" +
                "  \"type\": \"WIRE_CONNECTING\",\n" +
                "  \"instructions\": \"Match items\",\n" +
                "  \"leftItems\": [\"Term 1\", \"Term 2\"],\n" +
                "  \"rightItems\": [\"Definition A\", \"Definition B\"],\n" +
                "  \"correctMatches\": {\"0\": 1, \"1\": 0}\n" +
                "}\n\n" +
                "9. RECAP:\n" +
                "{\n" +
                "  \"type\": \"RECAP\",\n" +
                "  \"recapTitle\": \"Review\",\n" +
                "  \"wrappedContainer\": { /* any container type */ }\n" +
                "}\n\n" +
                "IMPORTANT:\n" +
                "- Each Challenge should take approximately 5 minutes to complete\n" +
                "- Include 3-7 containers per Challenge\n" +
                "- Vary container types to maintain engagement\n" +
                "- Preserve all information from source files\n" +
                "- Output ONLY valid JSON, no markdown formatting\n";
    }
    
    /**
     * Creates a Part from a file
     */
    private Part createFilePartFromFile(File file) {
        // For simplicity, we'll use FileData
        // In a real implementation, we'd detect mime type
        String mimeType = detectMimeType(file.getName());
        return new FileData(file.toURI().toString(), mimeType);
    }
    
    /**
     * Detects MIME type from file extension
     */
    private String detectMimeType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
            return "application/msword";
        } else if (lowerName.endsWith(".md")) {
            return "text/markdown";
        }
        return "application/octet-stream";
    }
    
    /**
     * Extracts JSON from response, handling markdown code blocks
     */
    private String extractJson(String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            return "{}";
        }
        
        // Remove markdown code blocks if present
        String cleaned = responseText.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }
    
    /**
     * Validates that the API key is configured
     */
    public static boolean isApiKeyConfigured() {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("null");
    }
}

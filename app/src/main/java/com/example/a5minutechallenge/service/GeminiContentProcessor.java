/** Service for processing files with Google's Gemini 2.5 Pro API.
 * Handles file uploads and structured content generation.
 */
package com.example.a5minutechallenge.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.a5minutechallenge.BuildConfig;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeminiContentProcessor {
    
    private static final String TAG = "GeminiContentProcessor";
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent";
    private static final String COUNT_TOKENS_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:countTokens";
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // Increased to 50MB
    private static final int MAX_TOKENS_PER_PART = 90000;
    
    private final String apiKey;
    
    public GeminiContentProcessor() {
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("null")) {
            throw new IllegalStateException("GEMINI_API_KEY not configured in local.properties");
        }
    }
    
    /**
     * Processes uploaded files and generates structured learning content
     * @param files List of SubjectFile objects to process
     * @param subjectTitle Title of the subject for context
     * @return JSON string containing the generated content structure
     * @throws IOException if file processing fails
     * @throws JSONException if JSON generation fails
     */
    public String processFiles(List<SubjectFile> files, String subjectTitle, Context context) throws IOException, JSONException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for processing");
        }
        
        // Combine all file contents
        StringBuilder fullContent = new StringBuilder();
        for (SubjectFile subjectFile : files) {
            File file = subjectFile.getFile();
            if (file.exists() && file.length() < MAX_FILE_SIZE) {
                fullContent.append("\n\n=== File: ").append(subjectFile.getFileName()).append(" ===\n");
                fullContent.append(readFileContent(file));
            }
        }
        
        if (fullContent.length() == 0) {
            throw new IOException("No readable content found in files");
        }

        // Split content into parts based on token count
        List<String> contentParts = splitContentIntoParts(fullContent.toString());
        
        // Process parts in parallel for maximum productivity
        ExecutorService partExecutor = Executors.newFixedThreadPool(Math.min(contentParts.size(), 5));
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < contentParts.size(); i++) {
            final int partIndex = i;
            final String partContent = contentParts.get(i);
            final int totalParts = contentParts.size();
            
            futures.add(partExecutor.submit(() -> {
                String partInfo = totalParts > 1 ? " (Part " + (partIndex + 1) + " of " + totalParts + ")" : "";
                String prompt = buildPrompt(subjectTitle + partInfo);
                
                JSONObject request = new JSONObject();
                
                // Add text prompt as first part
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);
                
                JSONObject fileTextPart = new JSONObject();
                fileTextPart.put("text", partContent);
                
                request.put("contents", new org.json.JSONArray()
                        .put(new JSONObject().put("parts", new org.json.JSONArray()
                                .put(textPart)
                                .put(fileTextPart))));
                
                // Add generation config
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.4);
                generationConfig.put("topK", 40);
                generationConfig.put("topP", 0.95);
                generationConfig.put("maxOutputTokens", 8192);
                request.put("generationConfig", generationConfig);
                
                // Make API call
                String response = makeApiCall(API_ENDPOINT, request);
                
                // Extract JSON from response
                return extractJsonFromResponse(response);
            }));
        }
        
        List<String> jsonResponses = new ArrayList<>();
        try {
            for (Future<String> future : futures) {
                jsonResponses.add(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to process content parts in parallel: " + e.getMessage(), e);
        } finally {
            partExecutor.shutdown();
        }
        
        // Merge results
        return mergeJsonResponses(jsonResponses);
    }
    
    /**
     * Reads file content as text
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Makes API call to Gemini
     */
    private String makeApiCall(String endpoint, JSONObject request) throws IOException, JSONException {
        URL url = new URL(endpoint + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorBody = readErrorResponse(conn);
                throw new IOException("API call failed with code " + responseCode + ": " + errorBody);
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Reads error response from API
     */
    private String readErrorResponse(HttpURLConnection conn) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            return "Unable to read error response";
        }
    }
    
    /**
     * Extracts the generated text from API response
     */
    private String extractJsonFromResponse(String responseText) throws JSONException {
        JSONObject response = new JSONObject(responseText);
        
        if (response.has("candidates")) {
            org.json.JSONArray candidates = response.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                if (candidate.has("content")) {
                    JSONObject content = candidate.getJSONObject("content");
                    if (content.has("parts")) {
                        org.json.JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            JSONObject part = parts.getJSONObject(0);
                            if (part.has("text")) {
                                String text = part.getString("text");
                                return extractJson(text);
                            }
                        }
                    }
                }
            }
        }
        
        throw new JSONException("Unable to extract text from API response");
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

    private List<String> splitContentIntoParts(String content) throws IOException, JSONException {
        List<String> parts = new ArrayList<>();
        String[] lines = content.split("\n");
        int startIndex = 0;
        
        Log.d(TAG, "Splitting content of " + content.length() + " chars into parts...");

        while (startIndex < lines.length) {
            int low = startIndex;
            int high = lines.length;
            int bestEnd = startIndex + 1;
            
            // Binary search for the largest chunk that fits in MAX_TOKENS_PER_PART
            while (low <= high) {
                int mid = (low + high) / 2;
                if (mid <= startIndex) {
                    low = mid + 1;
                    continue;
                }
                
                StringBuilder sb = new StringBuilder();
                for (int i = startIndex; i < mid; i++) {
                    sb.append(lines[i]).append("\n");
                }
                
                int tokens = countTokens(sb.toString());
                if (tokens <= MAX_TOKENS_PER_PART) {
                    bestEnd = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            
            StringBuilder partBuffer = new StringBuilder();
            for (int i = startIndex; i < bestEnd; i++) {
                partBuffer.append(lines[i]).append("\n");
            }
            parts.add(partBuffer.toString());
            Log.d(TAG, "Added part with " + (bestEnd - startIndex) + " lines");
            startIndex = bestEnd;
        }
        
        return parts;
    }

    private int countTokens(String text) throws IOException, JSONException {
        JSONObject textPart = new JSONObject();
        textPart.put("text", text);
        
        JSONObject content = new JSONObject();
        content.put("parts", new org.json.JSONArray().put(textPart));
        
        JSONObject request = new JSONObject();
        request.put("contents", new org.json.JSONArray().put(content));
        
        String response = makeApiCall(COUNT_TOKENS_ENDPOINT, request);
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getInt("totalTokens");
    }

    private String mergeJsonResponses(List<String> responses) throws JSONException {
        if (responses.isEmpty()) return "{\"topics\": []}";
        if (responses.size() == 1) return responses.get(0);
        
        JSONObject first = new JSONObject(responses.get(0));
        org.json.JSONArray allTopics = first.optJSONArray("topics");
        if (allTopics == null) {
            allTopics = new org.json.JSONArray();
            first.put("topics", allTopics);
        }
        
        // Map to keep track of topics by title for merging
        Map<String, JSONObject> topicMap = new HashMap<>();
        for (int i = 0; i < allTopics.length(); i++) {
            JSONObject topic = allTopics.getJSONObject(i);
            topicMap.put(topic.optString("title"), topic);
        }
        
        for (int i = 1; i < responses.size(); i++) {
            JSONObject next = new JSONObject(responses.get(i));
            org.json.JSONArray nextTopics = next.optJSONArray("topics");
            if (nextTopics != null) {
                for (int j = 0; j < nextTopics.length(); j++) {
                    JSONObject nextTopic = nextTopics.getJSONObject(j);
                    String title = nextTopic.optString("title");
                    
                    if (topicMap.containsKey(title)) {
                        // Merge challenges into existing topic
                        JSONObject existingTopic = topicMap.get(title);
                        org.json.JSONArray existingChallenges = existingTopic.optJSONArray("challenges");
                        org.json.JSONArray nextChallenges = nextTopic.optJSONArray("challenges");
                        
                        if (existingChallenges != null && nextChallenges != null) {
                            for (int k = 0; k < nextChallenges.length(); k++) {
                                existingChallenges.put(nextChallenges.get(k));
                            }
                        } else if (nextChallenges != null) {
                            existingTopic.put("challenges", nextChallenges);
                        }
                    } else {
                        // Add new topic
                        allTopics.put(nextTopic);
                        topicMap.put(title, nextTopic);
                    }
                }
            }
        }
        
        return first.toString();
    }
}

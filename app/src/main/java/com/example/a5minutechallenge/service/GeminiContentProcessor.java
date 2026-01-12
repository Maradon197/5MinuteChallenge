/** Service for processing files with Google's Gemini 2.0 Pro API.
 * Handles file uploads and structured content generation.
 */
package com.example.a5minutechallenge.service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.a5minutechallenge.BuildConfig;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;

import org.json.JSONException;
import org.json.JSONObject;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GeminiContentProcessor {
    
    private static final String TAG = "GeminiContentProcessor";
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String COUNT_TOKENS_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:countTokens";
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // Increased to 50MB
    private static final int MAX_TOKENS_PER_PART = 300000;
    private static final int PDF_PAGES_PER_CHUNK = 50;
    private static final long MAX_RETRY_DURATION_MS = 30 * 60 * 1000; // 30 minutes
    private static final long INITIAL_RETRY_DELAY_MS = 2000; // 2 seconds
    private static final long MAX_RETRY_DELAY_MS = 30000; // 30 seconds    
    
    private final String apiKey;
    private final AtomicLong totalTokensProcessed = new AtomicLong(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final AtomicInteger rateLimitedThreads = new AtomicInteger(0);
    
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
        // Initialize PDFBox resource loader so embedded glyphlists and other
        // PDF resources are available on Android at runtime.
        try {
            PDFBoxResourceLoader.init(context);
        } catch (Exception e) {
            Log.w(TAG, "PDFBoxResourceLoader.init failed: " + e.getMessage());
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for processing");
        }
        
        List<JSONObject> allParts = new ArrayList<>();
        List<String> textBlocks = new ArrayList<>();
        StringBuilder textCollector = new StringBuilder();

        for (SubjectFile subjectFile : files) {
            File file = subjectFile.getFile();
            if (!file.exists() || file.length() >= MAX_FILE_SIZE) {
                continue;
            }

            String mimeType = getMimeType(file);
            if ("application/pdf".equals(mimeType)) {
                flushTextCollector(textCollector, textBlocks);
                List<PdfChunk> pdfChunks = extractPdfChunks(file, PDF_PAGES_PER_CHUNK);
                for (PdfChunk chunk : pdfChunks) {
                    textBlocks.add(String.format("\n\n=== File: %s (Pages %d-%d) ===\n%s",
                            subjectFile.getFileName(), chunk.startPage, chunk.endPage, chunk.text));
                }
            } else if (isBinaryMimeType(mimeType)) {
                flushTextCollector(textCollector, textBlocks);
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", mimeType);
                inlineData.put("data", fileToBase64(file));

                JSONObject part = new JSONObject();
                part.put("inline_data", inlineData);
                allParts.add(part);
            } else {
                textCollector.append("\n\n=== File: ").append(subjectFile.getFileName()).append(" ===\n");
                textCollector.append(readFileContent(file));
            }
        }
        flushTextCollector(textCollector, textBlocks);

        for (String block : textBlocks) {
            List<String> textSegments = splitTextIntoSegments(block);
            for (String segment : textSegments) {
                JSONObject part = new JSONObject();
                part.put("text", segment);
                allParts.add(part);
            }
        }

        if (allParts.isEmpty()) {
            throw new IOException("No readable content found in files");
        }

        // Group all parts into chunks that fit the token limit per request
        List<org.json.JSONArray> chunks = groupPartsIntoChunks(allParts);
        
        // Process chunks in parallel for maximum productivity
        ExecutorService partExecutor = Executors.newFixedThreadPool(Math.min(chunks.size(), 5));
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            final int chunkIndex = i;
            final org.json.JSONArray requestParts = chunks.get(i);
            final int totalChunks = chunks.size();
            
            futures.add(partExecutor.submit(() -> {
                long partStartTime = System.currentTimeMillis();
                long currentDelay = INITIAL_RETRY_DELAY_MS;
                
                while (true) {
                    try {
                        String partInfo = totalChunks > 1 ? " (Part " + (chunkIndex + 1) + " of " + totalChunks + ")" : "";
                        String prompt = buildPrompt(subjectTitle + partInfo);
                        
                        JSONObject request = new JSONObject();
                        
                        // Add text prompt as first part
                        JSONObject textPromptPart = new JSONObject();
                        textPromptPart.put("text", prompt);
                        
                        org.json.JSONArray finalParts = new org.json.JSONArray();
                        finalParts.put(textPromptPart);
                        for (int j = 0; j < requestParts.length(); j++) {
                            finalParts.put(requestParts.get(j));
                        }
                        
                        request.put("contents", new org.json.JSONArray()
                                .put(new JSONObject().put("parts", finalParts)));
                        
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
                        
                    } catch (JSONException | IOException e) {
                        long elapsed = System.currentTimeMillis() - partStartTime;
                        if (elapsed < MAX_RETRY_DURATION_MS) {
                            Log.w(TAG, String.format("Error processing chunk %d. Retrying in %dms... (%s)", 
                                (chunkIndex + 1), currentDelay, e.getMessage()));
                            try {
                                Thread.sleep(currentDelay);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Retry interrupted", ie);
                            }
                            currentDelay = Math.min(currentDelay * 2, MAX_RETRY_DELAY_MS);
                        } else {
                            Log.e(TAG, "Exceeded retry duration for chunk " + (chunkIndex + 1));
                            throw e;
                        }
                    }
                }
            }));
        }
        
        List<String> jsonResponses = new ArrayList<>();
        try {
            for (Future<String> future : futures) {
                jsonResponses.add(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to process chunks in parallel: " + e.getMessage(), e);
        } finally {
            partExecutor.shutdown();
        }
        
        // Merge results
        String mergedResult = mergeJsonResponses(jsonResponses);
        Log.i(TAG, String.format("Processing complete. Total tokens used so far: %d", totalTokensProcessed.get()));
        return mergedResult;
    }

    private String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        return "text/plain";
    }

    private boolean isBinaryMimeType(String mimeType) {
        return mimeType.startsWith("image/");
    }

    private String fileToBase64(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int readTotal = 0;
            while (readTotal < bytes.length) {
                int read = fis.read(bytes, readTotal, bytes.length - readTotal);
                if (read == -1) break;
                readTotal += read;
            }
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private List<org.json.JSONArray> groupPartsIntoChunks(List<JSONObject> allParts) throws IOException, JSONException {
        List<org.json.JSONArray> chunks = new ArrayList<>();
        org.json.JSONArray currentChunk = new org.json.JSONArray();
        int currentChunkTokens = 0;
        
        // Token overhead for the prompt
        int promptTokens = countTokens(new org.json.JSONArray().put(new JSONObject().put("text", buildPrompt(""))));

        for (JSONObject part : allParts) {
            int partTokens = countTokens(new org.json.JSONArray().put(part));
            
            if (currentChunk.length() > 0 && currentChunkTokens + partTokens + promptTokens > MAX_TOKENS_PER_PART) {
                chunks.add(currentChunk);
                currentChunk = new org.json.JSONArray();
                currentChunkTokens = 0;
            }
            
            currentChunk.put(part);
            currentChunkTokens += partTokens;
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk);
        }

        return chunks;
    }

    private void flushTextCollector(StringBuilder collector, List<String> blocks) {
        if (collector.length() > 0) {
            blocks.add(collector.toString());
            collector.setLength(0);
        }
    }

    private List<PdfChunk> extractPdfChunks(File file, int pagesPerChunk) throws IOException {
        List<PdfChunk> chunks = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            int currentPage = 1;
            while (currentPage <= totalPages) {
                int endPage = Math.min(currentPage + pagesPerChunk - 1, totalPages);
                stripper.setStartPage(currentPage);
                stripper.setEndPage(endPage);
                String text = stripper.getText(document).trim();
                if (!text.isEmpty()) {
                    chunks.add(new PdfChunk(text, currentPage, endPage));
                }
                currentPage = endPage + 1;
            }
        }
        return chunks;
    }

    private static final class PdfChunk {
        final String text;
        final int startPage;
        final int endPage;

        private PdfChunk(String text, int startPage, int endPage) {
            this.text = text;
            this.startPage = startPage;
            this.endPage = endPage;
        }
    }

    private List<String> splitTextIntoSegments(String content) throws IOException, JSONException {
        List<String> segments = new ArrayList<>();
        String[] lines = content.split("\n");
        int startIndex = 0;
        
        while (startIndex < lines.length) {
            int low = startIndex;
            int high = lines.length;
            int bestEnd = startIndex + 1;
            
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
                
                JSONObject part = new JSONObject();
                part.put("text", sb.toString());
                int tokens = countTokens(new org.json.JSONArray().put(part));
                
                if (tokens <= MAX_TOKENS_PER_PART / 2) { // Allow space for prompt and binary parts
                    bestEnd = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            
            StringBuilder buffer = new StringBuilder();
            for (int i = startIndex; i < bestEnd; i++) {
                buffer.append(lines[i]).append("\n");
            }
            segments.add(buffer.toString());
            startIndex = bestEnd;
        }
        return segments;
    }
    
    /**
     * Reads file content as text and sanitizes it by removing non-printable characters
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Remove non-printable characters except common whitespace
                String sanitized = line.replaceAll("[^\\x20-\\x7E\\x0A\\x0D\\x09\\u00A0-\\uD7FF\\\uE000-\\uFFFD]", "");
                content.append(sanitized).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Makes API call to Gemini with retry logic for rate limiting
     */
    private String makeApiCall(String endpoint, JSONObject request) throws IOException, JSONException {
        long startTime = System.currentTimeMillis();
        long currentDelay = INITIAL_RETRY_DELAY_MS;
        
        activeThreads.incrementAndGet();
        try {
            while (true) {
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
                    
                    if (responseCode == 429) { // Rate limited
                        rateLimitedThreads.incrementAndGet();
                        long elapsed = System.currentTimeMillis() - startTime;
                        try {
                            if (elapsed < MAX_RETRY_DURATION_MS) {
                                Log.w(TAG, String.format("Rate limited (429). [%d active, %d rate-limited] Retrying in %dms... (Total wait: %ds)", 
                                    activeThreads.get(), rateLimitedThreads.get(), currentDelay, (elapsed / 1000)));
                                try {
                                    Thread.sleep(currentDelay);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new IOException("Retry interrupted", ie);
                                }
                            } else {
                                throw new IOException("Exceeded retry duration for rate limit");
                            }
                        } finally {
                            rateLimitedThreads.decrementAndGet();
                        }
                        currentDelay = Math.min(currentDelay * 2, MAX_RETRY_DELAY_MS);
                        continue;
                    }
                    
                    if (responseCode >= 400) {
                        throw new IOException("HTTP Error: " + responseCode + " - " + conn.getResponseMessage());
                    }
                    
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        return response.toString();
                    }
                } finally {
                    conn.disconnect();
                }
            }
        } finally {
            activeThreads.decrementAndGet();
        }
    }

    /**
     * Counts tokens for a given set of parts by calling the countTokens endpoint
     */
    private int countTokens(org.json.JSONArray parts) throws IOException, JSONException {
        JSONObject request = new JSONObject();
        request.put("contents", new org.json.JSONArray()
                .put(new JSONObject().put("parts", parts)));
        
        String response = makeApiCall(COUNT_TOKENS_ENDPOINT, request);
        JSONObject jsonResponse = new JSONObject(response);
        int tokens = jsonResponse.getInt("totalTokens");
        totalTokensProcessed.addAndGet(tokens);
        return tokens;
    }

    /**
     * Extracts the generated JSON content from the Gemini API response
     */
    private String extractJsonFromResponse(String response) throws IOException {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String text = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
            
            // Clean up the response, removing markdown backticks
            return text.replaceAll("```json", "").replaceAll("```", "").trim();
        } catch (JSONException e) {
            throw new IOException("Failed to parse JSON from Gemini response: " + response, e);
        }
    }

    /**
     * Merges JSON responses from multiple chunks into a single valid JSON string
     */
    private String mergeJsonResponses(List<String> jsonResponses) throws JSONException {
        if (jsonResponses.isEmpty()) {
            return "{}";
        }
        if (jsonResponses.size() == 1) {
            return jsonResponses.get(0);
        }

        JSONObject mergedRoot = new JSONObject();
        org.json.JSONArray mergedTopics = new org.json.JSONArray();
        mergedRoot.put("topics", mergedTopics);

        for (String jsonStr : jsonResponses) {
            try {
                JSONObject chunkRoot = new JSONObject(jsonStr);
                if (chunkRoot.has("topics")) {
                    org.json.JSONArray chunkTopics = chunkRoot.getJSONArray("topics");
                    for (int i = 0; i < chunkTopics.length(); i++) {
                        mergedTopics.put(chunkTopics.getJSONObject(i));
                    }
                }
            } catch (JSONException e) {
                Log.w(TAG, "Failed to parse a JSON chunk: " + jsonStr);
            }
        }

        return mergedRoot.toString();
    }

    /**
     * Builds the main prompt for content generation
     */
    private String buildPrompt(String subjectTitle) {
        return """
Your task is to act as a structured learning content generator. I will provide you with one or more documents (text, PDFs, images). 
Based *only* on the content of these documents, you will generate a structured JSON object that breaks down the information into topics, challenges, and content containers. 
Do not use any information outside of the provided documents.

Subject Title: {subjectTitle}

Follow this JSON schema strictly:

{
  "topics": [
    {
      "title": "<A concise, descriptive title for a main topic covered in the documents>",
      "challenges": [
        {
          "title": "<A concise title for a specific challenge or lesson within this topic>",
          "description": "<An optional, brief (1-2 sentences) description of the challenge>",
          "containers": [
            {
              "type": "<Type of content: 'PlainText', 'Image', 'MultipleChoice', 'TrueFalse'>",
              // For PlainText
              "content": "<A paragraph of text explaining a concept. Use markdown for formatting if necessary.>",
              // For Image
              "image_description": "<A detailed description of the image content>",
              // For MultipleChoice
              "question": "<The multiple-choice question>",
              "options": ["<Option A>", "<Option B>", "<Option C>", "<Option D>"],
              "correct_answer_index": <Index of the correct answer (0-3)>,
              "explanation": "<An explanation for why the correct answer is right>",
              // For TrueFalse
              "statement": "<The statement to be evaluated>",
              "is_true": <true or false>,
              "explanation": "<An explanation for why the statement is true or false>"
            }
            // ... more containers
          ]
        }
        // ... more challenges
      ]
    }
    // ... more topics
  ]
}

- Analyze the documents and identify the main learning topics.
- For each topic, create 1-5 specific challenges or lessons.
- For each challenge, create a sequence of 'content containers' to teach the concept step-by-step. Use a mix of PlainText, MultipleChoice, and TrueFalse questions.
- If you encounter images, describe them in an 'Image' container. Do not analyze or interpret them beyond what is visible.
- Ensure all generated content is directly and exclusively derived from the provided documents.
- Do not include a container if you cannot find relevant information in the documents.
- The final output must be a single, valid JSON object and nothing else.
""".replace("{subjectTitle}", subjectTitle);
    }
}
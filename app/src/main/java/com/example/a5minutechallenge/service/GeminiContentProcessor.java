/** Service for processing files with Google's Gemini 2.0 API.
 * Handles file uploads and structured content generation using a multi-stage prompt approach.
 * 
 * Architecture:
 * - Stage 0: Semantic Document Analysis - LLM identifies logical sections/chapters with page ranges
 * - Stage 1: Topic Extraction - Identifies topics and maps them to semantic sections
 * - Stage 2: Content Generation - Generates detailed content per topic using filtered context
 */
package com.example.a5minutechallenge.service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.a5minutechallenge.BuildConfig;
import com.example.a5minutechallenge.datawrapper.subject.SubjectFile;

import org.json.JSONArray;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GeminiContentProcessor {

    private static final String TAG = "GeminiContentProcessor";
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_RETRY_DURATION_MS = 30 * 60 * 1000; // 30 minutes
    private static final long INITIAL_RETRY_DELAY_MS = 2000; // 2 seconds
    private static final long MAX_RETRY_DELAY_MS = 30000; // 30 seconds
    private static final int MAX_STRUCTURE_RETRIES = 2; // Total 3 attempts (original + 2 retries)

    private final String apiKey;
    private final AtomicLong totalTokensProcessed = new AtomicLong(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final AtomicInteger rateLimitedThreads = new AtomicInteger(0);

    /**
     * Interface for tracking progress during content generation
     */
    public interface ProgressListener {
        void onProgress(int progress, String message);
    }

    public GeminiContentProcessor() {
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("null")) {
            throw new IllegalStateException("GEMINI_API_KEY not configured in local.properties");
        }
    }

    /**
     * Processes uploaded files and generates structured learning content
     * using a multi-stage approach:
     * 1. Semantic document analysis (get logical sections)
     * 2. Topic extraction with section references
     * 3. Detailed content generation per topic
     */
    public String processFiles(List<SubjectFile> files, String subjectTitle, Context context, ProgressListener listener)
            throws IOException, JSONException {
        try {
            PDFBoxResourceLoader.init(context);
        } catch (Exception e) {
            Log.w(TAG, "PDFBoxResourceLoader.init failed: " + e.getMessage());
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for processing");
        }

        if (listener != null)
            listener.onProgress(5, "Extracting document content...");

        // 1. Extract full document content with page-level granularity
        Log.i(TAG, "Extracting document content...");
        List<DocumentContent> documents = extractDocumentContents(files);
        if (documents.isEmpty()) {
            throw new IOException("No readable content found in files");
        }

        if (listener != null)
            listener.onProgress(15, "Analyzing document structure...");

        // 2. Stage 0: Semantic Document Analysis - Get logical sections from LLM
        Log.i(TAG, "Stage 0: Analyzing document structure...");
        List<SemanticSection> semanticSections = analyzeDocumentStructure(documents, subjectTitle);
        Log.i(TAG, "Found " + semanticSections.size() + " semantic sections.");

        if (listener != null)
            listener.onProgress(30, "Extracting topics...");

        // 3. Stage 1: Extract Topics mapped to semantic sections
        Log.i(TAG, "Stage 1: Extracting topics...");
        List<TopicOutline> topicOutlines = extractTopics(documents, semanticSections, subjectTitle);
        Log.i(TAG, "Found " + topicOutlines.size() + " topics.");

        if (topicOutlines.isEmpty()) {
            throw new IOException("No topics could be extracted from the documents");
        }

        // 4. Stage 2: Generate Content for each Topic (Parallelized)
        Log.i(TAG, "Stage 2: Generating content for topics...");
        // Increased thread pool size to handle more parallel requests (especially with
        // individual challenges)
        ExecutorService topicExecutor = Executors.newFixedThreadPool(15);
        List<Future<JSONObject>> topicFutures = new ArrayList<>();

        int baseProgress = 40;
        int totalProgressRange = 55; // From 40 to 95
        AtomicInteger completedTopics = new AtomicInteger(0);
        int totalTopics = topicOutlines.size();

        for (int i = 0; i < totalTopics; i++) {
            TopicOutline outline = topicOutlines.get(i);
            int topicIndex = i;
            topicFutures.add(topicExecutor.submit(() -> {
                try {
                    int topicBaseProgress = baseProgress + (topicIndex * totalProgressRange / totalTopics);
                    int topicProgressRange = totalProgressRange / totalTopics;

                    return generateTopicContent(outline, documents, listener, topicBaseProgress,
                            topicProgressRange);
                } catch (Exception e) {
                    Log.e(TAG, "Topic generation failed for: " + outline.title, e);
                    return null;
                } finally {
                    completedTopics.incrementAndGet();
                }
            }));
        }

        JSONArray generatedTopics = new JSONArray();
        try {
            for (Future<JSONObject> future : topicFutures) {
                JSONObject topicContent = future.get();
                if (topicContent != null) {
                    generatedTopics.put(topicContent);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error gathering topic results: " + e.getMessage());
        } finally {
            topicExecutor.shutdown();
        }

        if (listener != null)
            listener.onProgress(98, "Finalizing structure...");

        // 5. Wrap in final structure
        JSONObject finalResult = new JSONObject();
        finalResult.put("topics", generatedTopics);

        Log.i(TAG, String.format("Processing complete. Total tokens used: %d", totalTokensProcessed.get()));
        if (listener != null)
            listener.onProgress(100, "Generation complete");
        return finalResult.toString();
    }

    // --- Data Structures ---

    /** Represents a full document with per-page text extraction */
    private static class DocumentContent {
        String fileName;
        List<PageContent> pages; // Individual pages
        boolean isImage;
        JSONObject imageData; // For binary images

        DocumentContent(String fileName) {
            this.fileName = fileName;
            this.pages = new ArrayList<>();
            this.isImage = false;
        }
    }

    /** Represents a single page of text content */
    private static class PageContent {
        int pageNumber;
        String text;

        PageContent(int pageNumber, String text) {
            this.pageNumber = pageNumber;
            this.text = text;
        }
    }

    /** Represents a semantic section identified by the LLM */
    private static class SemanticSection {
        String title;
        String fileName;
        int startPage;
        int endPage;

        SemanticSection(String title, String fileName, int startPage, int endPage) {
            this.title = title;
            this.fileName = fileName;
            this.startPage = startPage;
            this.endPage = endPage;
        }
    }

    /** Represents a topic outline with references to semantic sections */
    private static class TopicOutline {
        String title;
        List<SectionRef> sectionRefs;

        TopicOutline(String title, List<SectionRef> refs) {
            this.title = title;
            this.sectionRefs = refs;
        }
    }

    /** Reference to a semantic section */
    private static class SectionRef {
        String fileName;
        int startPage;
        int endPage;

        SectionRef(String fileName, int startPage, int endPage) {
            this.fileName = fileName;
            this.startPage = startPage;
            this.endPage = endPage;
        }
    }

    /** Outline for a single challenge within a topic */
    private static class ChallengeOutline {
        String title;
        String description;

        ChallengeOutline(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    // --- Stage 0: Semantic Document Analysis ---

    private List<SemanticSection> analyzeDocumentStructure(List<DocumentContent> documents, String subjectTitle)
            throws IOException, JSONException {
        List<JSONObject> promptParts = new ArrayList<>();

        // Build the analysis prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(buildDocumentAnalysisPrompt(subjectTitle));
        promptBuilder.append("\n\nDOCUMENT CONTENT:\n");
        promptParts.add(new JSONObject().put("text", promptBuilder.toString()));

        // Add all document content with clear page markers
        for (DocumentContent doc : documents) {
            if (doc.isImage) {
                promptParts.add(doc.imageData);
            } else {
                StringBuilder docText = new StringBuilder();
                docText.append("\n\n========== FILE: ").append(doc.fileName).append(" ==========\n");
                for (PageContent page : doc.pages) {
                    docText.append("\n--- PAGE ").append(page.pageNumber).append(" ---\n");
                    docText.append(page.text);
                }
                promptParts.add(new JSONObject().put("text", docText.toString()));
            }
        }

        int attempts = 0;
        while (true) {
            try {
                String jsonResponse = callGemini(promptParts);
                return parseSemanticSections(jsonResponse, documents);
            } catch (JSONException | IOException e) {
                attempts++;
                if (attempts > MAX_STRUCTURE_RETRIES) {
                    throw e;
                }
                Log.w(TAG, "Stage 0: Malformed structure (" + e.getMessage() + "), retrying... (Attempt "
                        + (attempts + 1) + "/" + (MAX_STRUCTURE_RETRIES + 1) + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry wait", ie);
                }
            }
        }
    }

    private String buildDocumentAnalysisPrompt(String subjectTitle) {
        return String.format("""
                Analyze the structure of the provided document(s) for the subject: "%s".
                Output MUST be in English, regardless of the document's language.

                Identify LOGICAL SECTIONS such as:
                - Chapters
                - Major sections/subsections
                - Distinct topic areas
                - Introduction/Conclusion sections

                For each section, provide the EXACT page range where it appears.

                Return a JSON array using this compact format:
                [
                  {
                    "s": "Section Title",
                    "f": "filename.pdf",
                    "sp": 1,
                    "ep": 5
                  }
                ]

                Keys:
                - "s": Section title (descriptive name, MUST BE IN ENGLISH)
                - "f": Exact filename from the document headers
                - "sp": Start page number
                - "ep": End page number

                Rules:
                1. Sections should not overlap.
                2. Cover ALL pages of the document.
                3. Use the page numbers shown in "--- PAGE X ---" markers.
                4. Output valid JSON array only, no other text.
                """, subjectTitle);
    }

    private List<SemanticSection> parseSemanticSections(String jsonResponse, List<DocumentContent> documents)
            throws JSONException {
        List<SemanticSection> sections = new ArrayList<>();

        JSONArray array;
        if (jsonResponse.trim().startsWith("[")) {
            array = new JSONArray(jsonResponse);
        } else {
            JSONObject obj = new JSONObject(jsonResponse);
            if (obj.has("sections"))
                array = obj.getJSONArray("sections");
            else if (obj.has("s")) {
                // Single section wrapped in object
                array = new JSONArray();
                array.put(obj);
            } else {
                // Try to find any array
                array = new JSONArray();
                for (String key : new String[] { "data", "items", "results" }) {
                    if (obj.has(key)) {
                        array = obj.getJSONArray(key);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            String title = item.optString("s", item.optString("title", "Section " + (i + 1)));
            String fileName = item.optString("f", item.optString("file", ""));
            int startPage = item.optInt("sp", item.optInt("startPage", 1));
            int endPage = item.optInt("ep", item.optInt("endPage", startPage));

            // If no filename specified, use first document
            if (fileName.isEmpty() && !documents.isEmpty()) {
                fileName = documents.get(0).fileName;
            }

            sections.add(new SemanticSection(title, fileName, startPage, endPage));
        }

        // Fallback: if no sections found, create one section per document
        if (sections.isEmpty()) {
            for (DocumentContent doc : documents) {
                if (!doc.isImage && !doc.pages.isEmpty()) {
                    int lastPage = doc.pages.get(doc.pages.size() - 1).pageNumber;
                    sections.add(new SemanticSection("Full Document: " + doc.fileName, doc.fileName, 1, lastPage));
                }
            }
        }

        return sections;
    }

    // --- Stage 1: Topic Extraction ---

    private List<TopicOutline> extractTopics(List<DocumentContent> documents, List<SemanticSection> sections,
            String subjectTitle) throws IOException, JSONException {
        List<JSONObject> promptParts = new ArrayList<>();

        // Build prompt with section information
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(buildTopicExtractionPrompt(subjectTitle, sections));
        promptParts.add(new JSONObject().put("text", promptBuilder.toString()));

        // Add document content for context
        for (DocumentContent doc : documents) {
            if (doc.isImage) {
                promptParts.add(doc.imageData);
            } else {
                StringBuilder docText = new StringBuilder();
                docText.append("\n\n========== FILE: ").append(doc.fileName).append(" ==========\n");
                for (PageContent page : doc.pages) {
                    docText.append("\n--- PAGE ").append(page.pageNumber).append(" ---\n");
                    docText.append(page.text);
                }
                promptParts.add(new JSONObject().put("text", docText.toString()));
            }
        }

        int attempts = 0;
        while (true) {
            try {
                String jsonResponse = callGemini(promptParts);
                return parseTopicOutlines(jsonResponse);
            } catch (JSONException | IOException e) {
                attempts++;
                if (attempts > MAX_STRUCTURE_RETRIES) {
                    throw e;
                }
                Log.w(TAG, "Stage 1: Malformed structure (" + e.getMessage() + "), retrying... (Attempt "
                        + (attempts + 1) + "/" + (MAX_STRUCTURE_RETRIES + 1) + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry wait", ie);
                }
            }
        }
    }

    private String buildTopicExtractionPrompt(String subjectTitle, List<SemanticSection> sections) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
                Based on the provided documents for subject: "%s", identify the main LEARNING TOPICS.
                Output MUST be in English, regardless of the document's language.

                The document has been analyzed and contains these semantic sections:
                """, subjectTitle));

        for (int i = 0; i < sections.size(); i++) {
            SemanticSection s = sections.get(i);
            sb.append(String.format("\n%d. \"%s\" (File: %s, Pages %d-%d)",
                    i + 1, s.title, s.fileName, s.startPage, s.endPage));
        }

        sb.append("""


                For each TOPIC you identify, map it to the relevant section(s) above.
                A topic may span multiple sections or be part of one section.

                Return a JSON array:
                [
                  {
                    "t": "Topic Title",
                    "refs": [
                      { "f": "filename.pdf", "sp": 1, "ep": 5 }
                    ]
                  }
                ]

                Keys:
                - "t": Topic title (learning-focused, MUST BE IN ENGLISH)
                - "refs": Array of page references
                  - "f": filename
                  - "sp": start page
                  - "ep": end page

                Rules:
                1. Create meaningful learning topics, not just section headings.
                2. Group related sections into cohesive topics.
                3. Each topic should be suitable for 1-3 challenges (lessons).
                4. Output valid JSON array only.
                """);

        return sb.toString();
    }

    private List<TopicOutline> parseTopicOutlines(String jsonResponse) throws JSONException {
        List<TopicOutline> outlines = new ArrayList<>();

        JSONArray array;
        if (jsonResponse.trim().startsWith("[")) {
            array = new JSONArray(jsonResponse);
        } else {
            JSONObject obj = new JSONObject(jsonResponse);
            if (obj.has("topics"))
                array = obj.getJSONArray("topics");
            else if (obj.has("ts"))
                array = obj.getJSONArray("ts");
            else
                array = new JSONArray().put(obj);
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            String title = item.optString("t", item.optString("title", "Topic " + (i + 1)));
            List<SectionRef> refs = new ArrayList<>();

            if (item.has("refs")) {
                JSONArray refsArray = item.getJSONArray("refs");
                for (int j = 0; j < refsArray.length(); j++) {
                    JSONObject refObj = refsArray.getJSONObject(j);
                    String file = refObj.optString("f", refObj.optString("file", ""));
                    int sp = refObj.optInt("sp", refObj.optInt("startPage", 1));
                    int ep = refObj.optInt("ep", refObj.optInt("endPage", sp));
                    refs.add(new SectionRef(file, sp, ep));
                }
            }
            outlines.add(new TopicOutline(title, refs));
        }
        return outlines;
    }

    // --- Stage 2: Content Generation ---

    private JSONObject generateTopicContent(TopicOutline topic, List<DocumentContent> documents,
            ProgressListener listener, int baseProgress, int progressRange)
            throws IOException, JSONException {
        // Stage 2a: Extract Challenge Outlines for the topic
        if (listener != null) {
            listener.onProgress(baseProgress, "Generating topic: " + topic.title + " (Extracting structure...)");
        }

        Log.i(TAG, "Stage 2a: Extracting challenge outlines for topic: " + topic.title);
        List<ChallengeOutline> challengeOutlines = extractChallengeOutlines(topic, documents);
        Log.i(TAG, "Found " + challengeOutlines.size() + " challenges for topic: " + topic.title);

        if (challengeOutlines.isEmpty()) {
            return new JSONObject().put("title", topic.title).put("challenges", new JSONArray());
        }

        // Stage 2b: Generate Content for each Challenge (Parallelized)
        Log.i(TAG, "Stage 2b: Generating individual challenge content for topic: " + topic.title);
        ExecutorService challengeExecutor = Executors.newFixedThreadPool(Math.min(challengeOutlines.size(), 10));
        List<Future<JSONObject>> challengeFutures = new ArrayList<>();

        AtomicInteger completedChallenges = new AtomicInteger(0);
        int totalChallenges = challengeOutlines.size();

        for (ChallengeOutline outline : challengeOutlines) {
            challengeFutures.add(challengeExecutor.submit(() -> {
                JSONObject result = generateChallengeContent(topic, outline, documents);
                int completed = completedChallenges.incrementAndGet();

                if (listener != null) {
                    // Split the topic's progress range among its challenges
                    int progress = baseProgress + (completed * progressRange / totalChallenges);
                    listener.onProgress(progress,
                            String.format("Topic '%s': Generated %d/%d challenges", topic.title, completed,
                                    totalChallenges));
                }
                return result;
            }));
        }

        JSONArray generatedChallenges = new JSONArray();
        try {
            for (Future<JSONObject> future : challengeFutures) {
                try {
                    JSONObject challengeContent = future.get();
                    if (challengeContent != null) {
                        generatedChallenges.put(challengeContent);
                    }
                } catch (ExecutionException e) {
                    Log.e(TAG,
                            "Challenge failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Topic content generation interrupted: " + topic.title);
            Thread.currentThread().interrupt();
        } finally {
            challengeExecutor.shutdown();
        }

        JSONObject expandedTopic = new JSONObject();
        expandedTopic.put("title", topic.title);
        expandedTopic.put("challenges", generatedChallenges);
        return expandedTopic;
    }

    private List<ChallengeOutline> extractChallengeOutlines(TopicOutline topic, List<DocumentContent> documents)
            throws IOException, JSONException {
        List<JSONObject> promptParts = new ArrayList<>();
        promptParts.add(new JSONObject().put("text", buildChallengeOutlinesPrompt(topic.title)));

        // Add relevant content
        addRelevantContentToPrompt(topic, documents, promptParts);

        int attempts = 0;
        while (true) {
            try {
                String jsonResponse = callGemini(promptParts);
                return parseChallengeOutlines(jsonResponse);
            } catch (JSONException | IOException e) {
                attempts++;
                if (attempts > MAX_STRUCTURE_RETRIES) {
                    throw e;
                }
                Log.w(TAG, "Stage 2a: Malformed challenge outlines for topic '" + topic.title + "' (" + e.getMessage()
                        + "), retrying... (Attempt " + (attempts + 1) + "/" + (MAX_STRUCTURE_RETRIES + 1) + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry wait", ie);
                }
            }
        }
    }

    private JSONObject generateChallengeContent(TopicOutline topic, ChallengeOutline outline,
            List<DocumentContent> documents)
            throws IOException, JSONException {
        List<JSONObject> promptParts = new ArrayList<>();
        promptParts.add(new JSONObject().put("text", buildChallengeContainersPrompt(topic.title, outline)));

        // Add relevant content
        addRelevantContentToPrompt(topic, documents, promptParts);

        int attempts = 0;
        while (true) {
            try {
                String jsonResponse = callGemini(promptParts);
                JSONObject toonData = new JSONObject(jsonResponse);

                // Validate against guidelines
                if (!validateToonChallenge(toonData)) {
                    throw new JSONException(
                            "Generated content does not meet guidelines (e.g. empty wordOptions or missing markers)");
                }

                return expandChallenge(toonData);
            } catch (JSONException | IOException e) {
                attempts++;
                if (attempts > MAX_STRUCTURE_RETRIES) {
                    Log.e(TAG, "Stage 2b: FINAL content failure for challenge '" + outline.title + "' after " + attempts
                            + " attempts: " + e.getMessage());
                    return null; // Return null to skip this challenge silently
                }
                Log.w(TAG, "Stage 2b: Content error for challenge '" + outline.title + "' (" + e.getMessage()
                        + "), retrying... (Attempt " + (attempts + 1) + "/" + (MAX_STRUCTURE_RETRIES + 1) + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry wait", ie);
                }
            }
        }
    }

    private void addRelevantContentToPrompt(TopicOutline topic, List<DocumentContent> documents,
            List<JSONObject> promptParts) throws JSONException {
        Set<String> addedContent = new HashSet<>();
        for (SectionRef ref : topic.sectionRefs) {
            for (DocumentContent doc : documents) {
                if (!doc.fileName.equals(ref.fileName) && !ref.fileName.isEmpty())
                    continue;

                if (doc.isImage) {
                    String key = "img:" + doc.fileName;
                    if (!addedContent.contains(key)) {
                        promptParts.add(doc.imageData);
                        addedContent.add(key);
                    }
                } else {
                    StringBuilder relevantText = new StringBuilder();
                    relevantText.append("\n\n=== ").append(doc.fileName).append(" ===\n");

                    for (PageContent page : doc.pages) {
                        if (page.pageNumber >= ref.startPage && page.pageNumber <= ref.endPage) {
                            String key = doc.fileName + ":" + page.pageNumber;
                            if (!addedContent.contains(key)) {
                                relevantText.append("\n--- Page ").append(page.pageNumber).append(" ---\n");
                                relevantText.append(page.text);
                                addedContent.add(key);
                            }
                        }
                    }

                    if (relevantText.length() > 50) { // Only add if there's actual content
                        promptParts.add(new JSONObject().put("text", relevantText.toString()));
                    }
                }
            }
        }

        // If no refs matched, include all content as fallback
        if (addedContent.isEmpty()) {
            for (DocumentContent doc : documents) {
                if (doc.isImage) {
                    promptParts.add(doc.imageData);
                } else {
                    StringBuilder allText = new StringBuilder();
                    allText.append("\n\n=== ").append(doc.fileName).append(" ===\n");
                    for (PageContent page : doc.pages) {
                        allText.append("\n--- Page ").append(page.pageNumber).append(" ---\n");
                        allText.append(page.text);
                    }
                    promptParts.add(new JSONObject().put("text", allText.toString()));
                }
            }
        }
    }

    private String buildChallengeOutlinesPrompt(String topicTitle) {
        return String.format(
                """
                        Based on the provided documents for the Topic: "%s", identify 3-5 individual learning challenges.
                        Output MUST be in English, regardless of the document's language.

                        For each challenge, provide a title and a brief description of what will be covered.

                        Return a JSON array:
                        [
                          {
                            "t": "Challenge Title",
                            "d": "Brief description of the learning objectives for this challenge."
                          }
                        ]

                        Rules:
                        1. Challenges should be cohesive and progress logically.
                        2. Titles and descriptions MUST BE IN ENGLISH.
                        3. Output valid JSON array only.
                        """,
                topicTitle);
    }

    private List<ChallengeOutline> parseChallengeOutlines(String jsonResponse) throws JSONException {
        List<ChallengeOutline> outlines = new ArrayList<>();
        JSONArray array;
        if (jsonResponse.trim().startsWith("[")) {
            array = new JSONArray(jsonResponse);
        } else {
            JSONObject obj = new JSONObject(jsonResponse);
            array = obj.optJSONArray("challenges");
            if (array == null)
                array = new JSONArray();
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            outlines.add(new ChallengeOutline(
                    item.optString("t", "Challenge " + (i + 1)),
                    item.optString("d", "")));
        }
        return outlines;
    }

    private String buildChallengeContainersPrompt(String topicTitle, ChallengeOutline outline) {
        return String.format(
                """
                        Generate detailed learning content for a specific Challenge within the Topic: "%s".
                        Challenge Title: "%s"
                        Challenge Description: "%s"

                        Use ONLY the provided document context.
                        ALL generated text MUST BE IN ENGLISH, even if the source document is in another language.

                        Requirement: Output a JSON object for the challenge using TOON (Token Oriented Object Notation).

                        STRUCTURE DEFINITION & GUIDELINES (FOLLOW STRICTLY):
                        {
                          "t": "Challenge Title (MUST BE IN ENGLISH)",
                          "d": "Challenge Description (MUST BE IN ENGLISH)",
                          "cn": [
                            {
                              "ty": "TITLE",
                              "t": "Title text (Non-empty)"
                            },
                            {
                              "ty": "TEXT",
                              "tx": "Detailed explanatory text (Non-empty)"
                            },
                            {
                              "ty": "MULTIPLE_CHOICE_QUIZ",
                              "q": "Question text?",
                              "os": ["Opt A", "Opt B", "Opt C", "Opt D"],
                              "ci": [0],
                              "am": false,
                              "e": "Detailed explanation why answers are correct"
                            },
                            {
                              "ty": "FILL_IN_THE_GAPS",
                              "tt": "Template text with indexed markers: {1} is a {2}.",
                              "cw": ["word1", "word2"],
                              "wo": ["word1", "word2", "distractor1", "distractor2"]
                            },
                            {
                              "ty": "SORTING_TASK",
                              "co": ["First", "Second", "Third", "Fourth"],
                              "in": "Instructions on what to sort"
                            },
                            {
                              "ty": "ERROR_SPOTTING",
                              "is": ["Correct1", "Correct2", "ERROR_ITEM", "Correct3"],
                              "ei": 2,
                              "in": "Instructions to find the error",
                              "e": "Why this item is the error"
                            },
                            {
                              "ty": "REVERSE_QUIZ",
                              "a": "The Answer",
                              "qo": ["Question1?", "Question2?", "Question3?"],
                              "cqi": 0,
                              "e": "Why this question matches the answer"
                            },
                            {
                              "ty": "WIRE_CONNECTING",
                              "li": ["Left1", "Left2", "Left3"],
                              "ri": ["Right1", "Right2", "Right3"],
                              "cm": {"0": 1, "1": 0, "2": 2},
                              "in": "Instructions for matching"
                            },
                            {
                              "ty": "RECAP",
                              "rt": "Recap Title",
                              "wc": { "ty": "TITLE", "t": "Inner Title" }
                            }
                          ]
                        }

                        STRICT RULES:
                        1. ALL TEXT MUST BE IN ENGLISH.
                        2. FILL_IN_THE_GAPS: `wo` (wordOptions) MUST NOT be empty. It must contain ALL `cw` (correctWords) plus 2-4 distractors. `tt` must use {1}, {2}, etc.
                        3. MULTIPLE_CHOICE_QUIZ: Minimum 2 options. `ci` must contain valid indices into `os`.
                        4. SORTING_TASK: Minimum 3 items in `co`.
                        5. ERROR_SPOTTING: Minimum 3 items in `is`. `ei` must be the index of the incorrect item.
                        6. WIRE_CONNECTING: `li` and `ri` must have the same length (min 3). `cm` must map EVERY left index to its matching right index.
                        7. Diversify container types. Use roughly 10 containers per challenge.
                        8. Output valid JSON only, no other text.
                        """,
                topicTitle, outline.title, outline.description);
    }

    // --- Validation and Regeneration ---

    private boolean validateToonChallenge(JSONObject toonC) {
        if (toonC == null)
            return false;
        try {
            if (toonC.optString("t", "").isEmpty())
                return false;

            JSONArray cn = toonC.optJSONArray("cn");
            if (cn == null || cn.length() == 0)
                return false;

            for (int i = 0; i < cn.length(); i++) {
                JSONObject container = cn.getJSONObject(i);
                String type = container.optString("ty", "");

                switch (type) {
                    case "TITLE":
                        if (container.optString("t", "").isEmpty())
                            return false;
                        break;
                    case "TEXT":
                        if (container.optString("tx", "").isEmpty())
                            return false;
                        break;
                    case "MULTIPLE_CHOICE_QUIZ":
                        if (container.optString("q", "").isEmpty())
                            return false;
                        JSONArray os = container.optJSONArray("os");
                        JSONArray ci = container.optJSONArray("ci");
                        if (os == null || os.length() < 2)
                            return false;
                        if (ci == null || ci.length() == 0)
                            return false;
                        for (int j = 0; j < ci.length(); j++) {
                            int idx = ci.getInt(j);
                            if (idx < 0 || idx >= os.length())
                                return false;
                        }
                        break;
                    case "FILL_IN_THE_GAPS":
                        String tt = container.optString("tt", "");
                        JSONArray cw = container.optJSONArray("cw");
                        JSONArray wo = container.optJSONArray("wo");
                        if (tt.isEmpty())
                            return false;
                        if (cw == null || cw.length() == 0)
                            return false;
                        if (wo == null || wo.length() == 0)
                            return false;
                        // Check if all correct words are in word options
                        Set<String> optionsSet = new HashSet<>();
                        for (int j = 0; j < wo.length(); j++)
                            optionsSet.add(wo.getString(j).toLowerCase());
                        for (int j = 0; j < cw.length(); j++) {
                            if (!optionsSet.contains(cw.getString(j).toLowerCase()))
                                return false;
                        }
                        // Check if markers {1}, {2}, etc. exist in template
                        for (int j = 1; j <= cw.length(); j++) {
                            if (!tt.contains("{" + j + "}"))
                                return false;
                        }
                        break;
                    case "SORTING_TASK":
                        JSONArray co = container.optJSONArray("co");
                        if (co == null || co.length() < 3)
                            return false;
                        break;
                    case "ERROR_SPOTTING":
                        JSONArray is = container.optJSONArray("is");
                        if (is == null || is.length() < 3)
                            return false;
                        int ei = container.optInt("ei", -1);
                        if (ei < 0 || ei >= is.length())
                            return false;
                        break;
                    case "REVERSE_QUIZ":
                        if (container.optString("a", "").isEmpty())
                            return false;
                        JSONArray qo = container.optJSONArray("qo");
                        if (qo == null || qo.length() < 2)
                            return false;
                        int cqi = container.optInt("cqi", -1);
                        if (cqi < 0 || cqi >= qo.length())
                            return false;
                        break;
                    case "WIRE_CONNECTING":
                        JSONArray li = container.optJSONArray("li");
                        JSONArray ri = container.optJSONArray("ri");
                        JSONObject cm = container.optJSONObject("cm");
                        if (li == null || ri == null || cm == null)
                            return false;
                        if (li.length() != ri.length() || li.length() < 3)
                            return false;
                        // Check if all left indices are present in cm and map to valid right indices
                        for (int j = 0; j < li.length(); j++) {
                            if (!cm.has(String.valueOf(j)))
                                return false;
                            int rIdx = cm.getInt(String.valueOf(j));
                            if (rIdx < 0 || rIdx >= ri.length())
                                return false;
                        }
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Validation error: " + e.getMessage());
            return false;
        }
    }

    // --- TOON Expansion ---

    private JSONObject expandChallenge(JSONObject toonC) throws JSONException {
        if (toonC == null) {
            Log.w(TAG, "Null TOON challenge received");
            return null;
        }

        JSONObject expanded = new JSONObject();
        // Use optString for title and description
        expanded.put("title", toonC.optString("t", "Untitled Challenge"));
        if (toonC.has("d")) {
            expanded.put("description", toonC.getString("d"));
        }

        if (toonC.has("cn")) {
            JSONArray containers = new JSONArray();
            JSONArray cnArray = toonC.getJSONArray("cn");
            for (int i = 0; i < cnArray.length(); i++) {
                try {
                    JSONObject containerObj = cnArray.optJSONObject(i);
                    JSONObject expandedContainer = expandContainer(containerObj);
                    if (expandedContainer != null) {
                        containers.put(expandedContainer);
                    } else {
                        Log.w(TAG, "Skipping null container at index " + i + " in challenge: "
                                + expanded.optString("title", "unknown"));
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "Error expanding container " + i + ": " + e.getMessage());
                }
            }
            expanded.put("containers", containers);
        }
        return expanded;
    }

    private JSONObject expandContainer(JSONObject toon) throws JSONException {
        if (toon == null) {
            Log.w(TAG, "Null TOON container received");
            return null;
        }

        String type = toon.optString("ty", "");
        if (type.isEmpty()) {
            Log.w(TAG, "TOON container missing 'ty' (type) field: "
                    + toon.toString().substring(0, Math.min(100, toon.toString().length())));
            return null;
        }

        JSONObject expanded = new JSONObject();
        expanded.put("type", type);

        // Common mappings - instructions are optional
        if (toon.has("in")) {
            expanded.put("instructions", toon.getString("in"));
        }
        if (toon.has("e")) {
            expanded.put("explanationText", toon.optString("e", ""));
        }

        switch (type) {
            case "TITLE":
                expanded.put("title", toon.optString("t", ""));
                break;
            case "TEXT":
                expanded.put("text", toon.optString("tx", ""));
                break;
            case "MULTIPLE_CHOICE_QUIZ":
                expanded.put("question", toon.optString("q", "No question provided"));
                expanded.put("options", toon.optJSONArray("os") != null ? toon.getJSONArray("os") : new JSONArray());
                expanded.put("correctAnswerIndices",
                        toon.optJSONArray("ci") != null ? toon.getJSONArray("ci") : new JSONArray());
                expanded.put("allowMultipleAnswers", toon.optBoolean("am", false));
                break;
            case "FILL_IN_THE_GAPS":
                expanded.put("textTemplate", toon.optString("tt", "No template provided"));
                expanded.put("correctWords",
                        toon.optJSONArray("cw") != null ? toon.getJSONArray("cw") : new JSONArray());
                expanded.put("wordOptions",
                        toon.optJSONArray("wo") != null ? toon.getJSONArray("wo") : new JSONArray());
                break;
            case "SORTING_TASK":
                expanded.put("correctOrder",
                        toon.optJSONArray("co") != null ? toon.getJSONArray("co") : new JSONArray());
                break;
            case "ERROR_SPOTTING":
                expanded.put("items", toon.optJSONArray("is") != null ? toon.getJSONArray("is") : new JSONArray());
                expanded.put("errorIndex", toon.optInt("ei", 0));
                break;
            case "REVERSE_QUIZ":
                expanded.put("answer", toon.optString("a", "No answer provided"));
                expanded.put("questionOptions",
                        toon.optJSONArray("qo") != null ? toon.getJSONArray("qo") : new JSONArray());
                expanded.put("correctQuestionIndex", toon.optInt("cqi", 0));
                break;
            case "WIRE_CONNECTING":
                expanded.put("leftItems", toon.optJSONArray("li") != null ? toon.getJSONArray("li") : new JSONArray());
                expanded.put("rightItems", toon.optJSONArray("ri") != null ? toon.getJSONArray("ri") : new JSONArray());
                expanded.put("correctMatches",
                        toon.optJSONObject("cm") != null ? toon.getJSONObject("cm") : new JSONObject());
                break;
            case "RECAP":
                expanded.put("recapTitle", toon.optString("rt", "Recap"));
                if (toon.has("wc")) {
                    JSONObject wrappedContainer = expandContainer(toon.getJSONObject("wc"));
                    if (wrappedContainer != null) {
                        expanded.put("wrappedContainer", wrappedContainer);
                    }
                }
                break;
            default:
                Log.w(TAG, "Unknown TOON container type: " + type);
                break;
        }
        return expanded;
    }

    // --- Document Extraction ---

    private List<DocumentContent> extractDocumentContents(List<SubjectFile> files) throws IOException {
        List<DocumentContent> documents = new ArrayList<>();

        for (SubjectFile file : files) {
            File f = file.getFile();
            if (!f.exists() || f.length() > MAX_FILE_SIZE)
                continue;

            String mime = getMimeType(f);
            DocumentContent doc = new DocumentContent(file.getFileName());

            if ("application/pdf".equals(mime)) {
                // Extract each page individually for precise filtering
                try (PDDocument pdfDoc = PDDocument.load(f)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    int totalPages = pdfDoc.getNumberOfPages();

                    for (int page = 1; page <= totalPages; page++) {
                        stripper.setStartPage(page);
                        stripper.setEndPage(page);
                        String text = stripper.getText(pdfDoc).trim();
                        if (!text.isEmpty()) {
                            doc.pages.add(new PageContent(page, text));
                        }
                    }
                }
                documents.add(doc);

            } else if (isBinaryMimeType(mime)) {
                // Image file
                try {
                    JSONObject inlineData = new JSONObject()
                            .put("mime_type", mime)
                            .put("data", fileToBase64(f));
                    doc.isImage = true;
                    doc.imageData = new JSONObject().put("inline_data", inlineData);
                    documents.add(doc);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to encode image: " + e.getMessage());
                }

            } else {
                // Plain text file - treat as single page
                String text = readFileContent(f);
                doc.pages.add(new PageContent(1, text));
                documents.add(doc);
            }
        }
        return documents;
    }

    // --- API Communication ---

    private String callGemini(List<JSONObject> parts) throws IOException, JSONException {
        JSONObject request = new JSONObject();
        JSONArray partsArray = new JSONArray();
        for (JSONObject p : parts)
            partsArray.put(p);

        JSONObject content = new JSONObject().put("parts", partsArray);
        request.put("contents", new JSONArray().put(content));

        JSONObject config = new JSONObject();
        config.put("temperature", 0.3); // Lower for more consistent structured output
        config.put("maxOutputTokens", 8192);
        request.put("generationConfig", config);

        return extractJsonFromResponse(makeApiCall(API_ENDPOINT, request));
    }

    private String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf"))
            return "application/pdf";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
            return "image/jpeg";
        if (name.endsWith(".png"))
            return "image/png";
        if (name.endsWith(".webp"))
            return "image/webp";
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
                if (read == -1)
                    break;
                readTotal += read;
            }
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String sanitized = line.replaceAll("[^\\x20-\\x7E\\x0A\\x0D\\x09\\u00A0-\\uD7FF\\\uE000-\\uFFFD]", "");
                content.append(sanitized).append("\n");
            }
        }
        return content.toString();
    }

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
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(120000); // 2 min read timeout for large responses

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();

                    if (responseCode == 429) {
                        rateLimitedThreads.incrementAndGet();
                        long elapsed = System.currentTimeMillis() - startTime;
                        try {
                            if (elapsed < MAX_RETRY_DURATION_MS) {
                                Log.w(TAG, String.format("Rate limited. Retrying in %dms...", currentDelay));
                                try {
                                    Thread.sleep(currentDelay);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new IOException("Interrupted during rate limit wait", e);
                                }
                            } else {
                                throw new IOException("Rate limit timeout after " + (elapsed / 1000) + "s");
                            }
                        } finally {
                            rateLimitedThreads.decrementAndGet();
                        }
                        currentDelay = Math.min(currentDelay * 2, MAX_RETRY_DELAY_MS);
                        continue;
                    }

                    if (responseCode >= 400) {
                        // Read error response
                        StringBuilder errorBody = new StringBuilder();
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = br.readLine()) != null)
                                errorBody.append(line);
                        } catch (Exception ignored) {
                        }
                        throw new IOException("HTTP " + responseCode + ": " + errorBody);
                    }

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null)
                            response.append(line);
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

    private String extractJsonFromResponse(String response) throws IOException {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            // Extract and log token usage
            if (jsonResponse.has("usageMetadata")) {
                JSONObject usage = jsonResponse.getJSONObject("usageMetadata");
                int promptTokens = usage.optInt("promptTokenCount", 0);
                int candidateTokens = usage.optInt("candidatesTokenCount", 0);
                int totalTokens = usage.optInt("totalTokenCount", 0);

                totalTokensProcessed.addAndGet(totalTokens);
                Log.i(TAG, String.format("Token Usage - Prompt: %d, Candidates: %d, Total: %d",
                        promptTokens, candidateTokens, totalTokens));
            }

            String text = jsonResponse.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
            // Clean markdown code blocks if present
            return text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        } catch (JSONException e) {
            throw new IOException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }
}
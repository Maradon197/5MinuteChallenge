package com.example.a5minutechallenge.service;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for ContentContainerFactory
 */
public class ContentContainerFactoryTest {

    @Before
    public void setUp() {
        ContentContainerFactory.resetIdCounter();
    }

    @Test
    public void testCreateTitle() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "TITLE");
        json.put("title", "Introduction to Java");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerTitle);
        assertEquals("Introduction to Java", ((ContainerTitle) container).getTitle());
        assertEquals(ContentContainer.Types.TITLE, container.getType());
    }

    @Test
    public void testCreateText() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "TEXT");
        json.put("text", "Java is a programming language.");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerText);
        assertEquals("Java is a programming language.", ((ContainerText) container).getText());
        assertEquals(ContentContainer.Types.TEXT, container.getType());
    }

    @Test
    public void testCreateMultipleChoiceQuiz() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "MULTIPLE_CHOICE_QUIZ");
        json.put("question", "What is 2+2?");
        json.put("options", new JSONArray().put("3").put("4").put("5"));
        json.put("correctAnswerIndices", new JSONArray().put(1));
        json.put("allowMultipleAnswers", false);
        json.put("explanationText", "Basic arithmetic");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerMultipleChoiceQuiz);
        
        ContainerMultipleChoiceQuiz quiz = (ContainerMultipleChoiceQuiz) container;
        assertEquals("What is 2+2?", quiz.getQuestion());
        assertEquals(3, quiz.getOptions().size());
        assertEquals("4", quiz.getOptions().get(1));
        assertEquals(1, quiz.getCorrectAnswerIndices().size());
        assertEquals(Integer.valueOf(1), quiz.getCorrectAnswerIndices().get(0));
        assertFalse(quiz.isAllowMultipleAnswers());
        assertEquals("Basic arithmetic", quiz.getExplanationText());
    }

    @Test
    public void testCreateFillInTheGaps() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "FILL_IN_THE_GAPS");
        json.put("textTemplate", "Java is a {} language");
        json.put("correctWords", new JSONArray().put("programming"));
        json.put("wordOptions", new JSONArray().put("programming").put("spoken").put("written"));

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerFillInTheGaps);
        
        ContainerFillInTheGaps gaps = (ContainerFillInTheGaps) container;
        assertEquals("Java is a {} language", gaps.getTextTemplate());
        assertEquals(1, gaps.getCorrectWords().size());
        assertEquals("programming", gaps.getCorrectWords().get(0));
        assertEquals(3, gaps.getWordOptions().size());
    }

    @Test
    public void testCreateSortingTask() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "SORTING_TASK");
        json.put("instructions", "Sort in chronological order");
        json.put("correctOrder", new JSONArray().put("First").put("Second").put("Third"));

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerSortingTask);
        
        ContainerSortingTask task = (ContainerSortingTask) container;
        assertEquals("Sort in chronological order", task.getInstructions());
        assertEquals(3, task.getCorrectOrder().size());
        assertEquals("First", task.getCorrectOrder().get(0));
    }

    @Test
    public void testCreateErrorSpotting() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "ERROR_SPOTTING");
        json.put("instructions", "Find the error");
        json.put("items", new JSONArray().put("Correct1").put("Error").put("Correct2"));
        json.put("errorIndex", 1);
        json.put("explanationText", "This is wrong because...");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerErrorSpotting);
        
        ContainerErrorSpotting spotting = (ContainerErrorSpotting) container;
        assertEquals("Find the error", spotting.getInstructions());
        assertEquals(3, spotting.getItems().size());
        assertEquals(1, spotting.getErrorIndex());
        assertEquals("This is wrong because...", spotting.getExplanationText());
    }

    @Test
    public void testCreateReverseQuiz() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "REVERSE_QUIZ");
        json.put("answer", "Paris");
        json.put("questionOptions", new JSONArray()
                .put("What is the capital of France?")
                .put("What is the capital of Italy?"));
        json.put("correctQuestionIndex", 0);
        json.put("explanationText", "Paris is the capital of France");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerReverseQuiz);
        
        ContainerReverseQuiz quiz = (ContainerReverseQuiz) container;
        assertEquals("Paris", quiz.getAnswer());
        assertEquals(2, quiz.getQuestionOptions().size());
        assertEquals(0, quiz.getCorrectQuestionIndex());
        assertEquals("Paris is the capital of France", quiz.getExplanationText());
    }

    @Test
    public void testCreateWireConnecting() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "WIRE_CONNECTING");
        json.put("instructions", "Match terms to definitions");
        json.put("leftItems", new JSONArray().put("Term1").put("Term2"));
        json.put("rightItems", new JSONArray().put("Def1").put("Def2"));
        
        JSONObject matches = new JSONObject();
        matches.put("0", 1);
        matches.put("1", 0);
        json.put("correctMatches", matches);

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerWireConnecting);
        
        ContainerWireConnecting wire = (ContainerWireConnecting) container;
        assertEquals("Match terms to definitions", wire.getInstructions());
        assertEquals(2, wire.getLeftItems().size());
        assertEquals(2, wire.getRightItems().size());
        
        Map<Integer, Integer> correctMatches = wire.getCorrectMatches();
        assertEquals(2, correctMatches.size());
        assertEquals(Integer.valueOf(1), correctMatches.get(0));
        assertEquals(Integer.valueOf(0), correctMatches.get(1));
    }

    @Test
    public void testCreateRecap() throws JSONException {
        JSONObject wrappedJson = new JSONObject();
        wrappedJson.put("type", "TEXT");
        wrappedJson.put("text", "Review content");
        
        JSONObject json = new JSONObject();
        json.put("type", "RECAP");
        json.put("recapTitle", "Chapter Review");
        json.put("wrappedContainer", wrappedJson);

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerRecap);
        
        ContainerRecap recap = (ContainerRecap) container;
        assertEquals("Chapter Review", recap.getRecapTitle());
        assertNotNull(recap.getWrappedContainer());
        assertTrue(recap.getWrappedContainer() instanceof ContainerText);
    }

    @Test
    public void testCreateVideo() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "VIDEO");
        json.put("url", "https://example.com/video.mp4");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerVideo);
        assertEquals("https://example.com/video.mp4", ((ContainerVideo) container).getUrl());
    }

    @Test
    public void testInvalidType() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "INVALID_TYPE");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNull(container);
    }

    @Test
    public void testCaseInsensitiveType() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "text");
        json.put("text", "Lowercase type");

        ContentContainer container = ContentContainerFactory.createFromJson(json);

        assertNotNull(container);
        assertTrue(container instanceof ContainerText);
    }

    @Test
    public void testIdIncrement() throws JSONException {
        ContentContainerFactory.resetIdCounter();
        
        JSONObject json1 = new JSONObject();
        json1.put("type", "TEXT");
        json1.put("text", "First");
        
        JSONObject json2 = new JSONObject();
        json2.put("type", "TEXT");
        json2.put("text", "Second");

        ContentContainer container1 = ContentContainerFactory.createFromJson(json1);
        ContentContainer container2 = ContentContainerFactory.createFromJson(json2);

        assertEquals(0, container1.getId());
        assertEquals(1, container2.getId());
    }
}

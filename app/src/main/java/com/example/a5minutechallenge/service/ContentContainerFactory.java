/** Factory class for creating ContentContainer instances from JSON data.
 * Parses Gemini's JSON response and creates appropriate ContentContainer subclass instances.
 */
package com.example.a5minutechallenge.service;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ContentContainerFactory {

    private static int containerIdCounter = 0;

    /**
     * Creates a ContentContainer from JSON data based on the type field
     * 
     * @param jsonObject JSON object containing container data
     * @return ContentContainer instance or null if type is invalid
     * @throws JSONException if JSON parsing fails
     */
    public static ContentContainer createFromJson(JSONObject jsonObject) throws JSONException {
        String type = jsonObject.getString("type").toUpperCase();
        int id = containerIdCounter++;

        switch (type) {
            case "TITLE":
                return createTitle(id, jsonObject);
            case "TEXT":
                return createText(id, jsonObject);
            case "MULTIPLE_CHOICE_QUIZ":
                return createMultipleChoiceQuiz(id, jsonObject);
            case "FILL_IN_THE_GAPS":
                return createFillInTheGaps(id, jsonObject);
            case "SORTING_TASK":
                return createSortingTask(id, jsonObject);
            case "ERROR_SPOTTING":
                return createErrorSpotting(id, jsonObject);
            case "REVERSE_QUIZ":
                return createReverseQuiz(id, jsonObject);
            case "WIRE_CONNECTING":
                return createWireConnecting(id, jsonObject);
            case "RECAP":
                return createRecap(id, jsonObject);
            case "VIDEO":
                return createVideo(id, jsonObject);
            default:
                return null;
        }
    }

    private static ContainerTitle createTitle(int id, JSONObject json) throws JSONException {
        ContainerTitle container = new ContainerTitle(id);
        container.setTitle(json.getString("title"));
        return container;
    }

    private static ContainerText createText(int id, JSONObject json) throws JSONException {
        ContainerText container = new ContainerText(id);
        container.setText(json.getString("text"));
        return container;
    }

    private static ContainerMultipleChoiceQuiz createMultipleChoiceQuiz(int id, JSONObject json) throws JSONException {
        ContainerMultipleChoiceQuiz container = new ContainerMultipleChoiceQuiz(id);
        container.setQuestion(json.getString("question"));

        // Parse options
        JSONArray optionsArray = json.getJSONArray("options");
        List<String> options = new ArrayList<>();
        for (int i = 0; i < optionsArray.length(); i++) {
            options.add(optionsArray.getString(i));
        }
        container.setOptions(options);

        // Parse correct answer indices
        JSONArray correctArray = json.getJSONArray("correctAnswerIndices");
        List<Integer> correctIndices = new ArrayList<>();
        for (int i = 0; i < correctArray.length(); i++) {
            correctIndices.add(correctArray.getInt(i));
        }
        container.setCorrectAnswerIndices(correctIndices);

        // Set allow multiple answers
        if (json.has("allowMultipleAnswers")) {
            container.setAllowMultipleAnswers(json.getBoolean("allowMultipleAnswers"));
        }

        // Set explanation text
        if (json.has("explanationText")) {
            container.setExplanationText(json.getString("explanationText"));
        }

        return container;
    }

    private static ContainerFillInTheGaps createFillInTheGaps(int id, JSONObject json) throws JSONException {
        ContainerFillInTheGaps container = new ContainerFillInTheGaps(id);
        container.setTextTemplate(json.getString("textTemplate"));

        // Parse correct words
        JSONArray correctWordsArray = json.getJSONArray("correctWords");
        List<String> correctWords = new ArrayList<>();
        for (int i = 0; i < correctWordsArray.length(); i++) {
            correctWords.add(correctWordsArray.getString(i));
        }
        container.setCorrectWords(correctWords);

        // Parse word options
        JSONArray wordOptionsArray = json.getJSONArray("wordOptions");
        List<String> wordOptions = new ArrayList<>();
        for (int i = 0; i < wordOptionsArray.length(); i++) {
            wordOptions.add(wordOptionsArray.getString(i));
        }
        container.setWordOptions(wordOptions);

        return container;
    }

    private static ContainerSortingTask createSortingTask(int id, JSONObject json) throws JSONException {
        ContainerSortingTask container = new ContainerSortingTask(id);
        container.setInstructions(json.getString("instructions"));

        // Parse correct order
        JSONArray correctOrderArray = json.getJSONArray("correctOrder");
        List<String> correctOrder = new ArrayList<>();
        for (int i = 0; i < correctOrderArray.length(); i++) {
            correctOrder.add(correctOrderArray.getString(i));
        }
        container.setCorrectOrder(correctOrder);

        return container;
    }

    private static ContainerErrorSpotting createErrorSpotting(int id, JSONObject json) throws JSONException {
        ContainerErrorSpotting container = new ContainerErrorSpotting(id);
        container.setInstructions(json.getString("instructions"));

        // Parse items
        JSONArray itemsArray = json.getJSONArray("items");
        List<String> items = new ArrayList<>();
        for (int i = 0; i < itemsArray.length(); i++) {
            items.add(itemsArray.getString(i));
        }
        container.setItems(items);

        container.setErrorIndex(json.getInt("errorIndex"));

        if (json.has("explanationText")) {
            container.setExplanationText(json.getString("explanationText"));
        }

        return container;
    }

    private static ContainerReverseQuiz createReverseQuiz(int id, JSONObject json) throws JSONException {
        ContainerReverseQuiz container = new ContainerReverseQuiz(id);
        container.setAnswer(json.getString("answer"));

        // Parse question options
        JSONArray questionOptionsArray = json.getJSONArray("questionOptions");
        List<String> questionOptions = new ArrayList<>();
        for (int i = 0; i < questionOptionsArray.length(); i++) {
            questionOptions.add(questionOptionsArray.getString(i));
        }
        container.setQuestionOptions(questionOptions);

        container.setCorrectQuestionIndex(json.getInt("correctQuestionIndex"));

        if (json.has("explanationText")) {
            container.setExplanationText(json.getString("explanationText"));
        }

        return container;
    }

    private static ContainerWireConnecting createWireConnecting(int id, JSONObject json) throws JSONException {
        ContainerWireConnecting container = new ContainerWireConnecting(id);
        container.setInstructions(json.getString("instructions"));

        // Parse left items
        JSONArray leftItemsArray = json.getJSONArray("leftItems");
        List<String> leftItems = new ArrayList<>();
        for (int i = 0; i < leftItemsArray.length(); i++) {
            leftItems.add(leftItemsArray.getString(i));
        }
        container.setLeftItems(leftItems);

        // Parse right items
        JSONArray rightItemsArray = json.getJSONArray("rightItems");
        List<String> rightItems = new ArrayList<>();
        for (int i = 0; i < rightItemsArray.length(); i++) {
            rightItems.add(rightItemsArray.getString(i));
        }
        container.setRightItems(rightItems);

        // Parse correct matches
        JSONObject correctMatchesObj = json.getJSONObject("correctMatches");
        Map<Integer, Integer> correctMatches = new HashMap<>();
        for (Iterator<String> it = correctMatchesObj.keys(); it.hasNext();) {
            String key = it.next();
            correctMatches.put(Integer.parseInt(key), correctMatchesObj.getInt(key));
        }
        container.setCorrectMatches(correctMatches);

        return container;
    }

    private static ContainerRecap createRecap(int id, JSONObject json) throws JSONException {
        ContainerRecap container = new ContainerRecap(id);

        if (json.has("recapTitle")) {
            container.setRecapTitle(json.getString("recapTitle"));
        }

        if (json.has("wrappedContainer")) {
            ContentContainer wrappedContainer = createFromJson(json.getJSONObject("wrappedContainer"));
            container.setWrappedContainer(wrappedContainer);
        }

        return container;
    }

    private static ContainerVideo createVideo(int id, JSONObject json) throws JSONException {
        ContainerVideo container = new ContainerVideo(id);
        if (json.has("url")) {
            container.setUrl(json.getString("url"));
        }
        return container;
    }

    /**
     * Resets the container ID counter. Useful for testing.
     */
    public static void resetIdCounter() {
        containerIdCounter = 0;
    }

    /**
     * Converts a ContentContainer to JSON for serialization.
     * This is the reverse of createFromJson.
     * 
     * @param container The container to serialize
     * @return JSONObject representation of the container
     * @throws JSONException if JSON creation fails
     */
    public static JSONObject containerToJson(ContentContainer container) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", container.getType().toString());
        json.put("id", container.getId());

        switch (container.getType()) {
            case TITLE:
                ContainerTitle title = (ContainerTitle) container;
                json.put("title", title.getTitle());
                break;

            case TEXT:
                ContainerText text = (ContainerText) container;
                json.put("text", text.getText());
                break;

            case MULTIPLE_CHOICE_QUIZ:
                ContainerMultipleChoiceQuiz mcq = (ContainerMultipleChoiceQuiz) container;
                json.put("question", mcq.getQuestion());
                json.put("options", new JSONArray(mcq.getOptions()));
                json.put("correctAnswerIndices", new JSONArray(mcq.getCorrectAnswerIndices()));
                json.put("allowMultipleAnswers", mcq.isAllowMultipleAnswers());
                if (mcq.getExplanationText() != null) {
                    json.put("explanationText", mcq.getExplanationText());
                }
                break;

            case FILL_IN_THE_GAPS:
                ContainerFillInTheGaps fitg = (ContainerFillInTheGaps) container;
                json.put("textTemplate", fitg.getTextTemplate());
                json.put("correctWords", new JSONArray(fitg.getCorrectWords()));
                json.put("wordOptions", new JSONArray(fitg.getWordOptions()));
                break;

            case SORTING_TASK:
                ContainerSortingTask sorting = (ContainerSortingTask) container;
                json.put("instructions", sorting.getInstructions());
                json.put("correctOrder", new JSONArray(sorting.getCorrectOrder()));
                break;

            case ERROR_SPOTTING:
                ContainerErrorSpotting error = (ContainerErrorSpotting) container;
                json.put("instructions", error.getInstructions());
                json.put("items", new JSONArray(error.getItems()));
                json.put("errorIndex", error.getErrorIndex());
                if (error.getExplanationText() != null) {
                    json.put("explanationText", error.getExplanationText());
                }
                break;

            case REVERSE_QUIZ:
                ContainerReverseQuiz reverse = (ContainerReverseQuiz) container;
                json.put("answer", reverse.getAnswer());
                json.put("questionOptions", new JSONArray(reverse.getQuestionOptions()));
                json.put("correctQuestionIndex", reverse.getCorrectQuestionIndex());
                if (reverse.getExplanationText() != null) {
                    json.put("explanationText", reverse.getExplanationText());
                }
                break;

            case WIRE_CONNECTING:
                ContainerWireConnecting wire = (ContainerWireConnecting) container;
                json.put("instructions", wire.getInstructions());
                json.put("leftItems", new JSONArray(wire.getLeftItems()));
                json.put("rightItems", new JSONArray(wire.getRightItems()));
                JSONObject matchesObj = new JSONObject();
                for (Map.Entry<Integer, Integer> entry : wire.getCorrectMatches().entrySet()) {
                    matchesObj.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                json.put("correctMatches", matchesObj);
                break;

            case RECAP:
                ContainerRecap recap = (ContainerRecap) container;
                if (recap.getRecapTitle() != null) {
                    json.put("recapTitle", recap.getRecapTitle());
                }
                if (recap.getWrappedContainer() != null) {
                    json.put("wrappedContainer", containerToJson(recap.getWrappedContainer()));
                }
                break;

            case VIDEO:
                ContainerVideo video = (ContainerVideo) container;
                if (video.getUrl() != null) {
                    json.put("url", video.getUrl());
                }
                break;
        }

        return json;
    }
}

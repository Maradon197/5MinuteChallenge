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

    private static final String TAG = "ContentContainerFactory";

    /**
     * Creates a ContentContainer from JSON data based on the type field.
     * Uses defensive parsing with graceful handling of missing optional fields.
     * 
     * @param jsonObject JSON object containing container data
     * @return ContentContainer instance or null if type is invalid/missing
     * @throws JSONException if required field parsing fails
     */
    public static ContentContainer createFromJson(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            android.util.Log.w(TAG, "Received null JSON object for container creation");
            return null;
        }

        String type = jsonObject.optString("type", "").toUpperCase();
        if (type.isEmpty()) {
            android.util.Log.w(TAG, "Container missing 'type' field: "
                    + jsonObject.toString().substring(0, Math.min(100, jsonObject.toString().length())));
            return null;
        }

        int id = containerIdCounter++;

        try {
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
                    android.util.Log.w(TAG, "Unknown container type: " + type);
                    return null;
            }
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Error parsing " + type + " container: " + e.getMessage());
            throw new JSONException("Failed to parse " + type + " container: " + e.getMessage());
        }
    }

    private static ContainerTitle createTitle(int id, JSONObject json) throws JSONException {
        ContainerTitle container = new ContainerTitle(id);
        String title = json.optString("title", "");
        if (title.isEmpty()) {
            title = "Untitled";
        }
        container.setTitle(title);
        return container;
    }

    private static ContainerText createText(int id, JSONObject json) throws JSONException {
        ContainerText container = new ContainerText(id);
        String text = json.optString("text", "");
        if (text.isEmpty()) {
            text = "(No content provided)";
        }
        container.setText(text);
        return container;
    }

    private static ContainerMultipleChoiceQuiz createMultipleChoiceQuiz(int id, JSONObject json) throws JSONException {
        ContainerMultipleChoiceQuiz container = new ContainerMultipleChoiceQuiz(id);
        container.setQuestion(json.optString("question", "No question provided"));

        // Parse options
        JSONArray optionsArray = json.optJSONArray("options");
        List<String> options = new ArrayList<>();
        if (optionsArray != null) {
            for (int i = 0; i < optionsArray.length(); i++) {
                options.add(optionsArray.getString(i));
            }
        }
        container.setOptions(options);

        // Parse correct answer indices
        JSONArray correctArray = json.optJSONArray("correctAnswerIndices");
        List<Integer> correctIndices = new ArrayList<>();
        if (correctArray != null) {
            for (int i = 0; i < correctArray.length(); i++) {
                correctIndices.add(correctArray.getInt(i));
            }
        }
        container.setCorrectAnswerIndices(correctIndices);

        // Set allow multiple answers
        container.setAllowMultipleAnswers(json.optBoolean("allowMultipleAnswers", false));

        // Set explanation text
        container.setExplanationText(json.optString("explanationText", ""));

        return container;
    }

    private static ContainerFillInTheGaps createFillInTheGaps(int id, JSONObject json) throws JSONException {
        ContainerFillInTheGaps container = new ContainerFillInTheGaps(id);
        container.setTextTemplate(json.optString("textTemplate", "No template provided"));

        // Parse correct words
        JSONArray correctWordsArray = json.optJSONArray("correctWords");
        List<String> correctWords = new ArrayList<>();
        if (correctWordsArray != null) {
            for (int i = 0; i < correctWordsArray.length(); i++) {
                correctWords.add(correctWordsArray.getString(i));
            }
        }
        container.setCorrectWords(correctWords);

        // Parse word options
        JSONArray wordOptionsArray = json.optJSONArray("wordOptions");
        List<String> wordOptions = new ArrayList<>();
        if (wordOptionsArray != null) {
            for (int i = 0; i < wordOptionsArray.length(); i++) {
                wordOptions.add(wordOptionsArray.getString(i));
            }
        }
        container.setWordOptions(wordOptions);

        return container;
    }

    private static ContainerSortingTask createSortingTask(int id, JSONObject json) throws JSONException {
        ContainerSortingTask container = new ContainerSortingTask(id);
        // Use optString for instructions - provide default if missing
        container.setInstructions(json.optString("instructions", "Arrange the items in the correct order"));

        // Parse correct order
        JSONArray correctOrderArray = json.optJSONArray("correctOrder");
        List<String> correctOrder = new ArrayList<>();
        if (correctOrderArray != null) {
            for (int i = 0; i < correctOrderArray.length(); i++) {
                correctOrder.add(correctOrderArray.getString(i));
            }
        }
        container.setCorrectOrder(correctOrder);

        return container;
    }

    private static ContainerErrorSpotting createErrorSpotting(int id, JSONObject json) throws JSONException {
        ContainerErrorSpotting container = new ContainerErrorSpotting(id);
        // Use optString for instructions - provide default if missing
        container.setInstructions(json.optString("instructions", "Find the error in the items below"));

        // Parse items
        JSONArray itemsArray = json.optJSONArray("items");
        List<String> items = new ArrayList<>();
        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.length(); i++) {
                items.add(itemsArray.getString(i));
            }
        }
        container.setItems(items);

        // Use optInt for errorIndex - default to 0 if missing
        container.setErrorIndex(json.optInt("errorIndex", 0));

        container.setExplanationText(json.optString("explanationText", ""));

        return container;
    }

    private static ContainerReverseQuiz createReverseQuiz(int id, JSONObject json) throws JSONException {
        ContainerReverseQuiz container = new ContainerReverseQuiz(id);
        container.setAnswer(json.optString("answer", "No answer provided"));

        // Parse question options
        JSONArray questionOptionsArray = json.optJSONArray("questionOptions");
        List<String> questionOptions = new ArrayList<>();
        if (questionOptionsArray != null) {
            for (int i = 0; i < questionOptionsArray.length(); i++) {
                questionOptions.add(questionOptionsArray.getString(i));
            }
        }
        container.setQuestionOptions(questionOptions);

        container.setCorrectQuestionIndex(json.optInt("correctQuestionIndex", 0));

        container.setExplanationText(json.optString("explanationText", ""));

        return container;
    }

    private static ContainerWireConnecting createWireConnecting(int id, JSONObject json) throws JSONException {
        ContainerWireConnecting container = new ContainerWireConnecting(id);
        // Use optString for instructions - provide default if missing
        container.setInstructions(
                json.optString("instructions", "Match the items on the left with the items on the right"));

        // Parse left items - required field
        if (!json.has("leftItems")) {
            throw new JSONException("WIRE_CONNECTING requires 'leftItems' array");
        }
        JSONArray leftItemsArray = json.getJSONArray("leftItems");
        List<String> leftItems = new ArrayList<>();
        for (int i = 0; i < leftItemsArray.length(); i++) {
            leftItems.add(leftItemsArray.getString(i));
        }
        container.setLeftItems(leftItems);

        // Parse right items - required field
        if (!json.has("rightItems")) {
            throw new JSONException("WIRE_CONNECTING requires 'rightItems' array");
        }
        JSONArray rightItemsArray = json.getJSONArray("rightItems");
        List<String> rightItems = new ArrayList<>();
        for (int i = 0; i < rightItemsArray.length(); i++) {
            rightItems.add(rightItemsArray.getString(i));
        }
        container.setRightItems(rightItems);

        // Parse correct matches - required field
        if (!json.has("correctMatches")) {
            throw new JSONException("WIRE_CONNECTING requires 'correctMatches' object");
        }
        JSONObject correctMatchesObj = json.getJSONObject("correctMatches");
        Map<Integer, Integer> correctMatches = new HashMap<>();
        for (Iterator<String> it = correctMatchesObj.keys(); it.hasNext();) {
            String key = it.next();
            try {
                correctMatches.put(Integer.parseInt(key), correctMatchesObj.getInt(key));
            } catch (NumberFormatException e) {
                android.util.Log.w(TAG, "Invalid match key: " + key + ", skipping");
            }
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

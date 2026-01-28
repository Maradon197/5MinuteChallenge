/** Content Container for Duolingo-style fill-in-the-gaps exercises.
 * Text with gaps that are filled by clicking word options below.
 **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.ArrayList;
import java.util.List;

public class ContainerFillInTheGaps extends ContentContainer {
    
    private String textTemplate; // Text with {} markers for gaps
    private List<String> correctWords; // Correct words for each gap in order
    private List<String> wordOptions; // All word options to choose from
    private List<String> userFilledWords; // Words user has filled in
    private List<Integer> userClickOrder; // Order of chip clicks (indices from wordOptions)
    private int currentGapIndex;
    private int userSelectedWordIndex = -1; // Stores the index of the word selected by the user from wordOptions
    
    public ContainerFillInTheGaps(int id) {
        super(id, Types.FILL_IN_THE_GAPS);
        this.correctWords = new ArrayList<>();
        this.wordOptions = new ArrayList<>();
        this.userFilledWords = new ArrayList<>();
        this.userClickOrder = new ArrayList<>();
        this.currentGapIndex = 0;
    }
    
    public ContainerFillInTheGaps setTextTemplate(String textTemplate) {
        this.textTemplate = textTemplate;
        return this;
    }
    
    public String getTextTemplate() {
        return textTemplate;
    }
    
    public ContainerFillInTheGaps setCorrectWords(List<String> correctWords) {
        this.correctWords = correctWords;
        return this;
    }
    
    public List<String> getCorrectWords() {
        return correctWords;
    }
    
    public ContainerFillInTheGaps setWordOptions(List<String> wordOptions) {
        this.wordOptions = wordOptions;
        return this;
    }
    
    public List<String> getWordOptions() {
        return wordOptions;
    }
    
    public void fillGap(String word) {
        if (currentGapIndex < correctWords.size()) {
            userFilledWords.add(word);
            currentGapIndex++;
        }
    }

    /**
     * Fills the next gap with the word at the given index and records the click order.
     * @param wordIndex The index of the word in wordOptions
     * @return true if a gap was filled, false if all gaps are already filled
     */
    public boolean fillGapWithIndex(int wordIndex) {
        if (currentGapIndex < correctWords.size() && wordIndex >= 0 && wordIndex < wordOptions.size()) {
            String word = wordOptions.get(wordIndex);
            userFilledWords.add(word);
            userClickOrder.add(wordIndex);
            currentGapIndex++;
            return true;
        }
        return false;
    }
    
    public void removeLastFilledWord() {
        if (!userFilledWords.isEmpty()) {
            userFilledWords.remove(userFilledWords.size() - 1);
            if (!userClickOrder.isEmpty()) {
                userClickOrder.remove(userClickOrder.size() - 1);
            }
            currentGapIndex--;
        }
    }
    
    public void removeFilledWord(int gapIndex) {
        if (gapIndex >= 0 && gapIndex < userFilledWords.size()) {
            String removedWord = userFilledWords.get(gapIndex);
            userFilledWords.set(gapIndex, "");
            wordOptions.add(removedWord);
        }
    }
    
    public List<String> getUserFilledWords() {
        return userFilledWords;
    }

    /**
     * Returns the order in which chips were clicked (indices from wordOptions).
     */
    public List<Integer> getUserClickOrder() {
        return userClickOrder;
    }

    /**
     * Checks if the click order matches the expected order for correct words.
     * The expected order is the indices of correctWords in wordOptions.
     */
    public boolean isClickOrderCorrect() {
        if (userClickOrder.size() != correctWords.size()) {
            return false;
        }
        for (int i = 0; i < correctWords.size(); i++) {
            int clickedIndex = userClickOrder.get(i);
            if (clickedIndex < 0 || clickedIndex >= wordOptions.size()) {
                return false;
            }
            String clickedWord = wordOptions.get(clickedIndex);
            if (!correctWords.get(i).equalsIgnoreCase(clickedWord)) {
                return false;
            }
        }
        return true;
    }
    
    public int getCurrentGapIndex() {
        return currentGapIndex;
    }
    
    public boolean isAllGapsFilled() {
        return currentGapIndex >= correctWords.size();
    }
    
    public boolean isCorrect() {
        if (userFilledWords.size() != correctWords.size()) {
            return false;
        }
        for (int i = 0; i < correctWords.size(); i++) {
            if (!correctWords.get(i).equalsIgnoreCase(userFilledWords.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns display text with placeholders replaced by filled words or placeholder markers.
     * Handles both numbered markers ({0}, {1}, ...) and simple markers ({}).
     * @param placeholder The placeholder string to use for unfilled gaps (e.g., "___")
     */
    public String getDisplayTextWithPlaceholder(String placeholder) {
        String displayText = textTemplate;
        
        // Check if the template uses numbered markers ({0}, {1}, etc.)
        boolean hasZeroBased = textTemplate.contains("{0}");
        boolean hasOneBased = textTemplate.contains("{1}");
        
        if (hasZeroBased || hasOneBased) {
            int offset = hasZeroBased ? 0 : 1;
            // Handle numbered gap markers ({0}, {1}, etc. or {1}, {2}, etc.)
            for (int i = 0; i < correctWords.size(); i++) {
                String marker = "{" + (i + offset) + "}";
                if (i < userFilledWords.size()) {
                    // Gap is filled with a word
                    displayText = displayText.replace(marker, "[" + userFilledWords.get(i) + "]");
                } else {
                    // Gap is not yet filled - show placeholder
                    displayText = displayText.replace(marker, placeholder);
                }
            }
        } else {
            // Handle simple {} markers for backward compatibility
            for (String word : userFilledWords) {
                displayText = displayText.replaceFirst("\\{\\}", "[" + word + "]");
            }
            // Replace remaining simple {} gaps with placeholder
            displayText = displayText.replace("{}", placeholder);
        }
        
        return displayText;
    }
    
    public String getDisplayText() {
        String displayText = textTemplate;

        // Check if the template uses numbered markers ({0}, {1}, etc.)
        boolean hasZeroBased = textTemplate.contains("{0}");
        boolean hasOneBased = textTemplate.contains("{1}");

        if (hasZeroBased || hasOneBased) {
            int offset = hasZeroBased ? 0 : 1;
            for (int i = 0; i < userFilledWords.size(); i++) {
                String marker = "{" + (i + offset) + "}";
                displayText = displayText.replace(marker, userFilledWords.get(i));
            }
        } else {
            for (String word : userFilledWords) {
                displayText = displayText.replaceFirst("\\{\\}", word);
            }
        }
        return displayText;
    }

    public int getUserSelectedWordIndex() {
        return userSelectedWordIndex;
    }

    public void setUserSelectedWordIndex(int userSelectedWordIndex) {
        this.userSelectedWordIndex = userSelectedWordIndex;
    }
}

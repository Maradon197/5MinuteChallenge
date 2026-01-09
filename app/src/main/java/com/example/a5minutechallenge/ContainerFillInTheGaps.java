/** Content Container for Duolingo-style fill-in-the-gaps exercises.
 * Text with gaps that are filled by clicking word options below.
 **/
package com.example.a5minutechallenge;

import java.util.ArrayList;
import java.util.List;

public class ContainerFillInTheGaps extends ContentContainer {
    
    private String textTemplate; // Text with {} markers for gaps
    private List<String> correctWords; // Correct words for each gap in order
    private List<String> wordOptions; // All word options to choose from
    private List<String> userFilledWords; // Words user has filled in
    private int currentGapIndex;
    
    public ContainerFillInTheGaps(int id) {
        super(id, Types.FILL_IN_THE_GAPS);
        this.correctWords = new ArrayList<>();
        this.wordOptions = new ArrayList<>();
        this.userFilledWords = new ArrayList<>();
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
    
    public void removeLastFilledWord() {
        if (!userFilledWords.isEmpty()) {
            userFilledWords.remove(userFilledWords.size() - 1);
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
    
    public String getDisplayText() {
        String displayText = textTemplate;
        for (String word : userFilledWords) {
            displayText = displayText.replaceFirst("\\{\\}", word);
        }
        return displayText;
    }
}

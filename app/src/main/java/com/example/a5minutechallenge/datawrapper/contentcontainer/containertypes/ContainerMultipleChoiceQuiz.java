/** Content Container for multiple choice quiz questions with single or multiple correct answers.
 * Supports visual feedback for correct/incorrect answers and scoring.
 **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.ArrayList;
import java.util.List;

public class ContainerMultipleChoiceQuiz extends ContentContainer {
    
    private String question;
    private List<String> options;
    private List<Integer> correctAnswerIndices;
    private boolean allowMultipleAnswers;
    private String explanationText;
    private List<Integer> userSelectedIndices;
    
    public ContainerMultipleChoiceQuiz(int id) {
        super(id, Types.MULTIPLE_CHOICE_QUIZ);
        this.options = new ArrayList<>();
        this.correctAnswerIndices = new ArrayList<>();
        this.userSelectedIndices = new ArrayList<>();
        this.allowMultipleAnswers = false;
    }
    
    public ContainerMultipleChoiceQuiz setQuestion(String question) {
        this.question = question;
        return this;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public ContainerMultipleChoiceQuiz setOptions(List<String> options) {
        this.options = options;
        return this;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public ContainerMultipleChoiceQuiz setCorrectAnswerIndices(List<Integer> correctAnswerIndices) {
        this.correctAnswerIndices = correctAnswerIndices;
        return this;
    }
    
    public List<Integer> getCorrectAnswerIndices() {
        return correctAnswerIndices;
    }
    
    public ContainerMultipleChoiceQuiz setAllowMultipleAnswers(boolean allowMultipleAnswers) {
        this.allowMultipleAnswers = allowMultipleAnswers;
        return this;
    }
    
    public boolean isAllowMultipleAnswers() {
        return allowMultipleAnswers;
    }
    
    public ContainerMultipleChoiceQuiz setExplanationText(String explanationText) {
        this.explanationText = explanationText;
        return this;
    }
    
    public String getExplanationText() {
        return explanationText;
    }
    
    public void addUserSelectedIndex(int index) {
        if (!allowMultipleAnswers) {
            userSelectedIndices.clear();
        }
        if (!userSelectedIndices.contains(index)) {
            userSelectedIndices.add(index);

        }
    }
    
    public void removeUserSelectedIndex(int index) {
        userSelectedIndices.remove(Integer.valueOf(index));
    }
    
    public List<Integer> getUserSelectedIndices() {
        return userSelectedIndices;
    }
    
    public boolean isCorrect() {
        if (userSelectedIndices.size() != correctAnswerIndices.size()) {
            return false;
        }
        return correctAnswerIndices.containsAll(userSelectedIndices);
    }
}

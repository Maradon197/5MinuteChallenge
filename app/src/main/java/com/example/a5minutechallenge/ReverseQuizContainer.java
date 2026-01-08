/** Content Container for reverse quiz questions where user gets an answer and selects the correct question.
 **/
package com.example.a5minutechallenge;

import java.util.ArrayList;
import java.util.List;

public class ReverseQuizContainer extends ContentContainer {
    
    private String answer;
    private List<String> questionOptions;
    private int correctQuestionIndex;
    private String explanationText;
    private int userSelectedIndex;
    
    public ReverseQuizContainer(int id) {
        super(id, Types.REVERSE_QUIZ);
        this.questionOptions = new ArrayList<>();
        this.userSelectedIndex = -1;
    }
    
    public ReverseQuizContainer setAnswer(String answer) {
        this.answer = answer;
        return this;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public ReverseQuizContainer setQuestionOptions(List<String> questionOptions) {
        this.questionOptions = questionOptions;
        return this;
    }
    
    public List<String> getQuestionOptions() {
        return questionOptions;
    }
    
    public ReverseQuizContainer setCorrectQuestionIndex(int correctQuestionIndex) {
        this.correctQuestionIndex = correctQuestionIndex;
        return this;
    }
    
    public int getCorrectQuestionIndex() {
        return correctQuestionIndex;
    }
    
    public ReverseQuizContainer setExplanationText(String explanationText) {
        this.explanationText = explanationText;
        return this;
    }
    
    public String getExplanationText() {
        return explanationText;
    }
    
    public void setUserSelectedIndex(int index) {
        this.userSelectedIndex = index;
    }
    
    public int getUserSelectedIndex() {
        return userSelectedIndex;
    }
    
    public boolean isCorrect() {
        return userSelectedIndex == correctQuestionIndex;
    }
}

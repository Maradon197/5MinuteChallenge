/** Content Container for error spotting tasks where user must find the outlier from a set of items.
 **/
package com.example.a5minutechallenge;

import java.util.ArrayList;
import java.util.List;

public class ErrorSpottingContainer extends ContentContainer {
    
    private String instructions;
    private List<String> items;
    private int errorIndex; // Index of the incorrect item
    private String explanationText;
    private int userSelectedIndex;
    
    public ErrorSpottingContainer(int id) {
        super(id, Types.ERROR_SPOTTING);
        this.items = new ArrayList<>();
        this.userSelectedIndex = -1;
    }
    
    public ErrorSpottingContainer setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public ErrorSpottingContainer setItems(List<String> items) {
        this.items = items;
        return this;
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public ErrorSpottingContainer setErrorIndex(int errorIndex) {
        this.errorIndex = errorIndex;
        return this;
    }
    
    public int getErrorIndex() {
        return errorIndex;
    }
    
    public ErrorSpottingContainer setExplanationText(String explanationText) {
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
        return userSelectedIndex == errorIndex;
    }
}

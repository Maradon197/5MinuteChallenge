/** Content Container for wire connecting tasks where user matches inputs to corresponding outputs.
 * Useful for vocabulary, algorithm-description matching, etc.
 **/
package com.example.a5minutechallenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WireConnectingContainer extends ContentContainer {
    
    private String instructions;
    private List<String> leftItems;
    private List<String> rightItems;
    private Map<Integer, Integer> correctMatches; // leftIndex -> rightIndex
    private Map<Integer, Integer> userMatches; // leftIndex -> rightIndex
    
    public WireConnectingContainer(int id) {
        super(id, Types.WIRE_CONNECTING);
        this.leftItems = new ArrayList<>();
        this.rightItems = new ArrayList<>();
        this.correctMatches = new HashMap<>();
        this.userMatches = new HashMap<>();
    }
    
    public WireConnectingContainer setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public WireConnectingContainer setLeftItems(List<String> leftItems) {
        this.leftItems = leftItems;
        return this;
    }
    
    public List<String> getLeftItems() {
        return leftItems;
    }
    
    public WireConnectingContainer setRightItems(List<String> rightItems) {
        this.rightItems = rightItems;
        return this;
    }
    
    public List<String> getRightItems() {
        return rightItems;
    }
    
    public WireConnectingContainer setCorrectMatches(Map<Integer, Integer> correctMatches) {
        this.correctMatches = correctMatches;
        return this;
    }
    
    public Map<Integer, Integer> getCorrectMatches() {
        return correctMatches;
    }
    
    public void addUserMatch(int leftIndex, int rightIndex) {
        userMatches.put(leftIndex, rightIndex);
    }
    
    public void removeUserMatch(int leftIndex) {
        userMatches.remove(leftIndex);
    }
    
    public Map<Integer, Integer> getUserMatches() {
        return userMatches;
    }
    
    public boolean isCorrect() {
        if (userMatches.size() != correctMatches.size()) {
            return false;
        }
        for (Map.Entry<Integer, Integer> entry : correctMatches.entrySet()) {
            if (!userMatches.containsKey(entry.getKey()) || 
                !userMatches.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}

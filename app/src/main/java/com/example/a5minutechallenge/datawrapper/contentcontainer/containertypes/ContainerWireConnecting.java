/** Content Container for wire connecting tasks where user matches inputs to corresponding outputs.
 * Useful for vocabulary, algorithm-description matching, etc.
 **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerWireConnecting extends ContentContainer {
    
    private String instructions;
    private List<String> leftItems;
    private List<String> rightItems;
    private Map<Integer, Integer> correctMatches; // leftIndex -> rightIndex
    
    public ContainerWireConnecting(int id) {
        super(id, Types.WIRE_CONNECTING);
        this.leftItems = new ArrayList<>();
        this.rightItems = new ArrayList<>();
        this.correctMatches = new HashMap<>();
    }
    
    public ContainerWireConnecting setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public ContainerWireConnecting setLeftItems(List<String> leftItems) {
        this.leftItems = leftItems;
        return this;
    }
    
    public List<String> getLeftItems() {
        return leftItems;
    }
    
    public ContainerWireConnecting setRightItems(List<String> rightItems) {
        this.rightItems = rightItems;
        return this;
    }
    
    public List<String> getRightItems() {
        return rightItems;
    }
    
    public ContainerWireConnecting setCorrectMatches(Map<Integer, Integer> correctMatches) {
        this.correctMatches = correctMatches;
        return this;
    }
    
    public Map<Integer, Integer> getCorrectMatches() {
        return correctMatches;
    }
    
    /**
     * Checks if the current arrangement is correct.
     * For wire connecting without actual wires, items are matched by position:
     * leftItems[i] should match with rightItems[i] based on the correct matches mapping.
     */
    public boolean isCorrect() {
        // Check if each left item at position i correctly matches the right item at position i
        for (int i = 0; i < Math.min(leftItems.size(), rightItems.size()); i++) {
            // Get what right index should be at position i based on correct matches
            Integer expectedRightIndex = correctMatches.get(i);
            if (expectedRightIndex == null) {
                continue; // No match required for this position
            }
            
            // Bounds check
            if (expectedRightIndex < 0 || expectedRightIndex >= rightItems.size()) {
                return false;
            }
            
            // Find the item that should be at position i based on correct matches
            String expectedRightItem = rightItems.get(expectedRightIndex);
            
            // In the current implementation, rightItems maintains its actual current order
            // So we check if the item at position i matches what we expect
            if (i >= rightItems.size() || !rightItems.get(i).equals(expectedRightItem)) {
                return false;
            }
        }
        return true;
    }
}

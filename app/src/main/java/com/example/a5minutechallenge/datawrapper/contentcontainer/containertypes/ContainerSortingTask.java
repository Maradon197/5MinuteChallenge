/** Content Container for sorting tasks where items must be arranged in the correct order.
 **/
package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;

import java.util.ArrayList;
import java.util.List;

public class ContainerSortingTask extends ContentContainer {
    
    private String instructions;
    private List<String> correctOrder; // Items in correct order
    private List<String> currentOrder; // Current user arrangement
    
    public ContainerSortingTask(int id) {
        super(id, Types.SORTING_TASK);
        this.correctOrder = new ArrayList<>();
        this.currentOrder = new ArrayList<>();
    }
    
    public ContainerSortingTask setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public ContainerSortingTask setCorrectOrder(List<String> correctOrder) {
        this.correctOrder = correctOrder;
        this.currentOrder = new ArrayList<>(correctOrder); // Start with same order
        return this;
    }
    
    public List<String> getCorrectOrder() {
        return correctOrder;
    }
    
    public List<String> getCurrentOrder() {
        return currentOrder;
    }
    
    public void swapItems(int fromPosition, int toPosition) {
        if (fromPosition >= 0 && fromPosition < currentOrder.size() &&
            toPosition >= 0 && toPosition < currentOrder.size()) {
            String temp = currentOrder.get(fromPosition);
            currentOrder.set(fromPosition, currentOrder.get(toPosition));
            currentOrder.set(toPosition, temp);
        }
    }
    
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition >= 0 && fromPosition < currentOrder.size() &&
            toPosition >= 0 && toPosition < currentOrder.size()) {
            String item = currentOrder.remove(fromPosition);
            currentOrder.add(toPosition, item);
        }
    }
    
    public boolean isCorrect() {
        if (currentOrder.size() != correctOrder.size()) {
            return false;
        }
        for (int i = 0; i < correctOrder.size(); i++) {
            if (!correctOrder.get(i).equals(currentOrder.get(i))) {
                return false;
            }
        }
        return true;
    }
}

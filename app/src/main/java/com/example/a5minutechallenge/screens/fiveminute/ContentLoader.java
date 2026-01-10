/** 
 * Content loader that provides lesson content for different subjects and topics.
 * This is where backend data would be integrated.
 */
package com.example.a5minutechallenge.screens.fiveminute;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerErrorSpotting;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerFillInTheGaps;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerMultipleChoiceQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerRecap;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerReverseQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerSortingTask;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerText;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerTitle;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerWireConnecting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentLoader {
    
    /**
     * Loads content containers for a specific subject and topic.
     * This is example population and for reference
     * subject to LOTS of change
     * @param subjectId The ID of the subject
     * @param topicName The name of the topic
     * @return List of ContentContainer objects for the lesson
     */
    public static List<ContentContainer> loadContent(int subjectId, String topicName) {
        List<ContentContainer> containers = new ArrayList<>();
        
        // Route to appropriate content based on subject and topic
        if (subjectId == 0) { // Jetbrains IDE
            return loadJetbrainsIDEContent(topicName);
        }
        
        // Default content for other subjects
        return loadDefaultContent(topicName);
    }
    
    /**
     * Loads content for Jetbrains IDE topics with examples of all container types.
     * @param topicName The name of the topic
     * @return List of ContentContainer objects
     */
    private static List<ContentContainer> loadJetbrainsIDEContent(String topicName) {
        List<ContentContainer> containers = new ArrayList<>();
        int idCounter = 0;
        
        // Title container
        containers.add(new ContainerTitle(idCounter++).setTitle(topicName));
        
        switch (topicName) {
            case "Shortcuts":
                loadShortcutsContent(containers, idCounter);
                break;
            case "Navigation":
                loadNavigationContent(containers, idCounter);
                break;
            case "Code Generation":
                loadCodeGenerationContent(containers, idCounter);
                break;
            case "Refactoring":
                loadRefactoringContent(containers, idCounter);
                break;
            case "Debugging":
                loadDebuggingContent(containers, idCounter);
                break;
            default:
                loadDefaultContent(containers, idCounter);
                break;
        }
        
        return containers;
    }
    
    private static void loadShortcutsContent(List<ContentContainer> containers, int startId) {
        int id = startId;

        //i get a list of contentcontainers, pull content from list via for loop and switch case
        
        // TEXT container
        containers.add(new ContainerText(id++)
            .setText("Keyboard shortcuts are essential for productive coding. Let's learn the most important JetBrains IDE shortcuts!"));
        
        // MULTIPLE_CHOICE_QUIZ container
        ContainerMultipleChoiceQuiz mcq1 = new ContainerMultipleChoiceQuiz(id++);
        mcq1.setQuestion("What is the shortcut for 'Search Everywhere' in IntelliJ IDEA?");
        mcq1.setOptions(Arrays.asList("Ctrl+Shift+F", "Double Shift", "Ctrl+N", "Alt+F7"));
        mcq1.setCorrectAnswerIndices(Arrays.asList(1));
        mcq1.setExplanationText("Double Shift opens the Search Everywhere dialog, your universal search for anything in the IDE.");
        containers.add(mcq1);
        
        // TEXT container
        containers.add(new ContainerText(id++)
            .setText("The 'Find in Files' shortcut helps you search across your entire project."));
        
        // REVERSE_QUIZ container
        ContainerReverseQuiz reverseQuiz = new ContainerReverseQuiz(id++);
        reverseQuiz.setAnswer("Ctrl+Shift+F (Cmd+Shift+F on Mac)");
        reverseQuiz.setQuestionOptions(Arrays.asList(
            "What opens the Search Everywhere dialog?",
            "What is the shortcut for Find in Files?",
            "What opens the Run menu?",
            "What is the shortcut for Navigate to Class?"
        ));
        reverseQuiz.setCorrectQuestionIndex(1);
        reverseQuiz.setExplanationText("Ctrl+Shift+F is the standard shortcut for searching text across all files in your project.");
        containers.add(reverseQuiz);
        
        // FILL_IN_THE_GAPS container
        ContainerFillInTheGaps fillGaps = new ContainerFillInTheGaps(id++);
        fillGaps.setTextTemplate("To quickly comment or uncomment a line, press {}. To duplicate a line, press {}. To delete a line, press {}.");
        fillGaps.setCorrectWords(Arrays.asList("Ctrl+/", "Ctrl+D", "Ctrl+Y"));
        fillGaps.setWordOptions(Arrays.asList("Ctrl+/", "Ctrl+D", "Ctrl+Y", "Ctrl+Z", "Ctrl+X", "Ctrl+C"));
        containers.add(fillGaps);
        
        // WIRE_CONNECTING container
        ContainerWireConnecting wireConnect = new ContainerWireConnecting(id++);
        wireConnect.setInstructions("Match the shortcut with its action:");
        wireConnect.setLeftItems(Arrays.asList("Ctrl+Alt+L", "Alt+Enter", "Ctrl+W", "Shift+F6"));
        wireConnect.setRightItems(Arrays.asList("Reformat Code", "Show Context Actions", "Extend Selection", "Rename"));
        Map<Integer, Integer> matches = new HashMap<>();
        matches.put(0, 0); // Ctrl+Alt+L -> Reformat Code
        matches.put(1, 1); // Alt+Enter -> Show Context Actions
        matches.put(2, 2); // Ctrl+W -> Extend Selection
        matches.put(3, 3); // Shift+F6 -> Rename
        wireConnect.setCorrectMatches(matches);
        containers.add(wireConnect);
        
        // SORTING_TASK container
        ContainerSortingTask sortTask = new ContainerSortingTask(id++);
        sortTask.setInstructions("Arrange these refactoring shortcuts from most to least commonly used:");
        sortTask.setCorrectOrder(Arrays.asList(
            "Shift+F6 (Rename)",
            "Ctrl+Alt+M (Extract Method)",
            "Ctrl+Alt+V (Extract Variable)",
            "Ctrl+Alt+C (Extract Constant)"
        ));
        containers.add(sortTask);
        
        // ERROR_SPOTTING container
        ContainerErrorSpotting errorSpot = new ContainerErrorSpotting(id++);
        errorSpot.setInstructions("Find the incorrect shortcut:");
        errorSpot.setItems(Arrays.asList(
            "Ctrl+N - Navigate to Class",
            "Ctrl+Shift+N - Navigate to File",
            "Ctrl+B - Go to Declaration",
            "Ctrl+G - Go to Implementation"
        ));
        errorSpot.setErrorIndex(3);
        errorSpot.setExplanationText("Ctrl+G goes to Line, not Implementation. Ctrl+Alt+B goes to Implementation.");
        containers.add(errorSpot);
        
        // RECAP container with TEXT inside
        ContainerRecap recap = new ContainerRecap(id++);
        recap.setRecapTitle("Shortcuts Recap");
        ContainerText recapText = new ContainerText(id++);
        recapText.setText("Great job! You learned the essential IntelliJ IDEA shortcuts including Search Everywhere (Double Shift), Find in Files (Ctrl+Shift+F), and common refactoring shortcuts.");
        recap.setWrappedContainer(recapText);
        containers.add(recap);
    }
    
    private static void loadNavigationContent(List<ContentContainer> containers, int startId) {
        int id = startId;
        
        containers.add(new ContainerText(id++)
            .setText("Efficient navigation is key to productivity. Master these navigation techniques!"));
        
        ContainerMultipleChoiceQuiz mcq = new ContainerMultipleChoiceQuiz(id++);
        mcq.setQuestion("Which action takes you to the declaration of a symbol?");
        mcq.setOptions(Arrays.asList("Ctrl+Click", "Alt+F7", "Ctrl+Shift+I", "F2"));
        mcq.setCorrectAnswerIndices(Arrays.asList(0));
        mcq.setExplanationText("Ctrl+Click (or Ctrl+B) navigates to the declaration of the symbol under the cursor.");
        containers.add(mcq);
        
        containers.add(new ContainerText(id++)
            .setText("The 'Recent Files' popup (Ctrl+E) shows your recently opened files for quick navigation."));
        
        ContainerFillInTheGaps fillGaps = new ContainerFillInTheGaps(id++);
        fillGaps.setTextTemplate("To navigate forward and backward through your code history, use {} and {}. To jump to the last edit location, press {}.");
        fillGaps.setCorrectWords(Arrays.asList("Ctrl+Alt+Left", "Ctrl+Alt+Right", "Ctrl+Shift+Backspace"));
        fillGaps.setWordOptions(Arrays.asList("Ctrl+Alt+Left", "Ctrl+Alt+Right", "Ctrl+Shift+Backspace", "Alt+Left", "Alt+Right", "Backspace"));
        containers.add(fillGaps);
    }
    
    private static void loadCodeGenerationContent(List<ContentContainer> containers, int startId) {
        int id = startId;
        
        containers.add(new ContainerText(id++)
            .setText("Code generation features help you write code faster with less typing!"));
        
        ContainerMultipleChoiceQuiz mcq = new ContainerMultipleChoiceQuiz(id++);
        mcq.setQuestion("What does Alt+Insert do in a Java class?");
        mcq.setOptions(Arrays.asList(
            "Opens Generate menu (constructor, getters, setters, etc.)",
            "Adds a new line",
            "Inserts a code template",
            "Creates a new class"
        ));
        mcq.setCorrectAnswerIndices(Arrays.asList(0));
        mcq.setExplanationText("Alt+Insert opens the Generate menu where you can generate constructors, getters, setters, equals/hashCode, and more.");
        containers.add(mcq);
        
        containers.add(new ContainerText(id++)
            .setText("Live templates let you insert common code patterns. Type 'psvm' and press Tab to generate a main method!"));
        
        ContainerWireConnecting wireConnect = new ContainerWireConnecting(id++);
        wireConnect.setInstructions("Match the live template with what it generates:");
        wireConnect.setLeftItems(Arrays.asList("sout", "fori", "ifn", "psvm"));
        wireConnect.setRightItems(Arrays.asList("System.out.println()", "for loop with index", "if null check", "public static void main"));
        Map<Integer, Integer> matches = new HashMap<>();
        matches.put(0, 0);
        matches.put(1, 1);
        matches.put(2, 2);
        matches.put(3, 3);
        wireConnect.setCorrectMatches(matches);
        containers.add(wireConnect);
    }
    
    private static void loadRefactoringContent(List<ContentContainer> containers, int startId) {
        int id = startId;
        
        containers.add(new ContainerText(id++)
            .setText("Refactoring helps you improve code structure safely. JetBrains IDEs offer powerful refactoring tools!"));
        
        ContainerMultipleChoiceQuiz mcq = new ContainerMultipleChoiceQuiz(id++);
        mcq.setQuestion("What's the safest way to rename a variable or method?");
        mcq.setOptions(Arrays.asList(
            "Find and Replace",
            "Manual editing",
            "Shift+F6 (Rename refactoring)",
            "Ctrl+H"
        ));
        mcq.setCorrectAnswerIndices(Arrays.asList(2));
        mcq.setExplanationText("Shift+F6 triggers the Rename refactoring which safely renames all occurrences and updates references.");
        containers.add(mcq);
        
        ContainerSortingTask sortTask = new ContainerSortingTask(id++);
        sortTask.setInstructions("Order these refactorings by complexity (simplest to most complex):");
        sortTask.setCorrectOrder(Arrays.asList(
            "Rename (Shift+F6)",
            "Extract Variable (Ctrl+Alt+V)",
            "Extract Method (Ctrl+Alt+M)",
            "Change Signature (Ctrl+F6)"
        ));
        containers.add(sortTask);
        
        containers.add(new ContainerText(id++)
            .setText("Extract Method (Ctrl+Alt+M) helps you break down large methods into smaller, more maintainable pieces."));
    }
    
    private static void loadDebuggingContent(List<ContentContainer> containers, int startId) {
        int id = startId;
        
        containers.add(new ContainerText(id++)
            .setText("Debugging is essential for finding and fixing bugs. Let's master the debugger!"));
        
        ContainerMultipleChoiceQuiz mcq = new ContainerMultipleChoiceQuiz(id++);
        mcq.setQuestion("How do you set a breakpoint in IntelliJ IDEA?");
        mcq.setOptions(Arrays.asList(
            "Right-click on line number",
            "Click on the gutter (left margin)",
            "Press Ctrl+F8",
            "Both B and C"
        ));
        mcq.setCorrectAnswerIndices(Arrays.asList(3));
        mcq.setExplanationText("You can set a breakpoint by clicking the gutter or pressing Ctrl+F8. Both methods work!");
        containers.add(mcq);
        
        ContainerWireConnecting wireConnect = new ContainerWireConnecting(id++);
        wireConnect.setInstructions("Match the debug action with its shortcut:");
        wireConnect.setLeftItems(Arrays.asList("F8", "F7", "F9", "Alt+F9"));
        wireConnect.setRightItems(Arrays.asList("Step Over", "Step Into", "Resume Program", "Run to Cursor"));
        Map<Integer, Integer> matches = new HashMap<>();
        matches.put(0, 0);
        matches.put(1, 1);
        matches.put(2, 2);
        matches.put(3, 3);
        wireConnect.setCorrectMatches(matches);
        containers.add(wireConnect);
        
        ContainerErrorSpotting errorSpot = new ContainerErrorSpotting(id++);
        errorSpot.setInstructions("Find the incorrect debug shortcut:");
        errorSpot.setItems(Arrays.asList(
            "F8 - Step Over",
            "F7 - Step Into",
            "F5 - Resume Program",
            "Shift+F8 - Step Out"
        ));
        errorSpot.setErrorIndex(2);
        errorSpot.setExplanationText("F5 is not Resume Program. F9 resumes the program. F5 is used for Copy in many applications.");
        containers.add(errorSpot);
    }
    
    /**
     * Loads default content for topics without specific content defined.
     * @param topicName The name of the topic
     * @return List of ContentContainer objects
     */
    private static List<ContentContainer> loadDefaultContent(String topicName) {
        List<ContentContainer> containers = new ArrayList<>();
        containers.add(new ContainerTitle(0).setTitle(topicName));
        loadDefaultContent(containers, 1);
        return containers;
    }
    
    private static void loadDefaultContent(List<ContentContainer> containers, int startId) {
        int id = startId;
        
        containers.add(new ContainerText(id++)
            .setText("Welcome to this 5-minute challenge! Let's learn something new."));
        
        containers.add(new ContainerText(id++)
            .setText("You'll be quizzed on the content. Answer correctly to earn points!"));
        
        ContainerMultipleChoiceQuiz quiz = new ContainerMultipleChoiceQuiz(id++);
        quiz.setQuestion("What is the purpose of this challenge?");
        quiz.setOptions(Arrays.asList(
            "To learn in 5 minutes",
            "To waste time",
            "To play games"
        ));
        quiz.setCorrectAnswerIndices(Arrays.asList(0));
        containers.add(quiz);
        
        containers.add(new ContainerText(id++)
            .setText("Great job! Keep going to complete the challenge."));
    }
}

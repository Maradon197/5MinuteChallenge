/** 
 * Content loader that provides lesson content for different subjects and topics.
 * This is where backend data would be integrated.
 */
package com.example.a5minutechallenge.screens.fiveminute;

import static com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer.Types.MULTIPLE_CHOICE_QUIZ;

import com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerMultipleChoiceQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerText;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerTitle;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerWireConnecting;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerErrorSpotting;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerRecap;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerFillInTheGaps;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerReverseQuiz;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerSortingTask;
import com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes.ContainerVideo;
import com.google.genai.types.Content;


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
        
        loadTestContent(containers, idCounter);
        return containers;
    }
    
    private static void loadTestContent(List<ContentContainer> containers, int startId){
        int id = startId;

        for (ContentContainer container : containers) {
            switch (container.getType()) {
                case TEXT:
                    (ContainerText) containers.add(new ContainerText(id++)//this is supposed to be a typecast to make ".getText" work. But the typecast is faulty somehow
                            .setText(container.getText()));
                    break;
                case ERROR_SPOTTING:
                    (ContainerErrorSpotting) containers.add(new ContainerErrorSpotting(id++)
                            .setText(container.getText()));
                    break;
                case FILL_IN_THE_GAPS:
                    (ContainerFillInTheGaps) containers.add(new ContainerFillInTheGaps(id++)
                            .setText(container.getText()));
                case MULTIPLE_CHOICE_QUIZ:
                    (ContainerMultipleChoiceQuiz) containers.add(new ContainerMultipleChoiceQuiz(id++)
                            .setQuestion(container.getQuestion())
                            .setOptions(container.getOptions())
                            .setCorrectAnswerIndices(container.getCorrectAnswerIndices())
                            .setExplanationText(container.getExplanationText()));
                    break;
                case RECAP:
                    (ContainerRecap) containers.add(new ContainerRecap(id++)
                            .setText(container.getText())
                            .getWrappedContainer.loadTestContent(List<ContentContainer> list = list.of(container), 0));
                    break;
                case REVERSE_QUIZ:
                    //GO THROUGH ALL TYPES ALPHABETICALLY
                default:
                    break;
            }
        }
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

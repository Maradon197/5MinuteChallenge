/** 
 * Content loader that provides lesson content for different subjects and topics.
 * This is where backend data would be integrated.
 */
package com.example.a5minutechallenge.screens.fiveminute;

import static com.example.a5minutechallenge.datawrapper.contentcontainer.ContentContainer.Types.MULTIPLE_CHOICE_QUIZ;

import com.example.a5minutechallenge.datawrapper.challenge.Challenge;
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
import com.example.a5minutechallenge.datawrapper.subject.Subject;
import com.example.a5minutechallenge.datawrapper.topic.Topic;
import com.google.genai.types.Content;


import org.checkerframework.checker.units.qual.A;

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
    public static List<ContentContainer> loadContent(int subjectId, String topicName, int challengePosition) {

        Subject subject = new Subject(subjectId);
        ArrayList<Topic> topics = subject.getTopics(/*MISSING CONTEXT*/);

        int topicId = 0;
        for (int i =0; i < subject.getTopics().size(); i++) {
            if (topics.get(i).getTitle().equals(topicName)) {
                topicId = i;
                break;
            }
        }

        ArrayList<Challenge> challenges = topics.get(topicId).getChallenges();

        List<ContentContainer> containers = challenges.get(challengePosition).getContainerlist();

        return loadTestContent(containers, 0);
    }
    
    /**
     * Loads test content by extracting data from source containers and adding new containers to the list.
     * Processes containers with sequential IDs starting from startId.
     * @param containers List to add the processed containers to
     * @param startId Starting ID for new containers
     */
    private static List<ContentContainer> loadTestContent(List<ContentContainer> containers, int startId){
        int id = startId;
        //copy to iterate over
        List<ContentContainer> sourceContainers = new ArrayList<>(containers);
        
        for (ContentContainer container : sourceContainers) {
            switch (container.getType()) {
                case ERROR_SPOTTING:
                    ContainerErrorSpotting errorSpotting = (ContainerErrorSpotting) container;
                    containers.add(new ContainerErrorSpotting(id++)
                            .setInstructions(errorSpotting.getInstructions())
                            .setItems(errorSpotting.getItems())
                            .setErrorIndex(errorSpotting.getErrorIndex())
                            .setExplanationText(errorSpotting.getExplanationText()));
                    break;
                    
                case FILL_IN_THE_GAPS:
                    ContainerFillInTheGaps fillInGaps = (ContainerFillInTheGaps) container;
                    containers.add(new ContainerFillInTheGaps(id++)
                            .setTextTemplate(fillInGaps.getTextTemplate())
                            .setCorrectWords(fillInGaps.getCorrectWords())
                            .setWordOptions(fillInGaps.getWordOptions()));
                    break;
                    
                case MULTIPLE_CHOICE_QUIZ:
                    ContainerMultipleChoiceQuiz mcQuiz = (ContainerMultipleChoiceQuiz) container;
                    containers.add(new ContainerMultipleChoiceQuiz(id++)
                            .setQuestion(mcQuiz.getQuestion())
                            .setOptions(mcQuiz.getOptions())
                            .setCorrectAnswerIndices(mcQuiz.getCorrectAnswerIndices())
                            .setAllowMultipleAnswers(mcQuiz.isAllowMultipleAnswers())
                            .setExplanationText(mcQuiz.getExplanationText()));
                    break;
                    
                case RECAP:
                    ContainerRecap recap = (ContainerRecap) container;
                    ContainerRecap newRecap = new ContainerRecap(id++);
                    newRecap.setRecapTitle(recap.getRecapTitle());
                    if (recap.getWrappedContainer() != null) {
                        newRecap.setWrappedContainer(recap.getWrappedContainer());
                    }
                    containers.add(newRecap);
                    break;
                    
                case REVERSE_QUIZ:
                    ContainerReverseQuiz reverseQuiz = (ContainerReverseQuiz) container;
                    containers.add(new ContainerReverseQuiz(id++)
                            .setAnswer(reverseQuiz.getAnswer())
                            .setQuestionOptions(reverseQuiz.getQuestionOptions())
                            .setCorrectQuestionIndex(reverseQuiz.getCorrectQuestionIndex())
                            .setExplanationText(reverseQuiz.getExplanationText()));
                    break;
                    
                case SORTING_TASK:
                    ContainerSortingTask sortingTask = (ContainerSortingTask) container;
                    containers.add(new ContainerSortingTask(id++)
                            .setInstructions(sortingTask.getInstructions())
                            .setCorrectOrder(sortingTask.getCorrectOrder()));
                    break;
                    
                case TEXT:
                    ContainerText text = (ContainerText) container;
                    containers.add(new ContainerText(id++)
                            .setText(text.getText()));
                    break;
                    
                case TITLE:
                    ContainerTitle title = (ContainerTitle) container;
                    containers.add(new ContainerTitle(id++)
                            .setTitle(title.getTitle()));
                    break;
                    
                case VIDEO:
                    ContainerVideo video = (ContainerVideo) container;
                    containers.add(new ContainerVideo(id++)
                            .setUrl(video.getUrl()));
                    break;
                    
                case WIRE_CONNECTING:
                    ContainerWireConnecting wireConnecting = (ContainerWireConnecting) container;
                    containers.add(new ContainerWireConnecting(id++)
                            .setInstructions(wireConnecting.getInstructions())
                            .setLeftItems(wireConnecting.getLeftItems())
                            .setRightItems(wireConnecting.getRightItems())
                            .setCorrectMatches(wireConnecting.getCorrectMatches()));
                    break;
                    
                default:
                    // Unknown container type - skip it
                    break;
            }
        }
        return containers;
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

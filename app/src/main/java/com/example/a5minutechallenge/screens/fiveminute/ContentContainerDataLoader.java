/** 
 * Content loader that provides lesson content for different subjects and topics.
 * This is where backend data would be integrated.
 */
package com.example.a5minutechallenge.screens.fiveminute;

import android.content.Context;

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


import java.util.ArrayList;
import java.util.List;

public class ContentContainerDataLoader {

    /**
     * Loads content containers for a specific subject and topic.
     * This loads actual data from generated datawrapper files stored for the subject.
     *
     * @param context           The application context for loading data from storage
     * @param subjectId         The ID of the subject
     * @param topicName         The name of the topic
     * @param challengePosition The position of the challenge in the topic
     * @return List of ContentContainer objects for the lesson
     */
    public static List<ContentContainer> loadContent(Context context, int subjectId, String topicName, int challengePosition, int start_container) {

        Subject subject = new Subject(subjectId);
        ArrayList<Topic> topics = subject.getTopics(context);

        //match topic name because i was too dumb to implement an id in topic.java
        int topicId = 0;
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).getTitle().equals(topicName)) {
                topicId = i;
                break;
            }
        }
        ArrayList<Challenge> challenges = topics.get(topicId).getChallenges();
        List<ContentContainer> containers = challenges.get(challengePosition).getContainerlist();


        //copy to return
        List<ContentContainer> populatedContainers = new ArrayList<>();

        for (ContentContainer container : containers) {
            switch (container.getType()) {
                case ERROR_SPOTTING:
                    ContainerErrorSpotting errorSpotting = (ContainerErrorSpotting) container;
                    populatedContainers.add(new ContainerErrorSpotting(start_container++)
                            .setInstructions(errorSpotting.getInstructions())
                            .setItems(errorSpotting.getItems())
                            .setErrorIndex(errorSpotting.getErrorIndex())
                            .setExplanationText(errorSpotting.getExplanationText()));
                    break;

                case FILL_IN_THE_GAPS:
                    ContainerFillInTheGaps fillInGaps = (ContainerFillInTheGaps) container;
                    populatedContainers.add(new ContainerFillInTheGaps(start_container++)
                            .setTextTemplate(fillInGaps.getTextTemplate())
                            .setCorrectWords(fillInGaps.getCorrectWords())
                            .setWordOptions(fillInGaps.getWordOptions()));
                    break;

                case MULTIPLE_CHOICE_QUIZ:
                    ContainerMultipleChoiceQuiz mcQuiz = (ContainerMultipleChoiceQuiz) container;
                    populatedContainers.add(new ContainerMultipleChoiceQuiz(start_container++)
                            .setQuestion(mcQuiz.getQuestion())
                            .setOptions(mcQuiz.getOptions())
                            .setCorrectAnswerIndices(mcQuiz.getCorrectAnswerIndices())
                            .setAllowMultipleAnswers(mcQuiz.isAllowMultipleAnswers())
                            .setExplanationText(mcQuiz.getExplanationText()));
                    break;

                case RECAP:
                    ContainerRecap recap = (ContainerRecap) container;
                    ContainerRecap newRecap = new ContainerRecap(start_container++);
                    newRecap.setRecapTitle(recap.getRecapTitle());
                    if (recap.getWrappedContainer() != null) {
                        newRecap.setWrappedContainer(recap.getWrappedContainer());
                    }
                    populatedContainers.add(newRecap);
                    break;

                case REVERSE_QUIZ:
                    ContainerReverseQuiz reverseQuiz = (ContainerReverseQuiz) container;
                    populatedContainers.add(new ContainerReverseQuiz(start_container++)
                            .setAnswer(reverseQuiz.getAnswer())
                            .setQuestionOptions(reverseQuiz.getQuestionOptions())
                            .setCorrectQuestionIndex(reverseQuiz.getCorrectQuestionIndex())
                            .setExplanationText(reverseQuiz.getExplanationText()));
                    break;

                case SORTING_TASK:
                    ContainerSortingTask sortingTask = (ContainerSortingTask) container;
                    populatedContainers.add(new ContainerSortingTask(start_container++)
                            .setInstructions(sortingTask.getInstructions())
                            .setCorrectOrder(sortingTask.getCorrectOrder()));
                    break;

                case TEXT:
                    ContainerText text = (ContainerText) container;
                    populatedContainers.add(new ContainerText(start_container++)
                            .setText(text.getText()));
                    break;

                case TITLE:
                    ContainerTitle title = (ContainerTitle) container;
                    populatedContainers.add(new ContainerTitle(start_container++)
                            .setTitle(title.getTitle()));
                    break;

                case VIDEO:
                    ContainerVideo video = (ContainerVideo) container;
                    populatedContainers.add(new ContainerVideo(start_container++)
                            .setUrl(video.getUrl()));
                    break;

                case WIRE_CONNECTING:
                    ContainerWireConnecting wireConnecting = (ContainerWireConnecting) container;
                    populatedContainers.add(new ContainerWireConnecting(start_container++)
                            .setInstructions(wireConnecting.getInstructions())
                            .setLeftItems(wireConnecting.getLeftItems())
                            .setRightItems(wireConnecting.getRightItems())
                            .setCorrectMatches(wireConnecting.getCorrectMatches()));
                    break;

                default:
                    // Unknown container type, skip
                    break;
            }
        }
        return populatedContainers;
    }
}

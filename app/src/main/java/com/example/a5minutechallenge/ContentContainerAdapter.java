/** ArrayAdapter to display containers in the 5-minute-challenge screen **/
package com.example.a5minutechallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ContentContainerAdapter extends ArrayAdapter<ContentContainer> {

    /**
     * Constructs a ContentContainerAdapter with context and content container data.
     * @param context The application context
     * @param contentContainers The list of content containers to display
     */
    public ContentContainerAdapter(@NonNull Context context, List<ContentContainer> contentContainers) {
        super(context, 0, contentContainers);
    }

    /**
     * Returns the number of different view types supported by this adapter.
     * @return The number of view types (equal to number of ContentContainer.Types)
     */
    @Override
    public int getViewTypeCount() {
        return ContentContainer.Types.values().length;
    }

    /**
     * Returns the view type for the item at the specified position.
     * @param position The position of the item
     * @return The ordinal of the ContentContainer.Types enum for this position
     */
    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    /**
     * Creates and returns the appropriate view for the content container at the specified position.
     * Inflates the correct layout based on container type and binds data to the view.
     * @param position The position of the item in the list
     * @param convertView The recycled view to reuse, if available
     * @param parent The parent ViewGroup
     * @return The View for the content container
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ContentContainer contentContainer = getItem(position);

        if (convertView == null) {
            switch (contentContainer.getType()) {
                case TITLE:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.title_container, parent, false);
                    break;
                case TEXT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.text_container, parent, false);
                    break;
                case VIDEO:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.video_container, parent, false);
                    break;
                case QUIZ:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_container, parent, false);
                    break;
                case MULTIPLE_CHOICE_QUIZ:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.multiple_choice_quiz_container, parent, false);
                    break;
                case REVERSE_QUIZ:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.reverse_quiz_container, parent, false);
                    break;
                case WIRE_CONNECTING:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.wire_connecting_container, parent, false);
                    break;
                case FILL_IN_THE_GAPS:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.fill_in_gaps_container, parent, false);
                    break;
                case SORTING_TASK:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.sorting_task_container, parent, false);
                    break;
                case ERROR_SPOTTING:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.error_spotting_container, parent, false);
                    break;
                case RECAP:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.recap_container, parent, false);
                    break;
            }
        }

        switch (contentContainer.getType()) {
            case TITLE:
                TextView titleView = convertView.findViewById(R.id.title_text);
                ContainerTitle titleContainer = (ContainerTitle) contentContainer;
                titleView.setText(titleContainer.getTitle());
                break;
            case TEXT:
                TextView textView = convertView.findViewById(R.id.text_content);
                ContainerText textContainer = (ContainerText) contentContainer;
                textView.setText(textContainer.getText());
                break;
            case MULTIPLE_CHOICE_QUIZ:
                TextView questionText = convertView.findViewById(R.id.question_text);
                ContainerMultipleChoiceQuiz mcqContainer = (ContainerMultipleChoiceQuiz) contentContainer;
                questionText.setText(mcqContainer.getQuestion());
                
                // Set explanation text if available
                TextView explanationText = convertView.findViewById(R.id.explanation_text);
                if (explanationText != null && mcqContainer.getExplanationText() != null && !mcqContainer.getExplanationText().isEmpty()) {
                    explanationText.setText(mcqContainer.getExplanationText());
                    explanationText.setVisibility(View.VISIBLE);
                }
                // Additional setup for options would be handled by RecyclerView adapter
                break;
            case REVERSE_QUIZ:
                TextView answerText = convertView.findViewById(R.id.answer_text);
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) contentContainer;
                answerText.setText(reverseQuizContainer.getAnswer());
                // Additional setup for question options would be handled by RecyclerView adapter
                break;
            case WIRE_CONNECTING:
                TextView wireInstructions = convertView.findViewById(R.id.instructions_text);
                ContainerWireConnecting wireContainer = (ContainerWireConnecting) contentContainer;
                wireInstructions.setText(wireContainer.getInstructions());
                // Additional setup for items would be handled by RecyclerView adapters
                break;
            case FILL_IN_THE_GAPS:
                TextView gapsText = convertView.findViewById(R.id.text_with_gaps);
                ContainerFillInTheGaps gapsContainer = (ContainerFillInTheGaps) contentContainer;
                gapsText.setText(gapsContainer.getDisplayText());
                // Additional setup for word chips would be handled by ChipGroup
                break;
            case SORTING_TASK:
                TextView sortInstructions = convertView.findViewById(R.id.instructions_text);
                ContainerSortingTask sortContainer = (ContainerSortingTask) contentContainer;
                sortInstructions.setText(sortContainer.getInstructions());
                // Additional setup for sortable items would be handled by RecyclerView adapter
                break;
            case ERROR_SPOTTING:
                TextView errorInstructions = convertView.findViewById(R.id.instructions_text);
                ContainerErrorSpotting errorContainer = (ContainerErrorSpotting) contentContainer;
                errorInstructions.setText(errorContainer.getInstructions());
                
                // Set explanation text if available
                TextView errorExplanationText = convertView.findViewById(R.id.explanation_text);
                if (errorExplanationText != null && errorContainer.getExplanationText() != null && !errorContainer.getExplanationText().isEmpty()) {
                    errorExplanationText.setText(errorContainer.getExplanationText());
                    errorExplanationText.setVisibility(View.VISIBLE);
                }
                // Additional setup for items would be handled by RecyclerView adapter
                break;
            case RECAP:
                TextView recapTitle = convertView.findViewById(R.id.recap_title);
                ContainerRecap recapContainer = (ContainerRecap) contentContainer;
                recapTitle.setText(recapContainer.getRecapTitle());
                // The wrapped container would need additional handling
                break;
            case VIDEO:
            case QUIZ:
                // tbd
                break;
        }

        return convertView;
    }
}

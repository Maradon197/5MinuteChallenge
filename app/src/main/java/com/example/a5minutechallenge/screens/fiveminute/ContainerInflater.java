package com.example.a5minutechallenge.screens.fiveminute;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a5minutechallenge.R;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ContainerInflater extends AppCompatActivity {

    /**
     * Listener interface for container item selection events.
     * Used to notify the activity when a user selects an item within a container.
     */
    public interface OnContainerItemSelectedListener {
        /**
         * Called when an item is selected in a container.
         * @param container The container where the selection occurred
         * @param position The position of the selected item
         */
        void onContainerItemSelected(ContentContainer container, int position);
    }

    /**
     * Inflates the appropriate view for a content container with an optional item selection listener.
     * @param container The content container to inflate
     * @param context The context for inflation
     * @param listener Optional listener for item selection events
     * @return The inflated view
     */
    public View inflateContainerView(ContentContainer container, Context context, @Nullable OnContainerItemSelectedListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = null;

        switch (container.getType()) {
            case TITLE:
                view = inflater.inflate(R.layout.title_container, null);
                TextView titleView = view.findViewById(R.id.title_text);
                ContainerTitle titleContainer = (ContainerTitle) container;
                titleView.setText(titleContainer.getTitle());
                break;
            case TEXT:
                view = inflater.inflate(R.layout.text_container, null);
                TextView textView = view.findViewById(R.id.text_content);
                ContainerText textContainer = (ContainerText) container;
                textView.setText(textContainer.getText());
                break;
            case MULTIPLE_CHOICE_QUIZ:
                view = inflater.inflate(R.layout.multiple_choice_quiz_container, null);
                TextView questionText = view.findViewById(R.id.question_text);
                ContainerMultipleChoiceQuiz mcqContainer = (ContainerMultipleChoiceQuiz) container;
                questionText.setText(mcqContainer.getQuestion());

                RecyclerView optionsRecyclerView = view.findViewById(R.id.options_recycler_view);
                if (optionsRecyclerView != null && mcqContainer.getOptions() != null && !mcqContainer.getOptions().isEmpty()) {
                    SimpleTextAdapter.OnItemClickListener mcqClickListener = (listener != null)
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    SimpleTextAdapter optionsAdapter = new SimpleTextAdapter(mcqContainer.getOptions(), mcqClickListener);
                    optionsRecyclerView.setAdapter(optionsAdapter);
                    optionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                }
                break;
            case REVERSE_QUIZ:
                view = inflater.inflate(R.layout.reverse_quiz_container, null);
                TextView answerText = view.findViewById(R.id.answer_text);
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) container;
                answerText.setText(reverseQuizContainer.getAnswer());

                RecyclerView questionOptionsRecyclerView = view.findViewById(R.id.question_options_recycler_view);
                if (questionOptionsRecyclerView != null && reverseQuizContainer.getQuestionOptions() != null && !reverseQuizContainer.getQuestionOptions().isEmpty()) {
                    SimpleTextAdapter.OnItemClickListener reverseQuizClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    SimpleTextAdapter questionOptionsAdapter = new SimpleTextAdapter(reverseQuizContainer.getQuestionOptions(), reverseQuizClickListener);
                    questionOptionsRecyclerView.setAdapter(questionOptionsAdapter);
                    questionOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                }
                break;
            case WIRE_CONNECTING:
                view = inflater.inflate(R.layout.wire_connecting_container, null);
                TextView wireInstructions = view.findViewById(R.id.instructions_text);
                ContainerWireConnecting wireContainer = (ContainerWireConnecting) container;
                wireInstructions.setText(wireContainer.getInstructions());

                RecyclerView leftItemsRecyclerView = view.findViewById(R.id.left_items_recycler_view);
                if (leftItemsRecyclerView != null && wireContainer.getLeftItems() != null && !wireContainer.getLeftItems().isEmpty()) {
                    SimpleTextAdapter.OnItemClickListener leftItemsClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    SimpleTextAdapter leftItemsAdapter = new SimpleTextAdapter(wireContainer.getLeftItems(), leftItemsClickListener);
                    leftItemsRecyclerView.setAdapter(leftItemsAdapter);
                    leftItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                }

                RecyclerView rightItemsRecyclerView = view.findViewById(R.id.right_items_recycler_view);
                if (rightItemsRecyclerView != null && wireContainer.getRightItems() != null && !wireContainer.getRightItems().isEmpty()) {
                    SimpleTextAdapter.OnItemClickListener rightItemsClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    SimpleTextAdapter rightItemsAdapter = new SimpleTextAdapter(wireContainer.getRightItems(), rightItemsClickListener);
                    rightItemsRecyclerView.setAdapter(rightItemsAdapter);
                    rightItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                }
                break;
            case FILL_IN_THE_GAPS:
                view = inflater.inflate(R.layout.fill_in_gaps_container, null);
                TextView gapsText = view.findViewById(R.id.text_with_gaps);
                ContainerFillInTheGaps gapsContainer = (ContainerFillInTheGaps) container;
                gapsText.setText(gapsContainer.getDisplayText());

                ChipGroup wordOptionsChipGroup = view.findViewById(R.id.word_options_chip_group);
                if (wordOptionsChipGroup != null && gapsContainer.getWordOptions() != null && !gapsContainer.getWordOptions().isEmpty()) {
                    wordOptionsChipGroup.removeAllViews();
                    for (int i = 0; i < gapsContainer.getWordOptions().size(); i++) {
                        String word = gapsContainer.getWordOptions().get(i);
                        Chip chip = new Chip(context);
                        chip.setText(word);
                        chip.setClickable(true);
                        chip.setCheckable(false);
                        final int chipPosition = i; // Make final for use in anonymous class
                        if (listener != null) {
                            chip.setOnClickListener(v -> listener.onContainerItemSelected(container, chipPosition));
                        }
                        wordOptionsChipGroup.addView(chip);
                    }
                }
                break;
            case SORTING_TASK:
                view = inflater.inflate(R.layout.sorting_task_container, null);
                TextView sortInstructions = view.findViewById(R.id.instructions_text);
                ContainerSortingTask sortContainer = (ContainerSortingTask) container;
                sortInstructions.setText(sortContainer.getInstructions());

                RecyclerView sortableItemsRecyclerView = view.findViewById(R.id.sortable_items_recycler_view);
                if (sortableItemsRecyclerView != null && sortContainer.getCurrentOrder() != null && !sortContainer.getCurrentOrder().isEmpty()) {
                    SimpleTextAdapter.OnItemClickListener sortableItemsClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    SimpleTextAdapter sortableItemsAdapter = new SimpleTextAdapter(sortContainer.getCurrentOrder(), sortableItemsClickListener);
                    sortableItemsRecyclerView.setAdapter(sortableItemsAdapter);
                    sortableItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                }
                break;
            case ERROR_SPOTTING:
                view = inflater.inflate(R.layout.error_spotting_container, null);
                TextView errorInstructions = view.findViewById(R.id.instructions_text);
                ContainerErrorSpotting errorContainer = (ContainerErrorSpotting) container;
                errorInstructions.setText(errorContainer.getInstructions());

                RecyclerView itemsRecyclerView = view.findViewById(R.id.items_recycler_view);
                if (itemsRecyclerView != null && errorContainer.getItems() != null && !errorContainer.getItems().isEmpty()) {
                    SimpleTextAdapter.OnItemClickListener errorSpottingClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    SimpleTextAdapter itemsAdapter = new SimpleTextAdapter(errorContainer.getItems(), errorSpottingClickListener);
                    itemsRecyclerView.setAdapter(itemsAdapter);
                    itemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                }
                break;
            case RECAP:
                view = inflater.inflate(R.layout.recap_container, null);
                TextView recapTitle = view.findViewById(R.id.recap_title);
                FrameLayout wrappedFrame = view.findViewById(R.id.wrapped_container_frame);

                ContainerRecap recapContainer = (ContainerRecap) container;
                recapTitle.setText(recapContainer.getRecapTitle());

                ContentContainer wrapped = recapContainer.getWrappedContainer();
                if (wrapped != null) {
                    View wrappedView = inflateContainerView(wrapped, context, listener);
                    if (wrappedView != null) {
                        wrappedFrame.addView(wrappedView);
                    }
                }
                break;
            case VIDEO:
                view = inflater.inflate(R.layout.video_container, null);
                break;
            case QUIZ:
                view = inflater.inflate(R.layout.quiz_container, null);
                break;
        }

        return view;
    }

    public ContainerInflater() {
        super();
    }
}

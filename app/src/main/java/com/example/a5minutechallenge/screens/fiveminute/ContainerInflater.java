package com.example.a5minutechallenge.screens.fiveminute;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
                Button checkButton = view.findViewById(R.id.submit_button);

                ContainerMultipleChoiceQuiz mcqContainer = (ContainerMultipleChoiceQuiz) container;
                questionText.setText(mcqContainer.getQuestion());

                RecyclerView optionsRecyclerView = view.findViewById(R.id.options_recycler_view);

                if (optionsRecyclerView != null && mcqContainer.getOptions() != null && !mcqContainer.getOptions().isEmpty()) {//if not empty
                    ContentContainerAdapter.OnItemClickListener mcqClickListener = (listener != null)
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    ContentContainerAdapter optionsAdapter = new ContentContainerAdapter(mcqContainer.getOptions(), mcqClickListener);
                    optionsRecyclerView.setAdapter(optionsAdapter);
                    optionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    // Tag the RecyclerView with adapter for later access
                    optionsRecyclerView.setTag(R.id.options_recycler_view, optionsAdapter);
                }
                break;
            case REVERSE_QUIZ:
                view = inflater.inflate(R.layout.reverse_quiz_container, null);
                TextView answerText = view.findViewById(R.id.answer_text);
                ContainerReverseQuiz reverseQuizContainer = (ContainerReverseQuiz) container;
                answerText.setText(reverseQuizContainer.getAnswer());

                RecyclerView questionOptionsRecyclerView = view.findViewById(R.id.question_options_recycler_view);
                if (questionOptionsRecyclerView != null && reverseQuizContainer.getQuestionOptions() != null && !reverseQuizContainer.getQuestionOptions().isEmpty()) {
                    ContentContainerAdapter.OnItemClickListener reverseQuizClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    ContentContainerAdapter questionOptionsAdapter = new ContentContainerAdapter(reverseQuizContainer.getQuestionOptions(), reverseQuizClickListener);
                    questionOptionsRecyclerView.setAdapter(questionOptionsAdapter);
                    questionOptionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    // Tag the RecyclerView with adapter for later access
                    questionOptionsRecyclerView.setTag(R.id.question_options_recycler_view, questionOptionsAdapter);
                }
                break;
            case WIRE_CONNECTING:
                view = inflater.inflate(R.layout.wire_connecting_container, null);
                TextView wireInstructions = view.findViewById(R.id.instructions_text);
                ContainerWireConnecting wireContainer = (ContainerWireConnecting) container;
                wireInstructions.setText(wireContainer.getInstructions());

                RecyclerView leftItemsRecyclerView = view.findViewById(R.id.left_items_recycler_view);
                if (leftItemsRecyclerView != null && wireContainer.getLeftItems() != null && !wireContainer.getLeftItems().isEmpty()) {
                    DraggableAdapter.OnItemMovedListener leftItemMovedListener = listener != null
                            ? (fromPosition, toPosition) -> listener.onContainerItemSelected(container, -1) // Signal move occurred
                            : null;
                    DraggableAdapter leftItemsAdapter = new DraggableAdapter(wireContainer.getLeftItems(), null, leftItemMovedListener);
                    leftItemsRecyclerView.setAdapter(leftItemsAdapter);
                    leftItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    leftItemsAdapter.attachToRecyclerView(leftItemsRecyclerView);
                    // Tag the RecyclerView with adapter for later access
                    leftItemsRecyclerView.setTag(R.id.left_items_recycler_view, leftItemsAdapter);
                }

                RecyclerView rightItemsRecyclerView = view.findViewById(R.id.right_items_recycler_view);
                if (rightItemsRecyclerView != null && wireContainer.getRightItems() != null && !wireContainer.getRightItems().isEmpty()) {
                    DraggableAdapter.OnItemMovedListener rightItemMovedListener = listener != null
                            ? (fromPosition, toPosition) -> listener.onContainerItemSelected(container, -1) // Signal move occurred
                            : null;
                    DraggableAdapter rightItemsAdapter = new DraggableAdapter(wireContainer.getRightItems(), null, rightItemMovedListener);
                    rightItemsRecyclerView.setAdapter(rightItemsAdapter);
                    rightItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    rightItemsAdapter.attachToRecyclerView(rightItemsRecyclerView);
                    // Tag the RecyclerView with adapter for later access
                    rightItemsRecyclerView.setTag(R.id.right_items_recycler_view, rightItemsAdapter);
                }
                break;
            case FILL_IN_THE_GAPS:
                view = inflater.inflate(R.layout.fill_in_gaps_container, null);
                TextView gapsText = view.findViewById(R.id.text_with_gaps);
                ContainerFillInTheGaps gapsContainer = (ContainerFillInTheGaps) container;
                String placeholder = context.getString(R.string.gap_placeholder);
                gapsText.setText(gapsContainer.getDisplayTextWithPlaceholder(placeholder));

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
                        final TextView gapsTextView = gapsText;
                        final String placeholderStr = placeholder;
                        chip.setOnClickListener(v -> {
                            // Fill the gap with this word
                            if (gapsContainer.fillGapWithIndex(chipPosition)) {
                                // Update the display text
                                gapsTextView.setText(gapsContainer.getDisplayTextWithPlaceholder(placeholderStr));
                                // Disable the chip after use
                                chip.setEnabled(false);
                                chip.setAlpha(0.5f);
                            }
                            // Notify listener
                            if (listener != null) {
                                listener.onContainerItemSelected(container, chipPosition);
                            }
                        });
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
                    DraggableAdapter.OnItemMovedListener sortItemMovedListener = listener != null
                            ? (fromPosition, toPosition) -> {
                                // Update the container's internal state
                                sortContainer.moveItem(fromPosition, toPosition);
                                // Notify listener that a move occurred
                                listener.onContainerItemSelected(container, -1);
                            }
                            : null;
                    DraggableAdapter sortableItemsAdapter = new DraggableAdapter(sortContainer.getCurrentOrder(), null, sortItemMovedListener);
                    sortableItemsRecyclerView.setAdapter(sortableItemsAdapter);
                    sortableItemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    sortableItemsAdapter.attachToRecyclerView(sortableItemsRecyclerView);
                    // Tag the RecyclerView with adapter for later access
                    sortableItemsRecyclerView.setTag(R.id.sortable_items_recycler_view, sortableItemsAdapter);
                }
                break;
            case ERROR_SPOTTING:
                view = inflater.inflate(R.layout.error_spotting_container, null);
                TextView errorInstructions = view.findViewById(R.id.instructions_text);
                ContainerErrorSpotting errorContainer = (ContainerErrorSpotting) container;
                errorInstructions.setText(errorContainer.getInstructions());

                RecyclerView itemsRecyclerView = view.findViewById(R.id.items_recycler_view);
                if (itemsRecyclerView != null && errorContainer.getItems() != null && !errorContainer.getItems().isEmpty()) {
                    ContentContainerAdapter.OnItemClickListener errorSpottingClickListener = listener != null
                            ? position -> listener.onContainerItemSelected(container, position)
                            : null;
                    ContentContainerAdapter itemsAdapter = new ContentContainerAdapter(errorContainer.getItems(), errorSpottingClickListener);
                    itemsRecyclerView.setAdapter(itemsAdapter);
                    itemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    // Tag the RecyclerView with adapter for later access
                    itemsRecyclerView.setTag(R.id.items_recycler_view, itemsAdapter);
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
                //deprecated
                break;
        }

        return view;
    }

    public ContainerInflater() {
        super();
    }
}

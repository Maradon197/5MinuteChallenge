package com.example.a5minutechallenge.datawrapper.contentcontainer.containertypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

public class ContainerFillInTheGapsTest {

    @Test
    public void testOneBasedIndexing() {
        ContainerFillInTheGaps container = new ContainerFillInTheGaps(1);
        container.setTextTemplate("{1} is the {2} of the {3}");
        container.setCorrectWords(Arrays.asList("The", "king", "castle"));

        // Initial state
        assertEquals("___ is the ___ of the ___", container.getDisplayTextWithPlaceholder("___"));

        // Fill first gap
        container.fillGapWithIndex(0); // Assuming wordOptions has words in order for simplicity in this test
        container.setWordOptions(Arrays.asList("The", "king", "castle"));
        container.fillGapWithIndex(0);
        assertEquals("[The] is the ___ of the ___", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("The is the {2} of the {3}", container.getDisplayText());

        // Fill second gap
        container.fillGapWithIndex(1);
        assertEquals("[The] is the [king] of the ___", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("The is the king of the {3}", container.getDisplayText());

        // Fill third gap
        container.fillGapWithIndex(2);
        assertEquals("[The] is the [king] of the [castle]", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("The is the king of the castle", container.getDisplayText());
    }

    @Test
    public void testZeroBasedIndexing() {
        ContainerFillInTheGaps container = new ContainerFillInTheGaps(1);
        container.setTextTemplate("{0} is the {1}");
        container.setCorrectWords(Arrays.asList("Java", "best"));
        container.setWordOptions(Arrays.asList("Java", "best"));

        assertEquals("___ is the ___", container.getDisplayTextWithPlaceholder("___"));

        container.fillGapWithIndex(0);
        assertEquals("[Java] is the ___", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("Java is the {1}", container.getDisplayText());

        container.fillGapWithIndex(1);
        assertEquals("[Java] is the [best]", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("Java is the best", container.getDisplayText());
    }

    @Test
    public void testEmptyBracketIndexing() {
        ContainerFillInTheGaps container = new ContainerFillInTheGaps(1);
        container.setTextTemplate("{} is the {}");
        container.setCorrectWords(Arrays.asList("Paris", "capital"));
        container.setWordOptions(Arrays.asList("Paris", "capital"));

        assertEquals("___ is the ___", container.getDisplayTextWithPlaceholder("___"));

        container.fillGapWithIndex(0);
        assertEquals("[Paris] is the ___", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("Paris is the {}", container.getDisplayText());

        container.fillGapWithIndex(1);
        assertEquals("[Paris] is the [capital]", container.getDisplayTextWithPlaceholder("___"));
        assertEquals("Paris is the capital", container.getDisplayText());
    }
}

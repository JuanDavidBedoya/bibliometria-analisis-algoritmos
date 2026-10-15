package com.uniquindio.bibliometria.algorithms.similarity.classic;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.uniquindio.bibliometria.algorithms.similarity.classic.JaccardSimilarity.EmptyUnionPolicy.*;

class JaccardSimilarityTest {
    private final JaccardSimilarity algorithm = new JaccardSimilarity();

    @Test void equalSetsIgnoreFrequencyAndOrder() {
        var result = algorithm.compare(List.of("AI", "learning", "AI"), List.of("learning", "AI"), REJECT);
        assertEquals(1, result.similarity());
        assertEquals(List.of("AI", "learning"), result.intersection());
    }
    @Test void disjointSets() {
        var result = algorithm.compare(List.of("a"), List.of("b"), REJECT);
        assertEquals(0, result.similarity());
        assertEquals(List.of("a", "b"), result.union());
    }
    @Test void partialOverlapHasMathematicalResult() {
        var result = algorithm.compare(List.of("a", "b", "b"), List.of("b", "c"), REJECT);
        assertEquals(List.of("b"), result.intersection());
        assertEquals(List.of("a", "b", "c"), result.union());
        assertEquals(1.0 / 3, result.similarity(), 1e-12);
    }
    @Test void oneEmptySetIsZero() {
        assertEquals(0, algorithm.compare(List.of(), List.of("a"), REJECT).similarity());
        assertEquals(0, algorithm.compare(List.of("a"), List.of(), REJECT).similarity());
    }
    @Test void emptyUnionRequiresExplicitPolicy() {
        assertThrows(IllegalArgumentException.class, () -> algorithm.compare(List.of(), List.of(), REJECT));
        assertEquals(0, algorithm.compare(List.of(), List.of(), ZERO).similarity());
        assertEquals(1, algorithm.compare(List.of(), List.of(), ONE).similarity());
    }
    @Test void nullAndBlankTokensAreRejected() {
        assertThrows(NullPointerException.class, () -> algorithm.compare(null, List.of(), REJECT));
        assertThrows(NullPointerException.class, () -> algorithm.compare(Arrays.asList("a", null), List.of(), REJECT));
        assertThrows(IllegalArgumentException.class, () -> algorithm.compare(List.of(" "), List.of(), REJECT));
    }
    @Test void symmetryAndBoundsForSeveralInputs() {
        var samples = List.of(List.<String>of(), List.of("a"), List.of("a", "b"), List.of("b", "c", "c"));
        for (var left : samples) for (var right : samples) {
            double forward = algorithm.compare(left, right, ZERO).similarity();
            assertEquals(forward, algorithm.compare(right, left, ZERO).similarity());
            assertTrue(forward >= 0 && forward <= 1);
        }
    }
    @Test void resultCannotBeMutated() {
        var result = algorithm.compare(List.of("a"), List.of("a"), REJECT);
        assertThrows(UnsupportedOperationException.class, () -> result.union().add("b"));
    }
}

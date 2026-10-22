package com.uniquindio.bibliometria.ingestion;

import java.util.List;
import java.util.Objects;

/** Temporary extraction result, not a database entity. Never implies manual validation. */
public record ExtractedArticle(String source, String title, List<String> authors,
                               String originalAbstract, List<String> reviewNotes,
                               String spacingReconstructedAbstract) {
    public ExtractedArticle(String source, String title, List<String> authors,
                            String originalAbstract, List<String> reviewNotes) {
        this(source, title, authors, originalAbstract, reviewNotes, originalAbstract);
    }

    public ExtractedArticle {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(originalAbstract, "originalAbstract");
        Objects.requireNonNull(spacingReconstructedAbstract, "spacingReconstructedAbstract");
        authors = List.copyOf(authors);
        reviewNotes = List.copyOf(reviewNotes);
    }

    public boolean hasRequiredFields() {
        return !title.isBlank() && !authors.isEmpty() && !originalAbstract.isBlank();
    }
}

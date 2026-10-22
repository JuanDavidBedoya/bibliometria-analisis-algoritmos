package com.uniquindio.bibliometria.ingestion;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ArticleExtractorTest {
    private PdfPage.Line line(String text, float x, float y, float size) {
        return new PdfPage.Line(text, x, y, 550, size);
    }
    private ExtractedArticle extract(String title, PdfPage.Line... lines) {
        return new ArticleExtractor().extract(new PdfTextDocument("sample.pdf", title,
                List.of(new PdfPage(1, 600, 800, "", List.of(lines)))));
    }
    @Test void spacingEvidenceIsCarriedSeparatelyWithoutChangingOriginalRegion() {
        var result = extract("A study of artificial intelligence",
                line("A study of artificial intelligence", 40, 100, 15),
                line("Alice Smith", 40, 125, 10),
                new PdfPage.Line("Abstract: Modelslearn.", 200, 180, 550, 9,
                        "Abstract: Models learn."),
                new PdfPage.Line("Resultsimprove.", 200, 193, 550, 9, "Results improve."),
                line("Keywords: education", 200, 206, 9));
        assertEquals("Modelslearn.\nResultsimprove.", result.originalAbstract());
        assertEquals("Models learn.\nResults improve.", result.spacingReconstructedAbstract());
        assertFalse(result.spacingReconstructedAbstract().contains("Keywords"));
    }
    @Test void extractsSeparateColumnsWithoutKeywords() {
        var result = extract("A study of artificial intelligence",
                line("A study of artificial intelligence", 40, 100, 15),
                line("Alice Smith, Bob Jones", 40, 125, 10),
                line("Department of Computing, University X", 40, 143, 7),
                line("A B S T R A C T", 200, 180, 8),
                line("Keywords:", 40, 195, 7),
                line("AI improves learning.", 200, 200, 9),
                line("machine learning", 40, 205, 7),
                line("We investigate its effects.", 200, 210, 9),
                line("1. Introduction", 40, 250, 9));
        assertEquals("A study of artificial intelligence", result.title());
        assertEquals(List.of("Alice Smith", "Bob Jones"), result.authors());
        assertEquals("AI improves learning.\nWe investigate its effects.", result.originalAbstract());
        assertTrue(result.hasRequiredFields());
    }
    @Test void keepsStructuredAbstractAndStopsAtSpacedKeywords() {
        var result = extract("A structured review of education",
                line("A structured review of education", 40, 100, 15),
                line("Alice Smith and Bob Jones", 40, 125, 10),
                line("Abstract", 40, 180, 8),
                line("Purpose – Study education.", 40, 200, 9),
                line("Findings – Positive effects.", 40, 210, 9),
                line("K E Y W O R D S", 40, 220, 8),
                line("education, AI", 40, 230, 9));
        assertEquals("Purpose – Study education.\nFindings – Positive effects.", result.originalAbstract());
    }
    @Test void inlineAbstractDoesNotLoseFirstSentence() {
        var result = extract("A systematic study of education",
                line("A systematic study of education", 40, 100, 15),
                line("Alice Smith", 40, 125, 10),
                line("Abstract: First sentence.", 200, 180, 9),
                line("Second sentence.", 200, 193, 9),
                line("Keywords: education", 200, 206, 9));
        assertEquals("First sentence.\nSecond sentence.", result.originalAbstract());
    }
    @Test void invalidMetadataFallsBackToVisibleTitle() {
        var result = extract("proof 100..120",
                line("A structured review of education", 40, 100, 15),
                line("Alice Smith", 40, 125, 10),
                line("Abstract", 40, 180, 8),
                line("Purpose – Study education.", 40, 200, 9),
                line("Keywords: education", 40, 210, 9));
        assertEquals("A structured review of education", result.title());
        assertTrue(result.reviewNotes().stream().anyMatch(n -> n.contains("metadata")));
    }
    @Test void missingAbstractIsReportedWithoutInventingFields() {
        var result = extract("A title without an abstract", line("A title without an abstract", 40, 100, 15));
        assertFalse(result.hasRequiredFields());
        assertEquals("A title without an abstract", result.title());
        assertEquals("", result.originalAbstract());
        assertTrue(result.reviewNotes().stream().anyMatch(n -> n.contains("heading")));
    }
    @Test void missingAuthorsIsReported() {
        var result = extract("A title without listed authors",
                line("A title without listed authors", 40, 100, 15),
                line("Abstract", 40, 180, 8),
                line("Text about education.", 40, 200, 9),
                line("Keywords: education", 40, 210, 9));
        assertFalse(result.hasRequiredFields());
        assertTrue(result.authors().isEmpty());
    }
    @Test void unboundedAbstractIsNotMarkedValidated() {
        var result = extract("A study of artificial intelligence",
                line("A study of artificial intelligence", 40, 100, 15),
                line("Alice Smith", 40, 125, 10),
                line("Abstract", 200, 180, 8),
                line("Text about education.", 200, 200, 9));
        assertTrue(result.reviewNotes().stream().anyMatch(n -> n.contains("abstract end")));
        assertTrue(result.reviewNotes().stream().anyMatch(n -> n.contains("Manual")));
    }
}

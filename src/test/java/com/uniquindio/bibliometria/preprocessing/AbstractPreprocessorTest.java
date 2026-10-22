package com.uniquindio.bibliometria.preprocessing;

import com.uniquindio.bibliometria.ingestion.ExtractedArticle;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AbstractPreprocessorTest {
    private final AbstractPreprocessor preprocessor = new AbstractPreprocessor();
    private ExtractedArticle article(String text) {
        return new ExtractedArticle("sample.pdf", "Sample", List.of("Author"), text, List.of());
    }

    @Test void normalLineJoinKeepsSpaceBetweenSentencesWithLowercaseAndNormalization() {
        String original = "first sentence.\nSecond sentence";
        var article = article(original);
        var options = new PreprocessingOptions(true, true, false, false);
        var result = preprocessor.process(article, options);
        assertEquals("first sentence. second sentence", result.processedAbstract());
        assertEquals(original, result.article().originalAbstract());
        assertEquals("first sentence. second sentence", preprocessor.process(article).processedAbstract());
    }

    @Test void reconstructionCannotDiscardSourceLineBoundaryBetweenSentences() {
        for (String newline : List.of("\n", "\r\n", "\r", "\u0085", "\u2028", "\u2029")) {
            String original = "first sentence." + newline + "Second sentence";
            var article = new ExtractedArticle("sample.pdf", "Sample", List.of("Author"), original,
                    List.of(), "first sentence.Second sentence");
            var result = preprocessor.process(article);
            assertEquals("first sentence. second sentence", result.processedAbstract());
            assertEquals(original, result.article().originalAbstract());
        }
    }
    @Test void defaultCleaningKeepsStopwordsNumbersAndAcronymsAndOriginal() {
        var article = article("THE\u00a0 AI 2024\n ﬁeld edu-\ncation.");
        var result = preprocessor.process(article);
        assertEquals("the ai 2024 ﬁeld education.", result.processedAbstract());
        assertEquals(List.of("the", "ai", "2024", "ﬁeld", "education"), result.tokens());
        assertEquals("THE\u00a0 AI 2024\n ﬁeld edu-\ncation.", result.article().originalAbstract());
        assertFalse(result.options().normalizeUnicode());
        assertTrue(result.options().joinLineHyphenation());
        assertTrue(result.options().reconstructWordSpacing());
    }
    @Test void preserveModeDoesNotChangeOriginal() {
        var article = article("AI\n  learning 2024.");
        var processed = preprocessor.process(article, PreprocessingOptions.preserveText());
        assertEquals(article.originalAbstract(), processed.processedAbstract());
        assertEquals(List.of("AI", "learning", "2024"), processed.tokens());
    }
    @Test void explicitNormalizationPreservesNumbersAndAcronyms() {
        var article = article("AI\u00a0  2024\nlearning");
        var processed = preprocessor.process(article, new PreprocessingOptions(true, true, false, false));
        assertEquals("ai 2024 learning", processed.processedAbstract());
        assertEquals("AI\u00a0  2024\nlearning", processed.article().originalAbstract());
    }
    @Test void unicodeLigaturesAreOnlyNormalizedWhenRequested() {
        assertEquals("ﬁeld", preprocessor.process(article("ﬁeld"), PreprocessingOptions.preserveText()).processedAbstract());
        assertEquals("field", preprocessor.process(article("ﬁeld"), new PreprocessingOptions(false, false, true, false)).processedAbstract());
    }
    @Test void lineHyphenationIsOptInAndTechnicalCompoundsRemain() {
        var article = article("edu-\ncation AI-driven");
        assertEquals("edu-\ncation AI-driven", preprocessor.process(article, PreprocessingOptions.preserveText()).processedAbstract());
        assertEquals("education AI-driven", preprocessor.process(article, new PreprocessingOptions(false, false, false, true)).processedAbstract());
    }

    @Test void reconstructsLineBreaksBeforeNormalizingAllWhitespace() {
        var original = "practi-\r\n tioners  and re-\nlationships\u00a0\tAI-driven 2024";
        var article = article(original);
        var result = preprocessor.process(article);
        assertEquals("practitioners and relationships ai-driven 2024", result.processedAbstract());
        assertEquals(original, result.article().originalAbstract());
        assertEquals(List.of("practitioners", "and", "relationships", "ai-driven", "2024"), result.tokens());
    }

    @Test void noDictionarySplittingOfJoinedWordsWithoutPdfEvidence() {
        var article = article("alsoprovides microscope AI-driven");
        assertEquals("alsoprovides microscope ai-driven", preprocessor.process(article).processedAbstract());
    }

    @Test void usesSpacingEvidenceOnlyInProcessedTextAndCanDisableIt() {
        var original = "The study alsoprovides\nnew evidence.";
        var article = new ExtractedArticle("sample.pdf", "Sample", List.of("Author"), original,
                List.of(), "The study also provides\nnew evidence.");
        var result = preprocessor.process(article);
        assertEquals("the study also provides new evidence.", result.processedAbstract());
        assertEquals(original, article.originalAbstract());
        assertEquals(original, preprocessor.process(article, PreprocessingOptions.preserveText()).processedAbstract());
    }

    @Test void preservesSourceBoundariesAfterPunctuationWhenCandidateLosesThem() {
        String original = "Analysis approaches. The results! Another finding? More; still: useful.";
        var article = new ExtractedArticle("sample.pdf", "Sample", List.of("Author"), original,
                List.of(), "Analysis approaches.The results!Another finding?More;still:useful.");
        var result = preprocessor.process(article);
        assertEquals("analysis approaches. the results! another finding? more; still: useful.",
                result.processedAbstract());
        assertEquals(original, result.article().originalAbstract());
        assertEquals(original, preprocessor.process(article, PreprocessingOptions.preserveText()).processedAbstract());
    }

    @Test void combinesSpacingEvidenceWithoutBreakingDecimalsAbbreviationsOrUrls() {
        String original = "Modelslearn. The value is 3.14, e.g. https://example.org/v1.2.";
        var article = new ExtractedArticle("sample.pdf", "Sample", List.of("Author"), original,
                List.of(), "Models learn.The value is 3.14, e.g. https://example.org/v1.2.");
        assertEquals("models learn. the value is 3.14, e.g. https://example.org/v1.2.",
                preprocessor.process(article).processedAbstract());
        assertEquals(original, article.originalAbstract());
    }

    @Test void rejectsNonSpacingChangesInReconstructionCandidate() {
        var article = new ExtractedArticle("sample.pdf", "Sample", List.of("Author"),
                "Correct results.", List.of(), "Invented results.");
        assertEquals("correct results.", preprocessor.process(article).processedAbstract());
    }

    @Test void howAndAiAreIndependentTokensWithSpaceOrCommaSeparators() {
        for (String input : List.of("how ai", "how,ai", "how, ai", "how\u00a0ai")) {
            assertEquals(List.of("how", "ai"), preprocessor.tokenize(input));
        }
        assertEquals(List.of("how", "ai"), preprocessor.process(article("HOW AI")).tokens());
    }

    @Test void retainsInlineHyphensAndDoesNotJoinBeforeUppercaseNumbersOrBlankLines() {
        var result = preprocessor.process(article("AI-driven 2024-2025 pre-\nAI 2024-\n2025 end-\n\nstart"));
        assertEquals("ai-driven 2024-2025 pre- ai 2024- 2025 end- start", result.processedAbstract());
    }
    @Test void tokenizationHandlesUnicodeMarksNumbersAndInternalConnectors() {
        // An apostrophe without a following letter is punctuation, not part of a token.
        assertEquals(List.of("café", "cafe\u0301", "2024", "AI-driven", "don't", "students", "learning"),
                preprocessor.tokenize("café cafe\u0301, 2024; AI-driven don't students’ learning."));
    }
    @Test void emptyTextHasNoTokens() {
        assertEquals(List.of(), preprocessor.tokenize(""));
        assertEquals(List.of(), preprocessor.tokenize("... \n"));
    }
    @Test void missingExtractionCannotSilentlyFeedAlgorithms() {
        var missing = new ExtractedArticle("bad.pdf", "", List.of(), "text", List.of("missing title"));
        assertThrows(IllegalArgumentException.class, () -> preprocessor.process(missing, PreprocessingOptions.preserveText()));
    }
    @Test void processingIsRepeatableAndResultsImmutable() {
        var options = new PreprocessingOptions(true, true, true, false);
        var a = preprocessor.process(article(" AI\n ﬁeld 2024 "), options);
        var b = preprocessor.process(article(a.processedAbstract()), options);
        assertEquals(a.processedAbstract(), b.processedAbstract());
        assertEquals(a.tokens(), b.tokens());
        assertThrows(UnsupportedOperationException.class, () -> a.tokens().add("x"));
    }
}

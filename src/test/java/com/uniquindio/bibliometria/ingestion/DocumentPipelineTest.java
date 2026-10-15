package com.uniquindio.bibliometria.ingestion;

import com.uniquindio.bibliometria.preprocessing.AbstractPreprocessor;
import com.uniquindio.bibliometria.preprocessing.PreprocessingOptions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DocumentPipelineTest {
    @Test void defaultPolicyIsAppliedThroughPipeline() {
        var pipeline = new DocumentPipeline((source, bytes) -> {
            throw new AssertionError("Preprocessing must not read the PDF");
        }, new ArticleExtractor(), new AbstractPreprocessor());
        var article = new ExtractedArticle("sample.pdf", "Title", List.of("Author"),
                "THE AI\n 2024", List.of());
        var result = pipeline.preprocess(article);
        assertEquals("the ai 2024", result.processedAbstract());
        assertEquals(List.of("the", "ai", "2024"), result.tokens());
        assertEquals("THE AI\n 2024", result.article().originalAbstract());
    }
    @Test void correctedExtractionCanBeProcessedWithoutChangingOriginal() {
        var pipeline = new DocumentPipeline((source, bytes) -> {
            throw new AssertionError("Preprocessing corrected metadata must not re-read the PDF");
        }, new ArticleExtractor(), new AbstractPreprocessor());
        var corrected = new ExtractedArticle("sample.pdf", "Verified title", List.of("Verified author"),
                "AI\nlearning 2024.", List.of("Manually checked"));
        var result = pipeline.preprocess(corrected, new PreprocessingOptions(true, true, false, false));
        assertEquals("ai learning 2024.", result.processedAbstract());
        assertSame(corrected, result.article());
        assertEquals("AI\nlearning 2024.", corrected.originalAbstract());
    }
    @Test void readingFailureIsNotConvertedIntoAnInventedArticle() {
        var pipeline = new DocumentPipeline((source, bytes) -> { throw new IOException("Unreadable PDF"); },
                new ArticleExtractor(), new AbstractPreprocessor());
        assertThrows(IOException.class, () -> pipeline.extract("bad.pdf", new byte[0]));
    }
}

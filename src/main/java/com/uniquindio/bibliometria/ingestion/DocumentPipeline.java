package com.uniquindio.bibliometria.ingestion;

import com.uniquindio.bibliometria.preprocessing.AbstractPreprocessor;
import com.uniquindio.bibliometria.preprocessing.PreprocessingOptions;
import com.uniquindio.bibliometria.preprocessing.ProcessedArticle;
import java.io.IOException;
import java.util.Objects;

/** Injectable entry point for backend integration without imposing controllers or database tables. */
public final class DocumentPipeline {
    private final PdfTextReader reader;
    private final ArticleExtractor extractor;
    private final AbstractPreprocessor preprocessor;

    public DocumentPipeline(PdfTextReader reader, ArticleExtractor extractor,
                            AbstractPreprocessor preprocessor) {
        this.reader = Objects.requireNonNull(reader);
        this.extractor = Objects.requireNonNull(extractor);
        this.preprocessor = Objects.requireNonNull(preprocessor);
    }

    public ExtractedArticle extract(String source, byte[] pdf) throws IOException {
        return extractor.extract(reader.read(source, pdf));
    }

    /** Can also consume a manually corrected extraction, keeping corrections outside PDF rules. */
    public ProcessedArticle preprocess(ExtractedArticle article) {
        return preprocessor.process(article);
    }

    public ProcessedArticle preprocess(ExtractedArticle article, PreprocessingOptions options) {
        return preprocessor.process(article, options);
    }
}

package com.uniquindio.bibliometria.preprocessing;

import com.uniquindio.bibliometria.ingestion.ExtractedArticle;
import java.util.List;
import java.util.Objects;

public record ProcessedArticle(ExtractedArticle article, String processedAbstract,
                               List<String> tokens, PreprocessingOptions options) {
    public ProcessedArticle {
        Objects.requireNonNull(article, "article");
        Objects.requireNonNull(processedAbstract, "processedAbstract");
        Objects.requireNonNull(options, "options");
        tokens = List.copyOf(tokens);
    }
}

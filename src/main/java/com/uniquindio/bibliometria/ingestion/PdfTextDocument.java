package com.uniquindio.bibliometria.ingestion;

import java.util.List;
import java.util.Objects;

public record PdfTextDocument(String source, String metadataTitle, List<PdfPage> pages) {
    public PdfTextDocument {
        Objects.requireNonNull(source, "source");
        metadataTitle = metadataTitle == null ? "" : metadataTitle;
        pages = List.copyOf(pages);
    }
}

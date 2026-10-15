package com.uniquindio.bibliometria.ingestion;

import java.io.IOException;

/** Adapter boundary: extraction rules do not depend on Spring or persistence. */
public interface PdfTextReader {
    PdfTextDocument read(String source, byte[] pdf) throws IOException;
}

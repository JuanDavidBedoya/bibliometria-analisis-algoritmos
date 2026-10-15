package com.uniquindio.bibliometria.ingestion;

import org.junit.jupiter.api.Test;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.jupiter.api.Assertions.*;

class PdfFontWarningFilterTest {
    private final PdfFontWarningFilter filter = new PdfFontWarningFilter();
    private LogRecord record(Level level, String logger, String message) {
        var record = new LogRecord(level, message);
        record.setLoggerName(logger);
        return record;
    }
    @Test void hidesOnlyPdfBoxUnmappedGlyphWarnings() {
        for (String font : new String[]{"PDSimpleFont", "PDType0Font"}) {
            assertFalse(filter.isLoggable(record(Level.WARNING,
                    "org.apache.pdfbox.pdmodel.font." + font, "No Unicode mapping for C19 (1)")));
        }
    }
    @Test void fontErrorsRemainVisible() {
        assertTrue(filter.isLoggable(record(Level.SEVERE, "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                "No Unicode mapping for critical error")));
    }
    @Test void otherWarningsRemainVisible() {
        assertTrue(filter.isLoggable(record(Level.WARNING, "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                "Font file is damaged")));
        assertTrue(filter.isLoggable(record(Level.WARNING, "application.other",
                "No Unicode mapping for a different component")));
    }
    @Test void incompleteLogMetadataRemainsVisible() {
        assertTrue(filter.isLoggable(record(Level.WARNING, null, "No Unicode mapping for C19")));
        assertTrue(filter.isLoggable(record(Level.WARNING, "org.apache.pdfbox.pdmodel.font.PDSimpleFont", null)));
    }
}

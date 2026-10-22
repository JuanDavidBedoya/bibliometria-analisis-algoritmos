package com.uniquindio.bibliometria.ingestion;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/** Optional console filter; does not alter extraction or suppress errors/other warnings. */
public final class PdfFontWarningFilter implements Filter {
    @Override
    public boolean isLoggable(LogRecord record) {
        String logger = record.getLoggerName();
        String message = record.getMessage();
        return !(Level.WARNING.equals(record.getLevel())
                && logger != null && logger.startsWith("org.apache.pdfbox.pdmodel.font.")
                && message != null && message.startsWith("No Unicode mapping for "));
    }
}

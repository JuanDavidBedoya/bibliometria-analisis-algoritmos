package com.uniquindio.bibliometria.ingesta;

import org.junit.jupiter.api.Test;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.jupiter.api.Assertions.*;

class FiltroAvisosFuentesTest {
    private final FiltroAvisosFuentes filter = new FiltroAvisosFuentes();
    private LogRecord record(Level nivel, String nombreRegistro, String mensaje) {
        var record = new LogRecord(nivel, mensaje);
        record.setLoggerName(nombreRegistro);
        return record;
    }
    @Test void ocultaSoloLosAvisosDeCaracteresSinCorrespondencia() {
        for (String fuente : new String[]{"PDSimpleFont", "PDType0Font"}) {
            assertFalse(filter.isLoggable(record(Level.WARNING,
                    "org.apache.pdfbox.pdmodel.font." + fuente, "No Unicode mapping for C19 (1)")));
        }
    }
    @Test void conservaLosErroresDeFuentes() {
        assertTrue(filter.isLoggable(record(Level.SEVERE, "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                "No Unicode mapping for critical error")));
    }
    @Test void conservaLasDemasAdvertencias() {
        assertTrue(filter.isLoggable(record(Level.WARNING, "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                "Font file is damaged")));
        assertTrue(filter.isLoggable(record(Level.WARNING, "application.other",
                "No Unicode mapping for a different component")));
    }
    @Test void conservaLosMensajesSinMetadatos() {
        assertTrue(filter.isLoggable(record(Level.WARNING, null, "No Unicode mapping for C19")));
        assertTrue(filter.isLoggable(record(Level.WARNING, "org.apache.pdfbox.pdmodel.font.PDSimpleFont", null)));
    }
}

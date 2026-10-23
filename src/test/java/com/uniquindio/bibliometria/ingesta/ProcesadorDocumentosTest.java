package com.uniquindio.bibliometria.ingesta;

import com.uniquindio.bibliometria.preprocesamiento.ProcesadorResumen;
import com.uniquindio.bibliometria.preprocesamiento.OpcionesPreprocesamiento;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProcesadorDocumentosTest {
    @Test void aplicaLaLimpiezaPredeterminada() {
        var procesadorDocumentos = new ProcesadorDocumentos((archivo, bytes) -> {
            throw new AssertionError("Preprocessing must not read the PDF");
        }, new ExtractorArticulos(), new ProcesadorResumen());
        var articulo = new ArticuloExtraido("sample.pdf", "Title", List.of("Author"),
                "THE AI\n 2024", List.of());
        var resultado = procesadorDocumentos.preprocesar(articulo);
        assertEquals("the ai 2024", resultado.resumenProcesado());
        assertEquals(List.of("the", "ai", "2024"), resultado.palabras());
        assertEquals("THE AI\n 2024", resultado.articulo().resumenOriginal());
    }
    @Test void procesaUnaCorreccionSinModificarElOriginal() {
        var procesadorDocumentos = new ProcesadorDocumentos((archivo, bytes) -> {
            throw new AssertionError("Preprocessing corrected metadata must not re-read the PDF");
        }, new ExtractorArticulos(), new ProcesadorResumen());
        var corrected = new ArticuloExtraido("sample.pdf", "Verified title", List.of("Verified author"),
                "AI\nlearning 2024.", List.of("Manually checked"));
        var resultado = procesadorDocumentos.preprocesar(corrected, new OpcionesPreprocesamiento(true, true, false, false));
        assertEquals("ai learning 2024.", resultado.resumenProcesado());
        assertSame(corrected, resultado.articulo());
        assertEquals("AI\nlearning 2024.", corrected.resumenOriginal());
    }
    @Test void conservaLosErroresDeLectura() {
        var procesadorDocumentos = new ProcesadorDocumentos((archivo, bytes) -> { throw new IOException("Unreadable PDF"); },
                new ExtractorArticulos(), new ProcesadorResumen());
        assertThrows(IOException.class, () -> procesadorDocumentos.extraer("bad.pdf", new byte[0]));
    }
}

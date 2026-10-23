package com.uniquindio.bibliometria.ingesta;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorArticulosTest {
    private PaginaPdf.Linea linea(String texto, float x, float y, float size) {
        return new PaginaPdf.Linea(texto, x, y, 550, size);
    }
    private ArticuloExtraido extraer(String titulo, PaginaPdf.Linea... lineas) {
        return new ExtractorArticulos().extraer(new DocumentoPdf("sample.pdf", titulo,
                List.of(new PaginaPdf(1, 600, 800, "", List.of(lineas)))));
    }
    @Test void conservaElOriginalSeparadoDeLaReconstruccion() {
        var resultado = extraer("A study of artificial intelligence",
                linea("A study of artificial intelligence", 40, 100, 15),
                linea("Alice Smith", 40, 125, 10),
                new PaginaPdf.Linea("Abstract: Modelslearn.", 200, 180, 550, 9,
                        "Abstract: Models learn."),
                new PaginaPdf.Linea("Resultsimprove.", 200, 193, 550, 9, "Results improve."),
                linea("Keywords: education", 200, 206, 9));
        assertEquals("Modelslearn.\nResultsimprove.", resultado.resumenOriginal());
        assertEquals("Models learn.\nResults improve.", resultado.resumenConEspacios());
        assertFalse(resultado.resumenConEspacios().contains("Keywords"));
    }
    @Test void extraeElResumenSinIncluirPalabrasClaveDeOtraColumna() {
        var resultado = extraer("A study of artificial intelligence",
                linea("A study of artificial intelligence", 40, 100, 15),
                linea("Alice Smith, Bob Jones", 40, 125, 10),
                linea("Department of Computing, University X", 40, 143, 7),
                linea("A B S T R A C T", 200, 180, 8),
                linea("Keywords:", 40, 195, 7),
                linea("AI improves learning.", 200, 200, 9),
                linea("machine learning", 40, 205, 7),
                linea("We investigate its effects.", 200, 210, 9),
                linea("1. Introduction", 40, 250, 9));
        assertEquals("A study of artificial intelligence", resultado.titulo());
        assertEquals(List.of("Alice Smith", "Bob Jones"), resultado.autores());
        assertEquals("AI improves learning.\nWe investigate its effects.", resultado.resumenOriginal());
        assertTrue(resultado.tieneCamposCompletos());
    }
    @Test void conservaElResumenEstructurado() {
        var resultado = extraer("A structured review of education",
                linea("A structured review of education", 40, 100, 15),
                linea("Alice Smith and Bob Jones", 40, 125, 10),
                linea("Abstract", 40, 180, 8),
                linea("Purpose – Study education.", 40, 200, 9),
                linea("Findings – Positive effects.", 40, 210, 9),
                linea("K E Y W O R D S", 40, 220, 8),
                linea("education, AI", 40, 230, 9));
        assertEquals("Purpose – Study education.\nFindings – Positive effects.", resultado.resumenOriginal());
    }
    @Test void conservaLaPrimeraFraseDelResumenEnLinea() {
        var resultado = extraer("A systematic study of education",
                linea("A systematic study of education", 40, 100, 15),
                linea("Alice Smith", 40, 125, 10),
                linea("Abstract: First sentence.", 200, 180, 9),
                linea("Second sentence.", 200, 193, 9),
                linea("Keywords: education", 200, 206, 9));
        assertEquals("First sentence.\nSecond sentence.", resultado.resumenOriginal());
    }
    @Test void usaElTituloVisibleSiLosMetadatosNoCoinciden() {
        var resultado = extraer("proof 100..120",
                linea("A structured review of education", 40, 100, 15),
                linea("Alice Smith", 40, 125, 10),
                linea("Abstract", 40, 180, 8),
                linea("Purpose – Study education.", 40, 200, 9),
                linea("Keywords: education", 40, 210, 9));
        assertEquals("A structured review of education", resultado.titulo());
        assertTrue(resultado.observaciones().stream().anyMatch(n -> n.contains("metadatos")));
    }
    @Test void avisaCuandoFaltaElResumen() {
        var resultado = extraer("A title without an abstract", linea("A title without an abstract", 40, 100, 15));
        assertFalse(resultado.tieneCamposCompletos());
        assertEquals("A title without an abstract", resultado.titulo());
        assertEquals("", resultado.resumenOriginal());
        assertTrue(resultado.observaciones().stream().anyMatch(n -> n.contains("encabezado")));
    }
    @Test void avisaCuandoFaltanAutores() {
        var resultado = extraer("A title without listed authors",
                linea("A title without listed authors", 40, 100, 15),
                linea("Abstract", 40, 180, 8),
                linea("Text about education.", 40, 200, 9),
                linea("Keywords: education", 40, 210, 9));
        assertFalse(resultado.tieneCamposCompletos());
        assertTrue(resultado.autores().isEmpty());
    }
    @Test void avisaSiElFinalDelResumenNoEsExplicito() {
        var resultado = extraer("A study of artificial intelligence",
                linea("A study of artificial intelligence", 40, 100, 15),
                linea("Alice Smith", 40, 125, 10),
                linea("Abstract", 200, 180, 8),
                linea("Text about education.", 200, 200, 9));
        assertTrue(resultado.observaciones().stream().anyMatch(n -> n.contains("final explicito")));
        assertTrue(resultado.observaciones().stream().anyMatch(n -> n.contains("revision manual")));
    }
}

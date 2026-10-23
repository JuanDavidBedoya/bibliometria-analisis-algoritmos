package com.uniquindio.bibliometria.service;

import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreprocesamientoServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void extraerFrecuenciasGlobalesFiltrandoStopwords() {
        PreprocesamientoService service = new PreprocesamientoService();

        ArticuloDocumental art1 = new ArticuloDocumental(
                1,
                "art1.pdf",
                "Titulo 1",
                List.of("Autor 1"),
                "Original",
                "education intelligence artificial",
                List.of("education", "intelligence", "artificial", "the", "a"),
                5
        );

        ArticuloDocumental art2 = new ArticuloDocumental(
                2,
                "art2.pdf",
                "Titulo 2",
                List.of("Autor 2"),
                "Original 2",
                "education learning intelligence",
                List.of("education", "learning", "intelligence", "in", "and"),
                5
        );

        List<TerminoFrecuencia> frecuencias = service.extraerFrecuenciasGlobales(List.of(art1, art2), true);

        // "the", "a", "in", "and" deben ser filtradas por stopwords
        assertFalse(frecuencias.stream().anyMatch(tf -> tf.getTermino().equals("the")));
        assertFalse(frecuencias.stream().anyMatch(tf -> tf.getTermino().equals("a")));
        assertFalse(frecuencias.stream().anyMatch(tf -> tf.getTermino().equals("in")));
        assertFalse(frecuencias.stream().anyMatch(tf -> tf.getTermino().equals("and")));

        // education debe tener frecuencia 2
        TerminoFrecuencia education = frecuencias.stream()
                .filter(tf -> tf.getTermino().equals("education"))
                .findFirst()
                .orElse(null);
        assertNotNull(education);
        assertEquals(2, education.getFrecuencia());

        // intelligence debe tener frecuencia 2
        TerminoFrecuencia intelligence = frecuencias.stream()
                .filter(tf -> tf.getTermino().equals("intelligence"))
                .findFirst()
                .orElse(null);
        assertNotNull(intelligence);
        assertEquals(2, intelligence.getFrecuencia());

        // learning debe tener frecuencia 1
        TerminoFrecuencia learning = frecuencias.stream()
                .filter(tf -> tf.getTermino().equals("learning"))
                .findFirst()
                .orElse(null);
        assertNotNull(learning);
        assertEquals(1, learning.getFrecuencia());
    }

    @Test
    void extraerFrecuenciasGlobalesSinFiltrarStopwords() {
        PreprocesamientoService service = new PreprocesamientoService();

        ArticuloDocumental art = new ArticuloDocumental(
                1,
                "art1.pdf",
                "Titulo 1",
                List.of("Autor 1"),
                "Original",
                "the education",
                List.of("the", "education"),
                2
        );

        List<TerminoFrecuencia> frecuencias = service.extraerFrecuenciasGlobales(List.of(art), false);

        assertTrue(frecuencias.stream().anyMatch(tf -> tf.getTermino().equals("the")));
        assertTrue(frecuencias.stream().anyMatch(tf -> tf.getTermino().equals("education")));
    }

    @Test
    void obtenerCorpusDesdeJsonExistente() throws IOException {
        PreprocesamientoService service = new PreprocesamientoService();

        Path jsonPath = Path.of("datos", "procesados", "corpus_procesado.json");
        if (Files.exists(jsonPath)) {
            List<ArticuloDocumental> corpus = service.obtenerCorpus(Path.of("datos", "pdfs_originales"), jsonPath);
            assertNotNull(corpus);
            assertFalse(corpus.isEmpty());
            assertEquals(20, corpus.size());
        }
    }
}

package com.uniquindio.bibliometria.ingesta;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.*;

class ConsolaPdfTest {
    @TempDir Path carpeta;

    @Test void muestraCadaPalabraEntreComillas() {
        assertEquals("[\"how\", \"ai\"]", ConsolaPdf.mostrarPalabras(List.of("how", "ai")));
        assertEquals("[\"how,ai\"]", ConsolaPdf.mostrarPalabras(List.of("how,ai")));
    }

    @Test void avisaCuandoElCorpusEstaVacio() throws Exception {
        var empty = carpeta.resolve("empty.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(empty))) { }
        assertThrows(IOException.class, () -> ConsolaPdf.cargarCorpus(empty));
    }
    @Test void avisaDelPdfInvalidoSinExtraerElZipAlDisco() throws Exception {
        var corpus = carpeta.resolve("corpus.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(corpus))) {
            zip.putNextEntry(new ZipEntry("../broken.pdf"));
            zip.write(new byte[]{1,2,3});
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("note.txt"));
            zip.write(new byte[]{4});
            zip.closeEntry();
        }
        var articulos = ConsolaPdf.cargarCorpus(corpus);
        assertEquals(1, articulos.size());
        assertFalse(articulos.get(0).tieneCamposCompletos());
        assertTrue(articulos.get(0).observaciones().get(0).contains("No se pudo leer"));
        assertFalse(Files.exists(carpeta.resolve("broken.pdf")));
        try (var paths = Files.list(carpeta)) { assertEquals(1, paths.count()); }
    }
    @Test void rechazaComandosInvalidosAntesDeLeerLosPdf() {
        var salida = new PrintStream(new ByteArrayOutputStream());
        assertThrows(IllegalArgumentException.class, () -> ConsolaPdf.ejecutar(new String[0], salida));
        assertThrows(IllegalArgumentException.class, () -> ConsolaPdf.ejecutar(
                new String[]{"inspeccionar", "missing.zip", "1", "--unknown"}, salida));
        assertThrows(IllegalArgumentException.class, () -> ConsolaPdf.ejecutar(
                new String[]{"inspeccionar", "missing.zip", "0"}, salida));
        assertThrows(IllegalArgumentException.class, () -> ConsolaPdf.ejecutar(
                new String[]{"inspeccionar", "missing.zip", "abc"}, salida));
    }

    private ProcesadorDocumentos procesadorQueRegistraLecturas(List<String> lecturas) {
        return new ProcesadorDocumentos((archivo, bytes) -> {
            lecturas.add(archivo);
            assertArrayEquals(new byte[]{1,2,3}, bytes);
            return new DocumentoPdf(archivo, "", List.of());
        }, new ExtractorArticulos(), new com.uniquindio.bibliometria.preprocesamiento.ProcesadorResumen());
    }

    private Path zipDeSeleccion() throws IOException {
        Path corpus = carpeta.resolve("selected.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(corpus))) {
            for (String nombre : List.of("10. third.pdf", "2. second.PDF", "1. first.pdf", "note.txt")) {
                zip.putNextEntry(new ZipEntry(nombre));
                zip.write(new byte[]{1,2,3});
                zip.closeEntry();
            }
        }
        return corpus;
    }

    @Test void inspeccionaSoloElArticuloElegidoDelZip() throws Exception {
        var lecturas = new ArrayList<String>();
        var resultado = ConsolaPdf.cargarSeleccion(zipDeSeleccion(), List.of(2), procesadorQueRegistraLecturas(lecturas));
        assertEquals(3, resultado.totalDocumentos());
        assertEquals(1, resultado.documentosLeidos());
        assertEquals(List.of("2. second.PDF"), lecturas);
        assertEquals("2. second.PDF", resultado.articulos().get(0).archivo());
    }

    @Test void conservaElOrdenYLeeUnaVezLosArticulosRepetidos() throws Exception {
        var lecturas = new ArrayList<String>();
        var resultado = ConsolaPdf.cargarSeleccion(zipDeSeleccion(), List.of(3,1,3), procesadorQueRegistraLecturas(lecturas));
        assertEquals(List.of("10. third.pdf", "1. first.pdf"), lecturas);
        assertEquals(2, resultado.documentosLeidos());
        assertEquals(List.of("10. third.pdf", "1. first.pdf", "10. third.pdf"),
                resultado.articulos().stream().map(ArticuloExtraido::archivo).toList());
        assertSame(resultado.articulos().get(0), resultado.articulos().get(2));
    }

    @Test void rechazaLaSeleccionFueraDeRangoAntesDeLeer() throws Exception {
        var lecturas = new ArrayList<String>();
        Path corpus = zipDeSeleccion();
        assertThrows(IllegalArgumentException.class, () -> ConsolaPdf.cargarSeleccion(
                corpus, List.of(1,4), procesadorQueRegistraLecturas(lecturas)));
        assertTrue(lecturas.isEmpty());
    }

    @Test void leeSoloElPdfElegidoDeLaCarpeta() throws Exception {
        for (String nombre : List.of("10. third.pdf", "2. second.pdf", "1. first.pdf")) {
            Files.write(carpeta.resolve(nombre), new byte[]{1,2,3});
        }
        var lecturas = new ArrayList<String>();
        var resultado = ConsolaPdf.cargarSeleccion(carpeta, List.of(1), procesadorQueRegistraLecturas(lecturas));
        assertEquals(3, resultado.totalDocumentos());
        assertEquals(1, lecturas.size());
        assertTrue(lecturas.get(0).endsWith("1. first.pdf"));
    }
}

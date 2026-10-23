package com.uniquindio.bibliometria.ingesta;

import com.uniquindio.bibliometria.preprocesamiento.OpcionesPreprocesamiento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchivoArticulosJsonTest {
    @TempDir Path carpeta;
    private final ArchivoArticulosJson archivoJson = new ArchivoArticulosJson();

    @Test void guardaYLeeLosCamposEnEspanolSinCambiarElOriginal() throws Exception {
        String original = "Educación \"IA\".\nSegunda línea: 2024.";
        var articulo = new ArticuloExtraido("C:\\documentos\\articulo.pdf", "Título con ñ",
                List.of("María Pérez", "José Díaz"), original, List.of("Revisado manualmente"));
        Path destino = carpeta.resolve("procesados/articulos.json");
        archivoJson.guardar(List.of(articulo), destino, OpcionesPreprocesamiento.limpiezaMinima());

        var datos = archivoJson.leer(destino);
        assertEquals(1, datos.size());
        assertEquals("articulo.pdf", datos.get(0).archivo());
        assertEquals(articulo.titulo(), datos.get(0).titulo());
        assertEquals(articulo.autores(), datos.get(0).autores());
        assertEquals(original, datos.get(0).resumenOriginal());
        assertEquals(original, articulo.resumenOriginal());
        assertEquals("educación \"ia\". segunda línea: 2024.", datos.get(0).resumenProcesado());
        assertEquals(List.of("educación", "ia", "segunda", "línea", "2024"), datos.get(0).palabras());
        assertEquals(articulo.observaciones(), datos.get(0).observaciones());
        var nodo = new JsonMapper().readTree(Files.readString(destino, StandardCharsets.UTF_8));
        assertEquals(7, nodo.get(0).size());
        for (String campo : List.of("archivo", "titulo", "autores", "resumenOriginal",
                "resumenProcesado", "palabras", "observaciones")) {
            assertTrue(nodo.get(0).has(campo), campo);
        }
    }

    @Test void rechazaCamposFaltantesSinReemplazarUnJsonExistente() throws Exception {
        Path destino = carpeta.resolve("articulos.json");
        Files.writeString(destino, "[]", StandardCharsets.UTF_8);
        var incompleto = new ArticuloExtraido("fallido.pdf", "", List.of(), "", List.of());
        assertThrows(IllegalArgumentException.class, () -> archivoJson.guardar(List.of(incompleto),
                destino, OpcionesPreprocesamiento.limpiezaMinima()));
        assertEquals("[]", Files.readString(destino));
    }

    @Test void noPermiteUsarUnPdfComoArchivoDeSalida() throws Exception {
        Path pdf = carpeta.resolve("original.pdf");
        Files.write(pdf, new byte[]{1, 2, 3});
        var articulo = new ArticuloExtraido("original.pdf", "Titulo", List.of("Autor"), "Resumen", List.of());
        assertThrows(IllegalArgumentException.class, () -> archivoJson.guardar(List.of(articulo), pdf,
                OpcionesPreprocesamiento.limpiezaMinima()));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(pdf));
    }

    @Test void rechazaLaExportacionSinArticulos() {
        Path destino = carpeta.resolve("articulos.json");
        assertThrows(IllegalArgumentException.class, () -> archivoJson.guardar(List.of(), destino,
                OpcionesPreprocesamiento.limpiezaMinima()));
        assertFalse(Files.exists(destino));
    }
}

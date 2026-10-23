package com.uniquindio.bibliometria.controller;

import com.uniquindio.bibliometria.algoritmos.ordenamiento.BucketSort;
import com.uniquindio.bibliometria.algoritmos.ordenamiento.ResultadoOrdenamiento;
import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;
import com.uniquindio.bibliometria.service.PreprocesamientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Controlador REST que expone los datos del corpus y el algoritmo Bucket Sort a través de HTTP.
 *
 * Endpoints disponibles:
 *   GET /api/bucket-sort?topK=15&filtrarStopwords=true
 *   GET /api/articulos
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // Permite llamadas desde cualquier origen (React en localhost:5173)
public class BucketSortController {

    // Rutas del corpus (relativas al directorio de trabajo del proyecto)
    private static final Path RUTA_PDFS = Path.of("datos", "pdfs_originales");
    private static final Path RUTA_JSON = Path.of("datos", "procesados", "corpus_procesado.json");

    private final PreprocesamientoService preprocesamientoService;

    public BucketSortController() {
        this.preprocesamientoService = new PreprocesamientoService();
    }

    /**
     * Retorna la lista de todos los artículos científicos utilizados en el estudio.
     */
    @GetMapping("/articulos")
    public ResponseEntity<List<ArticuloResumenDto>> obtenerArticulos() {
        try {
            List<ArticuloDocumental> corpus = preprocesamientoService.obtenerCorpus(RUTA_PDFS, RUTA_JSON);
            List<ArticuloResumenDto> dtos = corpus.stream()
                    .map(a -> new ArticuloResumenDto(
                            a.id(),
                            a.archivo(),
                            a.titulo(),
                            a.autores(),
                            a.abstractOriginal(),
                            a.totalTokens()
                    ))
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ejecuta el algoritmo Bucket Sort sobre el corpus y devuelve el ranking de términos
     * junto con los parámetros analíticos (n, B, min, max, tiempo).
     *
     * @param topK            Número de términos a retornar en el ranking (por defecto 15).
     * @param filtrarStopwords Si es true, excluye palabras vacías como "the", "and", etc. (por defecto true).
     * @return JSON con los resultados del ordenamiento: lista de términos, tiempo, cubetas, etc.
     */
    @GetMapping("/bucket-sort")
    public ResponseEntity<RespuestaBucketSort> ejecutarBucketSort(
            @RequestParam(defaultValue = "15") int topK,
            @RequestParam(defaultValue = "true") boolean filtrarStopwords) {

        try {
            // 1. Cargar el corpus desde el JSON ya generado (o procesarlo si no existe)
            List<ArticuloDocumental> corpus = preprocesamientoService.obtenerCorpus(RUTA_PDFS, RUTA_JSON);

            // 2. Extraer frecuencias globales de los tokens del corpus
            List<TerminoFrecuencia> frecuencias = preprocesamientoService.extraerFrecuenciasGlobales(corpus, filtrarStopwords);

            // 3. Ordenar con Bucket Sort (implementación explícita, sin Collections.sort)
            ResultadoOrdenamiento resultado = BucketSort.ordenarDescendente(frecuencias);

            // 4. Construir la respuesta que enviará Spring Boot al frontend como JSON
            List<TerminoFrecuenciaDto> topTerminos = resultado.getTop(topK).stream()
                    .map(tf -> new TerminoFrecuenciaDto(tf.getTermino(), tf.getFrecuencia()))
                    .toList();

            RespuestaBucketSort respuesta = new RespuestaBucketSort(
                    resultado.algoritmo(),
                    topTerminos,
                    resultado.tiempoMilisegundos(),
                    resultado.totalElementos(),
                    resultado.numeroCubetas(),
                    resultado.frecuenciaMinima(),
                    resultado.frecuenciaMaxima(),
                    topK,
                    filtrarStopwords,
                    corpus.size()
            );

            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            // Si hay un error al leer el JSON o los PDFs, devolver 500
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── DTOs (Data Transfer Objects) ───────────────────────────────────────────

    /**
     * DTO para cada término y su frecuencia en el ranking.
     */
    public record TerminoFrecuenciaDto(String termino, int frecuencia) {}

    /**
     * DTO para los artículos del estudio mostrados en la ventana de información.
     */
    public record ArticuloResumenDto(
            int id,
            String archivo,
            String titulo,
            List<String> autores,
            String resumenOriginal,
            int totalPalabras
    ) {}

    /**
     * DTO completo de la respuesta del endpoint /api/bucket-sort.
     */
    public record RespuestaBucketSort(
            String algoritmo,
            List<TerminoFrecuenciaDto> terminos,
            double tiempoMs,
            int totalElementos,
            int numeroCubetas,
            int frecuenciaMinima,
            int frecuenciaMaxima,
            int topK,
            boolean stopwordsFiltradas,
            int totalArticulos
    ) {}
}

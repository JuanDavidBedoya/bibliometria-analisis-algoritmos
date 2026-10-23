package com.uniquindio.bibliometria.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uniquindio.bibliometria.algoritmos.ordenamiento.CountingSort;
import com.uniquindio.bibliometria.algoritmos.ordenamiento.ResultadoOrdenamiento;
import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;
import com.uniquindio.bibliometria.service.PreprocesamientoService;

/**
 * Controlador REST que expone el algoritmo COUNTING SORT a través de HTTP.
 *
 * Endpoint disponible:
 *   GET /api/counting-sort?topK=15&filtrarStopwords=true
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite llamadas desde cualquier origen (React en localhost:5173)
public class CountingSortController {

    // Rutas del corpus relativas al directorio de trabajo
    private static final Path RUTA_PDFS = Path.of("datos", "pdfs_originales");
    private static final Path RUTA_JSON = Path.of("datos", "procesados", "corpus_procesado.json");

    private final PreprocesamientoService preprocesamientoService;

    public CountingSortController() {
        this.preprocesamientoService = new PreprocesamientoService();
    }

    /**
     * Ejecuta el algoritmo Counting Sort sobre el corpus documental y devuelve el ranking
     * de términos más frecuentes junto con los parámetros analíticos (n, k, min, max, tiempo).
     *
     * @param topK             Cantidad de términos a incluir en el ranking (por defecto 15).
     * @param filtrarStopwords Si es true, excluye palabras vacías funcionales (por defecto true).
     * @return JSON con el resultado del ordenamiento y métricas empíricas.
     */
    @GetMapping("/counting-sort")
    public ResponseEntity<RespuestaCountingSort> ejecutarCountingSort(
            @RequestParam(defaultValue = "15") int topK,
            @RequestParam(defaultValue = "true") boolean filtrarStopwords) {

        try {
            // 1. Obtener o cargar el corpus documental
            List<ArticuloDocumental> corpus = preprocesamientoService.obtenerCorpus(RUTA_PDFS, RUTA_JSON);

            // 2. Extraer las frecuencias globales de términos
            List<TerminoFrecuencia> frecuencias = preprocesamientoService.extraerFrecuenciasGlobales(corpus, filtrarStopwords);

            // 3. Ejecutar Counting Sort (implementación explícita sin funciones de alto nivel)
            ResultadoOrdenamiento resultado = CountingSort.ordenarDescendente(frecuencias);

            // 4. Mapear al Top K solicitado
            List<TerminoFrecuenciaDto> topTerminos = resultado.getTop(topK).stream()
                    .map(tf -> new TerminoFrecuenciaDto(tf.getTermino(), tf.getFrecuencia()))
                    .toList();

            // 5. Construir y retornar el DTO de respuesta
            RespuestaCountingSort respuesta = new RespuestaCountingSort(
                    resultado.algoritmo(),
                    topTerminos,
                    resultado.tiempoMilisegundos(),
                    resultado.totalElementos(),
                    resultado.numeroCubetas(), // Corresponde al rango k = max - min + 1
                    resultado.frecuenciaMinima(),
                    resultado.frecuenciaMaxima(),
                    topK,
                    filtrarStopwords,
                    corpus.size()
            );

            return ResponseEntity.ok(respuesta);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── DTOs (Data Transfer Objects) ───────────────────────────────────────────

    public record TerminoFrecuenciaDto(String termino, int frecuencia) {}

    public record RespuestaCountingSort(
            String algoritmo,
            List<TerminoFrecuenciaDto> terminos,
            double tiempoMs,
            int totalElementos,
            int rangoK,
            int frecuenciaMinima,
            int frecuenciaMaxima,
            int topK,
            boolean stopwordsFiltradas,
            int totalArticulos
    ) {}
}

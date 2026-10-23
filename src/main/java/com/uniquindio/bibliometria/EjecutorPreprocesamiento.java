package com.uniquindio.bibliometria;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.service.PreprocesamientoService;

/**
 * Módulo independiente de PREPROCESAMIENTO del corpus documental.
 *
 * PROPÓSITO:
 * Se ejecuta UNA SOLA VEZ para:
 * 1. Leer los 20 artículos científicos en PDF desde el directorio 'datos/pdfs_originales'.
 * 2. Extraer metadatos esenciales (título del proyecto, autores, abstract original,
 *    abstract procesado y lista de tokens).
 * 3. Almacenar toda la información estructurada en el archivo centralizado
 *    'datos/procesados/corpus_procesado.json'.
 *
 * Una vez generado el archivo JSON, los algoritmos de análisis (Bucket Sort, Similitud Clásica,
 * Similitud con IA, Agrupamiento Jerárquico) pueden ejecutarse indefinidamente accediendo
 * directamente a la información en memoria sin volver a procesar los documentos PDF.
 */
public class EjecutorPreprocesamiento {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  MÓDULO DE PREPROCESAMIENTO DEL CORPUS CIENTÍFICO (EJECUCIÓN ÚNICA)");
        System.out.println("  UNIVERSIDAD DEL QUINDÍO - ANÁLISIS DE ALGORITMOS EN BIBLIOMETRÍA");
        System.out.println("=========================================================================\n");

        Path rutaPdfs = Path.of("datos", "pdfs_originales");
        Path rutaJson = Path.of("datos", "procesados", "corpus_procesado.json");

        if (args.length > 0) {
            rutaPdfs = Path.of(args[0]);
        }
        if (args.length > 1) {
            rutaJson = Path.of(args[1]);
        }

        try {
            System.out.println(">>> 1. Iniciando lectura y extracción de PDFs...");
            System.out.println("       Directorio de origen: " + rutaPdfs.toAbsolutePath());
            System.out.println("       Destino JSON:         " + rutaJson.toAbsolutePath());

            long inicio = System.currentTimeMillis();
            PreprocesamientoService servicio = new PreprocesamientoService();
            List<ArticuloDocumental> articulos = servicio.procesarYGuardarCorpus(rutaPdfs, rutaJson);
            long duracion = System.currentTimeMillis() - inicio;

            System.out.println("\n>>> 2. ¡Extracción completada con éxito!");
            System.out.printf("       Total artículos procesados: %d%n", articulos.size());
            System.out.printf("       Tamaño del archivo JSON:    %d bytes%n", Files.size(rutaJson));
            System.out.printf("       Tiempo total:               %d ms%n%n", duracion);

            System.out.println(">>> 3. Resumen de metadatos extraídos por artículo:");
            System.out.println("-------------------------------------------------------------------------");
            for (ArticuloDocumental a : articulos) {
                System.out.printf("Art %02d | %s%n", a.id(), a.titulo());
                System.out.printf("        Autores (%d): %s%n", a.autores().size(), String.join("; ", a.autores()));
                System.out.printf("        Tokens: %d palabras | Resumen procesado: %s...%n",
                        a.totalTokens(),
                        a.abstractProcesado().substring(0, Math.min(80, a.abstractProcesado().length())));
            }
            System.out.println("-------------------------------------------------------------------------\n");

            System.out.println(">>> ¡PREPROCESAMIENTO FINALIZADO EXITOSAMENTE!");
            System.out.println("    Los datos quedaron guardados en 'datos/procesados/corpus_procesado.json'.");
            System.out.println("    Ahora puedes ejecutar el algoritmo Bucket Sort cuantas veces desees");
            System.out.println("    utilizando la clase 'EjecutorBucketSort' de manera instantánea.");

        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo completar el preprocesamiento: " + e.getMessage());
        }
    }
}

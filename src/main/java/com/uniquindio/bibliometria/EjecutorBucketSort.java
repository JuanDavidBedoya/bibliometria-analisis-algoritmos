package com.uniquindio.bibliometria;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import com.uniquindio.bibliometria.algoritmos.ordenamiento.BucketSort;
import com.uniquindio.bibliometria.algoritmos.ordenamiento.ResultadoOrdenamiento;
import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;
import com.uniquindio.bibliometria.service.PreprocesamientoService;
import com.uniquindio.bibliometria.util.JsonUtil;

/**
 * Módulo independiente de ejecución del algoritmo BUCKET SORT.
 *
 * CARACTERÍSTICAS PRINCIPALES:
 * 1. Acceso instantáneo: Lee los datos directamente desde 'datos/procesados/corpus_procesado.json'
 *    sin volver a procesar los documentos PDF.
 * 2. Ejecución repetitiva: El usuario puede ejecutar el algoritmo tantas veces como lo desee,
 *    probando diferentes configuraciones (filtrado de stopwords, número de cubetas, tamaño de Top).
 * 3. Medición empírica precisa: En cada ejecución se mide el tiempo real en milisegundos.
 * 4. Modo interactivo o automático: Soporta menú en consola o ejecución directa.
 */
public class EjecutorBucketSort {

    public static void main(String[] args) {
        Path rutaJson = Path.of("datos", "procesados", "corpus_procesado.json");
        Path rutaPdfs = Path.of("datos", "pdfs_originales");

        if (args.length > 0 && !args[0].startsWith("-")) {
            rutaJson = Path.of(args[0]);
        }

        System.out.println("=========================================================================");
        System.out.println("  MÓDULO DE ORDENAMIENTO BIBLIOMÉTRICO: BUCKET SORT");
        System.out.println("  UNIVERSIDAD DEL QUINDÍO - ANÁLISIS DE ALGORITMOS");
        System.out.println("=========================================================================\n");

        PreprocesamientoService servicio = new PreprocesamientoService();
        List<ArticuloDocumental> articulos;

        // 1. Verificar existencia del JSON preprocesado
        if (!Files.exists(rutaJson)) {
            System.out.println("[AVISO] No se encontró el archivo preprocesado en: " + rutaJson.toAbsolutePath());
            System.out.println("        Generando preprocesamiento por primera vez...");
            try {
                articulos = servicio.procesarYGuardarCorpus(rutaPdfs, rutaJson);
                System.out.println("        [OK] Preprocesamiento inicial completado.");
            } catch (IOException e) {
                System.err.println("[ERROR] No se pudo leer el corpus para generar el JSON: " + e.getMessage());
                return;
            }
        } else {
            // Carga directa y ultra-rápida desde el JSON existente
            try {
                long t0 = System.currentTimeMillis();
                articulos = JsonUtil.cargarArticulosDesdeJson(rutaJson);
                long t1 = System.currentTimeMillis();
                System.out.printf(">>> [OK] Corpus cargado desde JSON en %d ms (%d artículos).%n%n", (t1 - t0), articulos.size());
            } catch (IOException e) {
                System.err.println("[ERROR] Error al leer el archivo JSON: " + e.getMessage());
                return;
            }
        }

        // Si se pasa el argumento "--auto", ejecuta directamente la demostración estándar
        boolean modoAuto = args.length > 0 && args[0].equalsIgnoreCase("--auto");
        if (modoAuto) {
            ejecutarDemostracionEstandar(servicio, articulos, 15);
            return;
        }

        try (// Menú interactivo para ejecutar cuantas veces se desee
        Scanner scanner = new Scanner(System.in)) {
            int topK = 15;
            boolean salir = false;

            while (!salir && scanner.hasNextLine()) {
                System.out.println("-------------------------------------------------------------------------");
                System.out.println("  MENÚ DE EJECUCIÓN - BUCKET SORT (Top actual: " + topK + " términos)");
                System.out.println("-------------------------------------------------------------------------");
                System.out.println("  1. Ejecutar Bucket Sort con vocabulario temático (sin stopwords)");
                System.out.println("  2. Ejecutar Bucket Sort con vocabulario completo (con stopwords)");
                System.out.println("  3. Ejecutar Bucket Sort con número de cubetas personalizado");
                System.out.println("  4. Cambiar la cantidad de términos del Top (actual: " + topK + ")");
                System.out.println("  5. Ejecutar demostración completa (temático + completo)");
                System.out.println("  0. Salir");
                System.out.print("  Seleccione una opción: ");

                String entrada = scanner.nextLine().trim();

                switch (entrada) {
                    case "1" -> {
                        System.out.println("\n>>> Extrayendo vocabulario temático...");
                        List<TerminoFrecuencia> frecuencias = servicio.extraerFrecuenciasGlobales(articulos, true);
                        System.out.printf("    Total términos únicos: %d. Ejecutando Bucket Sort...%n", frecuencias.size());
                        ResultadoOrdenamiento res = BucketSort.ordenarDescendente(frecuencias);
                        res.imprimirTop(topK);
                        System.out.println();
                    }
                    case "2" -> {
                        System.out.println("\n>>> Extrayendo vocabulario completo...");
                        List<TerminoFrecuencia> frecuencias = servicio.extraerFrecuenciasGlobales(articulos, false);
                        System.out.printf("    Total términos únicos: %d. Ejecutando Bucket Sort...%n", frecuencias.size());
                        ResultadoOrdenamiento res = BucketSort.ordenarDescendente(frecuencias);
                        res.imprimirTop(topK);
                        System.out.println();
                    }
                    case "3" -> {
                        System.out.print("  Ingrese el número de cubetas deseado (ej. 10, 20, 50, 100): ");
                        try {
                            int numCubetas = Integer.parseInt(scanner.nextLine().trim());
                            if (numCubetas <= 0) {
                                System.out.println("  [!] El número de cubetas debe ser mayor a 0.");
                                break;
                            }
                            System.out.print("  ¿Desea filtrar stopwords? (s/n, por defecto s): ");
                            String resp = scanner.nextLine().trim().toLowerCase();
                            boolean filtrar = !resp.equals("n");

                            List<TerminoFrecuencia> frecuencias = servicio.extraerFrecuenciasGlobales(articulos, filtrar);
                            ResultadoOrdenamiento res = BucketSort.ordenar(frecuencias, numCubetas, true);
                            res.imprimirTop(topK);
                            System.out.println();
                        } catch (NumberFormatException e) {
                            System.out.println("  [!] Valor inválido.");
                        }
                    }
                    case "4" -> {
                        System.out.print("  Ingrese nuevo tamaño de Top (ej. 10, 15, 20, 50): ");
                        try {
                            int nuevoK = Integer.parseInt(scanner.nextLine().trim());
                            if (nuevoK > 0) {
                                topK = nuevoK;
                                System.out.println("  [OK] Tamaño de Top actualizado a: " + topK);
                            } else {
                                System.out.println("  [!] El número debe ser mayor a 0.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("  [!] Entrada inválida.");
                        }
                    }
                    case "5" -> ejecutarDemostracionEstandar(servicio, articulos, topK);
                    case "0", "q", "salir" -> {
                        System.out.println("\n>>> Finalizando ejecutor de Bucket Sort. ¡Hasta pronto!");
                        salir = true;
                    }
                    default -> System.out.println("  [!] Opción no reconocida. Intente nuevamente.");
                }
            }

            scanner.close();
        }
    }

    /**
     * Ejecuta una pasada estándar de Bucket Sort mostrando tanto el Top temático
     * como el vocabulario general con sus métricas empíricas.
     */
    private static void ejecutarDemostracionEstandar(PreprocesamientoService servicio, List<ArticuloDocumental> articulos, int topK) {
        System.out.println(">>> EJECUTANDO DEMOSTRACIÓN ESTÁNDAR DE BUCKET SORT:");

        // 1. Términos temáticos
        List<TerminoFrecuencia> tematicos = servicio.extraerFrecuenciasGlobales(articulos, true);
        ResultadoOrdenamiento resTematico = BucketSort.ordenarDescendente(tematicos);
        System.out.println("\n>>> TABLA 1: RANKING TEMÁTICO (RELEVANCIA BIBLIOMÉTRICA - SIN STOPWORDS)");
        resTematico.imprimirTop(topK);

        // 2. Vocabulario completo
        List<TerminoFrecuencia> completos = servicio.extraerFrecuenciasGlobales(articulos, false);
        ResultadoOrdenamiento resCompleto = BucketSort.ordenarDescendente(completos);
        System.out.println("\n>>> TABLA 2: VOCABULARIO COMPLETO (INCLUYE STOPWORDS)");
        resCompleto.imprimirTop(topK);
        System.out.println();
    }
}

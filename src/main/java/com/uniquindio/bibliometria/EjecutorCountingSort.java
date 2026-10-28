package com.uniquindio.bibliometria;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import com.uniquindio.bibliometria.algoritmos.ordenamiento.CountingSort;
import com.uniquindio.bibliometria.algoritmos.ordenamiento.ResultadoOrdenamiento;
import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;
import com.uniquindio.bibliometria.service.PreprocesamientoService;
import com.uniquindio.bibliometria.util.JsonUtil;

public class EjecutorCountingSort {

    public static void main(String[] args) {
        Path rutaJson = Path.of("datos", "procesados", "corpus_procesado.json");
        Path rutaPdfs = Path.of("datos", "pdfs_originales");

        if (args.length > 0 && !args[0].startsWith("-")) {
            rutaJson = Path.of(args[0]);
        }

        PreprocesamientoService servicio = new PreprocesamientoService();
        List<ArticuloDocumental> articulos;

        // 1. Verificar existencia del JSON preprocesado
        if (!Files.exists(rutaJson)) {
            try {
                articulos = servicio.procesarYGuardarCorpus(rutaPdfs, rutaJson);
            } catch (IOException e) {
                return;
            }
        } else {
            // Carga directa desde el archivo JSON existente
            try {
                articulos = JsonUtil.cargarArticulosDesdeJson(rutaJson);
            } catch (IOException e) {
                return;
            }
        }

        // Si se pasa el argumento "--auto", ejecuta directamente la demostracion estandar
        boolean modoAuto = args.length > 0 && args[0].equalsIgnoreCase("--auto");
        if (modoAuto) {
            ejecutarDemostracionEstandar(servicio, articulos, 15);
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            int topK = 15;
            boolean salir = false;

            while (!salir && scanner.hasNextLine()) {
                String entrada = scanner.nextLine().trim();

                switch (entrada) {
                    case "1" -> {
                        List<TerminoFrecuencia> frecuencias = servicio.extraerFrecuenciasGlobales(articulos, true);
                        ResultadoOrdenamiento res = CountingSort.ordenarDescendente(frecuencias);
                        res.imprimirTop(topK);
                    }
                    case "2" -> {
                        List<TerminoFrecuencia> frecuencias = servicio.extraerFrecuenciasGlobales(articulos, false);
                        ResultadoOrdenamiento res = CountingSort.ordenarDescendente(frecuencias);
                        res.imprimirTop(topK);
                    }
                    case "3" -> {
                        try {
                            int nuevoK = Integer.parseInt(scanner.nextLine().trim());
                            if (nuevoK > 0) {
                                topK = nuevoK;
                            }
                        } catch (NumberFormatException e) {
                            // entrada invalida, ignorar
                        }
                    }
                    case "4" -> ejecutarDemostracionEstandar(servicio, articulos, topK);
                    case "0", "q", "salir" -> salir = true;
                    default -> { /* opcion no reconocida */ }
                }
            }
        }
    }

    /**
     * Ejecuta una pasada estandar de Counting Sort mostrando tanto el Top tematico
     * como el vocabulario general con sus metricas empiricas de desempeno.
     */
    private static void ejecutarDemostracionEstandar(PreprocesamientoService servicio, List<ArticuloDocumental> articulos, int topK) {
        // 1. Terminos tematicos (sin stopwords)
        List<TerminoFrecuencia> tematicos = servicio.extraerFrecuenciasGlobales(articulos, true);
        ResultadoOrdenamiento resTematico = CountingSort.ordenarDescendente(tematicos);
        resTematico.imprimirTop(topK);

        // 2. Vocabulario completo (con stopwords)
        List<TerminoFrecuencia> completos = servicio.extraerFrecuenciasGlobales(articulos, false);
        ResultadoOrdenamiento resCompleto = CountingSort.ordenarDescendente(completos);
        resCompleto.imprimirTop(topK);
    }
}

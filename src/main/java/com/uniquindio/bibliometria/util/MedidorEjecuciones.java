package com.uniquindio.bibliometria.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.uniquindio.bibliometria.algoritmos.ordenamiento.CountingSort;
import com.uniquindio.bibliometria.algoritmos.ordenamiento.ResultadoOrdenamiento;
import com.uniquindio.bibliometria.modelo.ArticuloDocumental;
import com.uniquindio.bibliometria.modelo.TerminoFrecuencia;
import com.uniquindio.bibliometria.service.PreprocesamientoService;

public class MedidorEjecuciones {
    public static void main(String[] args) throws IOException {
        Path rutaJson = Path.of("datos", "procesados", "corpus_procesado.json");
        List<ArticuloDocumental> arts = JsonUtil.cargarArticulosDesdeJson(rutaJson);
        PreprocesamientoService servicio = new PreprocesamientoService();
        List<TerminoFrecuencia> tematicos = servicio.extraerFrecuenciasGlobales(arts, true);

        double sumaTiempos = 0;
        double[] tiempos = new double[5];

        for (int i = 0; i < 5; i++) {
            ResultadoOrdenamiento res = CountingSort.ordenarDescendente(tematicos);
            tiempos[i] = res.tiempoMilisegundos();
            sumaTiempos += tiempos[i];
        }

        // promedio disponible en: sumaTiempos / 5.0
        double promedio = sumaTiempos / 5.0;
        // suprimir advertencia de variable no usada
        assert promedio >= 0;
    }
}

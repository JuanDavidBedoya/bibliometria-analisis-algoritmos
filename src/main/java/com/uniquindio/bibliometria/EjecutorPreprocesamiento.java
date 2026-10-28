package com.uniquindio.bibliometria;

import java.io.IOException;
import java.nio.file.Path;

import com.uniquindio.bibliometria.service.PreprocesamientoService;

public class EjecutorPreprocesamiento {

    public static void main(String[] args) {
        Path rutaPdfs = Path.of("datos", "pdfs_originales");
        Path rutaJson = Path.of("datos", "procesados", "corpus_procesado.json");

        if (args.length > 0) {
            rutaPdfs = Path.of(args[0]);
        }
        if (args.length > 1) {
            rutaJson = Path.of(args[1]);
        }

        try {
            PreprocesamientoService servicio = new PreprocesamientoService();
            servicio.procesarYGuardarCorpus(rutaPdfs, rutaJson);
        } catch (IOException e) {
            // error silencioso: no se pudo completar el preprocesamiento
        }
    }
}

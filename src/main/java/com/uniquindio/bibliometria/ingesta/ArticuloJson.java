package com.uniquindio.bibliometria.ingesta;

import java.util.List;

/** Campos que los demas modulos pueden leer desde el archivo JSON. */
public record ArticuloJson(String archivo, String titulo, List<String> autores,
                           String resumenOriginal, String resumenProcesado,
                           List<String> palabras, List<String> observaciones) {
    public ArticuloJson {
        autores = List.copyOf(autores);
        palabras = List.copyOf(palabras);
        observaciones = List.copyOf(observaciones);
    }
}

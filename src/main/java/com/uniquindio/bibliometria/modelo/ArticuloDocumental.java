package com.uniquindio.bibliometria.modelo;

import java.util.List;
import java.util.Objects;

/**
 * Representa un artículo científico del corpus documental con sus metadatos
 * y contenido extraído y preprocesado.
 *
 * Esta clase sirve como estructura central de datos para que todos los
 * algoritmos del proyecto (ordenamiento, similitud clásica, similitud con IA,
 * agrupamiento jerárquico) puedan acceder a la información de manera homogénea.
 */
public record ArticuloDocumental(
        int id,
        String archivo,
        String titulo,
        List<String> autores,
        String abstractOriginal,
        String abstractProcesado,
        List<String> tokens,
        int totalTokens
) {
    public ArticuloDocumental {
        Objects.requireNonNull(archivo, "archivo no puede ser nulo");
        Objects.requireNonNull(titulo, "titulo no puede ser nulo");
        Objects.requireNonNull(abstractOriginal, "abstractOriginal no puede ser nulo");
        Objects.requireNonNull(abstractProcesado, "abstractProcesado no puede ser nulo");
        autores = autores == null ? List.of() : List.copyOf(autores);
        tokens = tokens == null ? List.of() : List.copyOf(tokens);
    }
}
